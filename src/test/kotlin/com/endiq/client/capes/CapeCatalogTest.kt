package com.endiq.client.capes

import com.endiq.client.accounts.AccountProfile
import com.google.gson.JsonParser
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import java.util.Base64
import java.util.UUID
import kotlin.test.*

/**
 * Cape discovery is offline-testable on purpose: the parsers take text, and the
 * network sits behind [CapeCatalog.Transport], so nothing here opens a socket.
 */
class CapeCatalogTest {
    private val uuid = UUID.fromString("12345678-1234-1234-1234-1234567890ab")

    private class FakeTransport(private val bodies: Map<String, ByteArray>) : CapeCatalog.Transport {
        val calls = mutableListOf<String>()
        override fun get(url: String): ByteArray? {
            calls += url
            return bodies[url]
        }
    }

    /** Mirrors NameMC's cape index: an anchor per cape, a 3D render image and a holder count. */
    private val indexHtml = """
        <div class="capelist">
          <a class="card" href="/cape/8c05ef3c54870d04">
            <div class="name">Turtle</div>
            <img src="https://s.namemc.com/3d/skin/body.png?id=12b92a9206470fe2&amp;cape=8c05ef3c54870d04&amp;theta=210" alt="Turtle">
            <div class="count">3</div>
          </a>
          <a class="card" href="https://namemc.com/cape/8a6cc02cc86e43f1">
            <div class="name">Migrator</div>
            <div class="count">6,078,719</div>
          </a>
          <a class="card" href="/cape/8c05ef3c54870d04"><div class="count">3</div></a>
          <a class="card" href="/cape/ebc798c3f7eca2a3"><img src="/x.png" alt=""></a>
        </div>
    """.trimIndent()

