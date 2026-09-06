package com.endiq.client.accounts

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.endiq.client.gui.components.TextInput
import java.net.URI
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.CancellationException
import kotlin.test.*

class AccountsTest {
    private val clientId="d39dc39d-c99b-4a9e-b91a-9f27870b15ef"
    private fun reply(status:Int,body:String)=MicrosoftLogin.Reply(status,JsonParser.parseString(body).asJsonObject)
    private class FakeTransport(val replies:MutableList<MicrosoftLogin.Reply>):MicrosoftLogin.Transport {
        val calls=mutableListOf<URI>()
        override fun request(method:String,uri:URI,body:String?,contentType:String?,bearer:String?):MicrosoftLogin.Reply {
            calls+=uri;return replies.removeAt(0)
        }
    }
    private fun device()=reply(200,"""{"user_code":"ABCD-EFGH","device_code":"device-secret","verification_uri":"https://www.microsoft.com/link","expires_in":900,"interval":1}""")
    private fun exchange()= mutableListOf(
        reply(200,"""{"access_token":"test-microsoft-token"}"""),
        reply(200,"""{"Token":"test-xbox-token"}"""),
        reply(200,"""{"Token":"test-xsts-token","DisplayClaims":{"xui":[{"uhs":"test-hash","xid":"123"}]}}"""),
        reply(200,"""{"access_token":"test-minecraft-token","expires_in":3600}"""),
        reply(200,"""{"id":"123456781234123412341234567890ab","name":"TurtleTester"}""")
    )
    @Test fun completeOfficialFlowPreservesVerifiedIdentityWithoutPrintingSecrets() {
        var clock=10000L;val http=FakeTransport((listOf(device())+exchange()).toMutableList())
        val auth=MicrosoftLogin(http,{clock},{clock+=it});val code=auth.begin(clientId)
        val result=auth.complete(clientId,code,MicrosoftLogin.Cancellation())
        assertEquals("TurtleTester",result.profile.name);assertEquals(AccountProfile.Kind.MICROSOFT,result.profile.kind)
        assertEquals(UUID.fromString("12345678-1234-1234-1234-1234567890ab"),result.profile.id)
        assertEquals("123",result.xuid);assertTrue(result.usable(clock));assertFalse(result.usable(clock+3600000))
        assertFalse(result.toString().contains("test-minecraft-token"));assertFalse(code.toString().contains("device-secret"))
        assertEquals(6,http.calls.size);assertTrue(http.calls.all { it.scheme=="https" })
    }
    @Test fun pollingHonorsPendingSlowDownAndCancellation() {
        var clock=0L;val sleeps=mutableListOf<Long>()
        val http=FakeTransport((listOf(device(),reply(400,"""{"error":"authorization_pending"}"""),reply(400,"""{"error":"slow_down"}"""))+exchange()).toMutableList())
        val auth=MicrosoftLogin(http,{clock},{sleeps+=it;clock+=it})
        auth.complete(clientId,auth.begin(clientId),MicrosoftLogin.Cancellation())
        assertEquals(listOf(1000L,1000L,6000L),sleeps)
        val cancel=MicrosoftLogin.Cancellation();cancel.cancel()
        assertFailsWith<CancellationException> { auth.complete(clientId,MicrosoftLogin.DeviceChallenge("CODE","private",URI("https://microsoft.com/link"),100000,1),cancel) }
    }
    @Test fun unlicensedAccountIsRejectedRatherThanSilentlyChangedToOffline() {
        val responses=exchange();responses[4]=reply(404,"{}");var clock=0L
        val auth=MicrosoftLogin(FakeTransport((listOf(device())+responses).toMutableList()),{clock},{clock+=it})
        val error=assertFailsWith<AccountProblem> { auth.complete(clientId,auth.begin(clientId),MicrosoftLogin.Cancellation()) }
        assertTrue(error.message.orEmpty().contains("licensed"))
    }
    @Test fun invalidClientAndUnexpectedBrowserHostAreRejected() {
        val auth=MicrosoftLogin(FakeTransport(mutableListOf()))
        assertFailsWith<AccountProblem> { auth.begin("not-a-client-id") }
        assertFalse(MicrosoftLogin.safeBrowserUri(URI("https://microsoft.com.evil.example/login")))
        assertFalse(MicrosoftLogin.safeBrowserUri(URI("http://microsoft.com/link")))
        assertFalse(MicrosoftLogin.safeBrowserUri(URI("https://microsoft.com@evil.example/link")))
        assertTrue(MicrosoftLogin.safeBrowserUri(URI("https://www.microsoft.com/link")))
    }
    @Test fun parentalAuthorizationErrorsAreNotBypassed() {
        var clock=0L
        val auth=MicrosoftLogin(FakeTransport(mutableListOf(device(),reply(200,"""{"access_token":"test"}"""),reply(200,"""{"Token":"test"}"""),reply(401,"""{"XErr":2148916238}"""))),{clock},{clock+=it})
        val error=assertFailsWith<AccountProblem> { auth.complete(clientId,auth.begin(clientId),MicrosoftLogin.Cancellation()) }
        assertTrue(error.message.orEmpty().contains("parental"))
    }
    @Test fun offlineProfilesUseMinecraftUuidRulesAndValidateNames() {
        val profile=AccountProfile.offline("Alex")
        assertEquals(UUID.nameUUIDFromBytes("OfflinePlayer:Alex".toByteArray()),profile.id)
        assertFailsWith<IllegalArgumentException> { AccountProfile.offline("../invalid") }
        assertFailsWith<IllegalArgumentException> { AccountProfile.offline("ab") }
    }
    @Test fun diskPersistenceContainsMetadataOnlyAndRoundTrips() {
        val dir=Files.createTempDirectory("turtle-account-test")
        try {
            val file=dir.resolve("accounts.json");val repo=AccountRepository(file)
            repo.microsoftClientId=clientId;repo.remember(AccountProfile.offline("TurtleTester"))
            val json=Files.readString(file)
            assertFalse(json.contains("access_token"));assertFalse(json.contains("refresh_token"));assertFalse(json.contains("password"))
            val loaded=AccountRepository(file);loaded.load()
            assertEquals(repo.profiles,loaded.profiles);assertEquals(clientId,loaded.microsoftClientId)
            loaded.remove(loaded.profiles.single().id);assertTrue(loaded.profiles.isEmpty())
        } finally { dir.toFile().deleteRecursively() }
    }
    @Test fun editorSupportsSelectionPasteUnicodeAndCursorDeletion() {
        val editor=TextInput(8);editor.set("Alex");editor.selectAll();editor.insert("Turtle\n");assertEquals("Turtle",editor.text)
        editor.home();editor.move(1);editor.delete();assertEquals("Trtle",editor.text)
        editor.end();editor.insert("🐢abc");assertEquals("Trtle🐢ab",editor.text)
        editor.backspace();editor.backspace();editor.backspace();assertEquals("Trtle",editor.text)
        editor.set("😀");editor.place(1);assertEquals(0,editor.cursor)
    }
}
