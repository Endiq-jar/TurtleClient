package com.endiq.client.gui

import com.google.gson.JsonParser
import java.security.MessageDigest
import javax.imageio.ImageIO
import kotlin.test.*

class UiAssetsTest {
    @Test fun shippedArtworkIsValidTransparentWhereNeededAndWithinBudget() {
        val root = "assets/turtle-client/"
        val loader = javaClass.classLoader
        val manifest = loader.getResourceAsStream(root + "ui-assets.json") ?: fail("Missing UI asset manifest")
        val assets = manifest.bufferedReader().use { JsonParser.parseReader(it).asJsonObject["assets"].asJsonArray }
        val names = mutableSetOf<String>()
        var total = 0
        for (entry in assets) {
            val spec = entry.asJsonObject
            val name = spec["path"].asString
            assertTrue(spec["source"].asString.startsWith("artwork/"), "Missing editable source: $name")
            assertTrue(spec["sourceSha256"].asString.matches(Regex("[a-f0-9]{64}")), name)
            assertTrue(names.add(name), "Duplicate asset $name")
            val bytes = loader.getResourceAsStream(root + name)?.use { it.readBytes() } ?: fail("Missing $name")
            total += bytes.size
            assertEquals(spec["bytes"].asInt, bytes.size, name)
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }
            assertEquals(spec["sha256"].asString, digest, name)
            val image = ImageIO.read(bytes.inputStream()) ?: fail("Invalid PNG: $name")
            assertEquals(spec["width"].asInt, image.width, name)
            assertEquals(spec["height"].asInt, image.height, name)
            assertTrue(image.colorModel.hasAlpha(), "Missing transparency: $name")
            assertEquals(0, image.getRGB(0, 0) ushr 24, "Baked backdrop in $name")
            assertNotNull(loader.getResource(root + name + ".mcmeta"), "Missing filtering metadata: $name")
        }
        assertTrue(total < 250_000, "Vector artwork exceeds the compressed size budget")
        assertTrue(names.containsAll(listOf("icon.png", "textures/gui/branding/turtle.png", "textures/gui/branding/badge.png",
            "textures/gui/branding/wordmark.png")))
        // Every icon the screens draw: a missing one renders as an empty square.
        for (name in listOf("account", "search", "close", "check", "settings", "refresh", "folder", "delete",
            "grid", "hud", "pvp", "render", "movement", "utility", "hypixel", "performance"))
            assertTrue("textures/gui/icons/$name.png" in names, "Missing $name icon")
        // Capes are downloaded, and menus are flat: neither ships pixels any more.
        assertTrue(names.none { it.startsWith("textures/cosmetics/") }, "Bundled cosmetics are back")
        assertTrue(names.none { it.contains("/backgrounds/") }, "Menu backdrop images are back")
        assertNull(loader.getResource(root + "textures/gui/menu/logo.png"), "Old Lunar logo is still bundled")
    }
}