    private fun png(width: Int, height: Int): ByteArray = ByteBuffer.allocate(64).apply {
        put(byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10)); putInt(13); put("IHDR".toByteArray())
        putInt(width); putInt(height)
    }.array()

    private fun profile(vararg textures: String): ByteArray {
        val payload = """{"textures":{${textures.joinToString(",")}}}"""
        val encoded = Base64.getEncoder().encodeToString(payload.toByteArray())
        return """{"id":"${uuid.toString().replace("-", "")}","name":"Turtle",
            "properties":[{"name":"textures","value":"$encoded"}]}""".toByteArray()
    }

    @Test fun nameMcTextureUrlsAreContentAddressedByHash() {
        assertEquals("https://texture.namemc.com/8c/05/8c05ef3c54870d04.png", CapeCatalog.nameMcTexture("8c05ef3c54870d04"))
        assertEquals("https://texture.namemc.com/eb/c7/ebc798c3f7eca2a3.png", CapeCatalog.nameMcTexture("ebc798c3f7eca2a3"))
        assertFailsWith<IllegalArgumentException> { CapeCatalog.nameMcTexture("../secret") }
        assertFailsWith<IllegalArgumentException> { CapeCatalog.nameMcTexture("8C05EF3C54870D04") }
    }

    @Test fun nameMcIndexIsParsedInOrderWithoutDuplicatesAndWithReadableNames() {
        val capes = CapeCatalog.nameMcCapes(indexHtml)
        assertEquals(listOf("8c05ef3c54870d04", "8a6cc02cc86e43f1", "ebc798c3f7eca2a3"), capes.map { it.hash })
        assertEquals("Turtle", capes[0].name, "Holder counts must not become the cape name")
        assertEquals("Migrator", capes[1].name)
        assertEquals("Cape ebc798", capes[2].name, "A missing label falls back to the hash, never blank")
    }

    @Test fun unparsableOrEmptyIndexesYieldNothingInsteadOfFailing() {
        assertTrue(CapeCatalog.nameMcCapes("").isEmpty())
        assertTrue(CapeCatalog.nameMcCapes("<html>no capes here</html>").isEmpty())
        assertTrue(CapeCatalog.nameMcCapes("<a href=\"/cape/NOT-A-HASH-000\">X</a>").isEmpty())
    }

    @Test fun theCatalogueIsBoundedSoOnePageCannotQueueUnlimitedDownloads() {
        val html = (0 until 200).joinToString("") { "<a href=\"/cape/${"%016x".format(it)}\">Cape $it</a>" }
        assertEquals(CapeCatalog.MAX_CATALOGUE, CapeCatalog.nameMcCapes(html).size)
    }

    @Test fun ownCapeUrlsUseTheDashedUuidAndTheCapeComesOutOfTheProfileBlob() {
        assertEquals("https://dl.labymod.net/capes/12345678-1234-1234-1234-1234567890ab", CapeCatalog.labymodCape(uuid))
        assertEquals("https://sessionserver.mojang.com/session/minecraft/profile/$uuid", CapeCatalog.mojangProfile(uuid))
        assertEquals("https://textures.minecraft.net/texture/abc123",
            CapeCatalog.mojangCapeUrl(String(profile("\"CAPE\":{\"url\":\"http://textures.minecraft.net/texture/abc123\"}"))))
        assertNull(CapeCatalog.mojangCapeUrl(String(profile("\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/skin\"}"))),
            "A profile without a cape has no cape")
        assertNull(CapeCatalog.mojangCapeUrl("{}"))
        assertNull(CapeCatalog.mojangCapeUrl("not json at all"))
    }

    @Test fun onlyHttpsTextureUrlsAreAccepted() {
        assertEquals("https://textures.minecraft.net/texture/a", CapeCatalog.secure("http://textures.minecraft.net/texture/a"))
        assertEquals("https://example.com/c.png", CapeCatalog.secure("https://example.com/c.png"))
        assertNull(CapeCatalog.secure("file:///etc/passwd"))
        assertNull(CapeCatalog.secure("::::"))
    }

    @Test fun downloadCachesOwnCapesAndTheCatalogueThenReusesCachedTextures() {
        val dir = Files.createTempDirectory("turtle-capes").toFile()
        val account = AccountProfile(uuid, "Turtle", AccountProfile.Kind.MICROSOFT)
        val first = FakeTransport(mapOf(
            CapeCatalog.mojangProfile(uuid) to profile("\"CAPE\":{\"url\":\"https://textures.minecraft.net/texture/abc123\"}"),
            "https://textures.minecraft.net/texture/abc123" to png(64, 32),
            CapeCatalog.labymodCape(uuid) to png(128, 64),
            CapeCatalog.NAME_MC_INDEX to indexHtml.toByteArray(),
            CapeCatalog.nameMcTexture("8c05ef3c54870d04") to png(64, 32),
            CapeCatalog.nameMcTexture("8a6cc02cc86e43f1") to png(64, 64), // not 2:1, must be skipped
            CapeCatalog.nameMcTexture("ebc798c3f7eca2a3") to png(64, 32)))
        CapeManager.download(first, dir) { account }

        val cached = indexIds(dir)
        assertEquals(setOf("mojang:abc123", "labymod:$uuid", "namemc:8c05ef3c54870d04", "namemc:ebc798c3f7eca2a3"), cached,
            "A cape whose texture is not 2:1 must not be cached")
        assertTrue(File(dir, "namemc/8c05ef3c54870d04.png").isFile)
        assertTrue(File(dir, "mojang/abc123.png").isFile)
        assertTrue(File(dir, "labymod/$uuid.png").isFile)
        assertEquals("Turtle", indexNames(dir)["namemc:8c05ef3c54870d04"])

        // Refresh with a new cape in the index: the cached texture is reused, the new one downloads.
        val secondIndex = """
            <a href="/cape/8c05ef3c54870d04"><div>Turtle</div></a>
            <a href="/cape/62db1a06cba0d59a"><div>Pan</div></a>
        """.trimIndent()
        val second = FakeTransport(mapOf(
            CapeCatalog.NAME_MC_INDEX to secondIndex.toByteArray(),
            CapeCatalog.nameMcTexture("62db1a06cba0d59a") to png(64, 32)))
        CapeManager.download(second, dir) { account }
        assertFalse(second.calls.contains(CapeCatalog.nameMcTexture("8c05ef3c54870d04")),
            "A content-addressed cape already on disk must not be downloaded again")
        val refreshed = indexIds(dir)
        assertEquals(cached + "namemc:62db1a06cba0d59a", refreshed)
        assertEquals("Pan", indexNames(dir)["namemc:62db1a06cba0d59a"])

        // Offline: nothing resolves, and every cached cape still lists.
        CapeManager.download(FakeTransport(emptyMap()), dir) { account }
        assertEquals(refreshed, indexIds(dir), "Cached capes must survive a failed refresh")
    }

    private fun indexIds(dir: File) = entries(dir).map { it["id"].asString }.toSet()
    private fun indexNames(dir: File) = entries(dir).associate { it["id"].asString to it["name"].asString }
    private fun entries(dir: File) =
        JsonParser.parseReader(File(dir, "index.json").reader()).asJsonObject["capes"].asJsonArray.map { it.asJsonObject }
}
