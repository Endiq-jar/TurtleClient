package com.endiq.client.accounts

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean

/** Official Microsoft -> Xbox Live -> XSTS -> Minecraft flow. No third-party token exchange. */
class MicrosoftLogin(
    private val transport: Transport = OfficialTransport(),
    private val now: () -> Long = System::currentTimeMillis,
    private val wait: (Long) -> Unit = Thread::sleep
) {
    data class Reply(val status: Int, val json: JsonObject)
    fun interface Transport { fun request(method: String, uri: URI, body: String?, contentType: String?, bearer: String?): Reply }
    class DeviceChallenge(val userCode: String, private val code: String, val verificationUri: URI,
                          val expiresAt: Long, val interval: Long) {
        internal fun deviceCode() = code
        override fun toString() = "Microsoft device challenge (redacted)"
    }
    class Cancellation {
        private val stopped=AtomicBoolean(false)
        fun cancel() { stopped.set(true) }
        fun check() { if(stopped.get() || Thread.currentThread().isInterrupted) throw CancellationException() }
    }
    companion object {
        private val BASE="https://login.microsoftonline.com/consumers/oauth2/v2.0/"
        private val HOSTS=setOf("login.microsoftonline.com","user.auth.xboxlive.com","xsts.auth.xboxlive.com","api.minecraftservices.com")
        fun validClientId(id:String)=runCatching { UUID.fromString(id).toString().equals(id,true) }.getOrDefault(false)
        fun safeBrowserUri(uri:URI) = uri.scheme=="https" && uri.userInfo==null && uri.port in listOf(-1,443) &&
            uri.host?.lowercase() in setOf("microsoft.com","www.microsoft.com","login.microsoftonline.com")
        private fun form(vararg entries:Pair<String,String>)=entries.joinToString("&") {
            URLEncoder.encode(it.first,StandardCharsets.UTF_8)+"="+URLEncoder.encode(it.second,StandardCharsets.UTF_8)
        }
    }
    fun begin(clientId: String): DeviceChallenge {
        if(!validClientId(clientId)) throw AccountProblem("Enter your registered Microsoft public-client application ID first.")
        val result=transport.request("POST",URI(BASE+"devicecode"),form("client_id" to clientId,"scope" to "XboxLive.signin"),"application/x-www-form-urlencoded",null)
        if(result.status!=200) throw AccountProblem("Microsoft could not start sign-in. Check the application's public-client/Xbox permissions.")
        val json=result.json;val browser=URI(json["verification_uri"].asString)
        if(!safeBrowserUri(browser)) throw AccountProblem("Microsoft returned an unexpected verification address.")
        return DeviceChallenge(json["user_code"].asString,json["device_code"].asString,browser,
            now()+json["expires_in"].asLong.coerceIn(1,1800)*1000,(json.get("interval")?.asLong ?: 5).coerceIn(1,30)*1000)
    }
    fun complete(clientId:String, challenge:DeviceChallenge, cancel:Cancellation):AccountCredentials {
        var interval=challenge.interval
        while(now()<challenge.expiresAt) {
            cancel.check();wait(interval);cancel.check()
            if(now()>=challenge.expiresAt) break
            val result=transport.request("POST",URI(BASE+"token"),form("client_id" to clientId,
                "grant_type" to "urn:ietf:params:oauth:grant-type:device_code","device_code" to challenge.deviceCode()),"application/x-www-form-urlencoded",null)
            if(result.status==200) {
                cancel.check()
                return exchange(result.json["access_token"].asString,clientId,cancel)
            }
            when(result.json.get("error")?.asString) {
                "authorization_pending" -> Unit
                "slow_down" -> interval=(interval+5000).coerceAtMost(60000)
                "authorization_declined", "access_denied" -> throw AccountProblem("Sign-in was declined. No account was changed.")
                "expired_token" -> break
                else -> throw AccountProblem("Microsoft sign-in failed. Start a new sign-in and try again.")
            }
        }
        throw AccountProblem("The sign-in code expired. Start again for a new code.")
    }
    private fun post(url:String,json:JsonObject)=transport.request("POST",URI(url),json.toString(),"application/json",null)
    private fun exchange(token:String,clientId:String,cancel:Cancellation):AccountCredentials {
        val xbl=post("https://user.auth.xboxlive.com/user/authenticate",JsonObject().apply {
            add("Properties",JsonObject().apply { addProperty("AuthMethod","RPS");addProperty("SiteName","user.auth.xboxlive.com");addProperty("RpsTicket","d=$token") })
            addProperty("RelyingParty","http://auth.xboxlive.com");addProperty("TokenType","JWT")
        })
        if(xbl.status!=200) throw AccountProblem("Xbox Live sign-in failed. Check that the Microsoft account has an Xbox profile.")
        cancel.check()
        val xsts=post("https://xsts.auth.xboxlive.com/xsts/authorize",JsonObject().apply {
            add("Properties",JsonObject().apply { addProperty("SandboxId","RETAIL");add("UserTokens",JsonArray().apply { add(xbl.json["Token"].asString) }) })
            addProperty("RelyingParty","rp://api.minecraftservices.com/");addProperty("TokenType","JWT")
        })
        if(xsts.status!=200) {
            val message=when(xsts.json.get("XErr")?.asLong) {
                2148916233L -> "Create an Xbox profile for this account before signing in."
                2148916238L -> "This account requires Microsoft family/parental approval. Manage it through Microsoft."
                else -> "Xbox could not authorize Minecraft. Check account and family permissions."
            }
            throw AccountProblem(message)
        }
        cancel.check()
        val claim=xsts.json["DisplayClaims"].asJsonObject["xui"].asJsonArray[0].asJsonObject
        val minecraft=post("https://api.minecraftservices.com/authentication/login_with_xbox",JsonObject().apply {
            addProperty("identityToken","XBL3.0 x=${claim["uhs"].asString};${xsts.json["Token"].asString}")
        })
        if(minecraft.status!=200) throw AccountProblem("Minecraft authorization failed. The app may require Minecraft API approval.")
        val access=minecraft.json["access_token"].asString
        cancel.check()
        val ownership=transport.request("GET",URI("https://api.minecraftservices.com/entitlements/mcstore"),null,null,access)
        if(ownership.status!=200 || ownership.json.getAsJsonArray("items")?.any {
            it.asJsonObject.get("name")?.asString in setOf("game_minecraft","product_minecraft")
        }!=true)throw AccountProblem("Minecraft Java ownership could not be verified. Check the license, Game Pass subscription and app approval.")
        cancel.check()
        val profile=transport.request("GET",URI("https://api.minecraftservices.com/minecraft/profile"),null,null,access)
        if(profile.status==404) throw AccountProblem("No licensed Minecraft Java profile was found for this account.")
        if(profile.status!=200) throw AccountProblem("Minecraft could not verify this account. It has not been added.")
        val raw=profile.json["id"].asString
        require(raw.matches(Regex("[a-fA-F0-9]{32}"))) { "Invalid Minecraft profile ID" }
        val id=UUID.fromString(raw.replace(Regex("(.{8})(.{4})(.{4})(.{4})(.{12})"),"$1-$2-$3-$4-$5"))
        val name=profile.json["name"].asString
        require(name.matches(Regex("[A-Za-z0-9_]{3,16}")))
        cancel.check()
        return AccountCredentials(AccountProfile(id,name,AccountProfile.Kind.MICROSOFT),access,
            now()+minecraft.json["expires_in"].asLong.coerceIn(1,86400)*1000,claim.get("xid")?.asString,clientId)
    }
    class OfficialTransport : Transport {
        private val client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NEVER).build()
        override fun request(method:String,uri:URI,body:String?,contentType:String?,bearer:String?):Reply {
            require(uri.scheme=="https" && uri.host in HOSTS && uri.userInfo==null && uri.port in listOf(-1,443))
            val request=HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(25)).header("Accept","application/json")
            contentType?.let { request.header("Content-Type",it) };bearer?.let { request.header("Authorization","Bearer $it") }
            request.method(method,body?.let { HttpRequest.BodyPublishers.ofString(it) } ?: HttpRequest.BodyPublishers.noBody())
            val response=client.send(request.build(),HttpResponse.BodyHandlers.ofInputStream())
            val bytes=response.body().use { it.readNBytes(262145) }
            if(bytes.size>262144) throw AccountProblem("The sign-in service returned an oversized response.")
            val json=runCatching { JsonParser.parseString(String(bytes,StandardCharsets.UTF_8)).asJsonObject }.getOrElse { JsonObject() }
            return Reply(response.statusCode(),json)
        }
    }
}
