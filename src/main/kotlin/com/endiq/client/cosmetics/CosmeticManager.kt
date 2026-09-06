package com.endiq.client.cosmetics

import com.endiq.TurtleClient
import com.endiq.client.compat.*
import com.endiq.client.config.JsonFiles
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest

object CosmeticManager {
    enum class CosmeticType(val displayName: String, val folderName: String) {
        CAPE("Cape", "capes"), HAT("Hat", "hats"), WINGS("Wings", "wings"),
        MASK("Mask", "masks"), SUIT("Suit", "suits"), PET("Pet", "pets")
    }
    data class CosmeticEntry(val id: String, val type: CosmeticType, val name: String,
                             val texture: Identifier, val preview: Identifier, val file: File? = null)
    data class Loadout(val items: List<CosmeticEntry>, val age: Float = 0f) {
        fun has(type: CosmeticType) = items.any { it.type == type }
        companion object { @JvmField val EMPTY = Loadout(emptyList()) }
    }
    private val registry = linkedMapOf<CosmeticType, List<CosmeticEntry>>()
    private val equipped = linkedMapOf<CosmeticType, CosmeticEntry>()
    private val wanted = linkedMapOf<CosmeticType, String>()
    private val loadedTextures = linkedMapOf<String, Identifier>()
    @Volatile private var current = Loadout.EMPTY
    var lastMessage = "18 included cosmetics. Press F5 to see your outfit."
        private set
    private var initialized = false
    private val config get() = FabricLoader.getInstance().configDir.resolve("turtle-client/cosmetics.json")
    fun baseDir() = File(MinecraftClient.getInstance().runDirectory, "custom_cosmetics")
    fun initialize() {
        if (initialized) return
        initialized = true
        runCatching {
            val json = JsonFiles.read(config)
            for (type in CosmeticType.values()) json.get(type.name)?.asString?.let { wanted[type] = it }
        }.onFailure { TurtleClient.LOGGER.warn("Could not read cosmetic selections; keeping defaults") }
        reload()
    }
    private fun builtins(): List<CosmeticEntry> {
        val stream = javaClass.classLoader.getResourceAsStream("assets/turtle-client/cosmetics.json") ?: return emptyList()
        return stream.bufferedReader().use { JsonParser.parseReader(it).asJsonObject["items"].asJsonArray }.map {
            val item = it.asJsonObject
            CosmeticEntry(item["id"].asString, CosmeticType.valueOf(item["type"].asString), item["name"].asString,
                identifier("turtle-client", item["texture"].asString), identifier("turtle-client", item["preview"].asString))
        }
    }
    /** Bounded PNGs only; no metadata downloads, file decoding, or disk reads in render loops. */
    fun reload() {
        val bundled = builtins()
        val used = mutableSetOf<String>()
        var skipped = 0
        for (type in CosmeticType.values()) {
            val folder = File(baseDir(), type.folderName)
            folder.mkdirs()
            val custom = folder.listFiles()?.filter { it.isFile && it.extension.equals("png", true) }?.sortedBy { it.name }?.take(256).orEmpty().mapNotNull { file ->
                runCatching {
                    require(file.canonicalFile.toPath().startsWith(folder.canonicalFile.toPath()))
                    require(file.length() in 24..2_097_152)
                    val bytes = file.readBytes()
                    validatePng(bytes, type == CosmeticType.CAPE)
                    val key = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }
                    used += key
                    val texture = loadedTextures.getOrPut(key) {
                        identifier("turtle-client", "custom/$key").also { CosmeticTextures.upload(it, bytes) }
                    }
                    CosmeticEntry("custom:${type.name}:${file.name}",type,file.nameWithoutExtension,texture,texture,file)
                }.getOrElse { skipped++; null }
            }
            val entries = bundled.filter { it.type == type } + custom
            registry[type] = entries
            val selected = wanted[type] ?: equipped[type]?.id
            equipped.remove(type)
            entries.firstOrNull { it.id == selected }?.let { equipped[type] = it }
        }
        for (key in loadedTextures.keys.toList()) if (key !in used) loadedTextures.remove(key)?.let { CosmeticTextures.release(it) }
        rebuild()
        lastMessage = if (skipped > 0) "$skipped invalid PNG(s) skipped. Maximum 512px; capes must be 2:1."
            else "${registry.values.sumOf { it.size }} cosmetics ready. Client-side; use F5 to see them."
    }
    fun getByType(type: CosmeticType) = registry[type].orEmpty()
    fun getEquipped(type: CosmeticType) = equipped[type]
    fun isEquipped(entry: CosmeticEntry) = equipped[entry.type]?.id == entry.id
    fun equip(entry: CosmeticEntry) { equipped[entry.type]=entry; wanted[entry.type]=entry.id; changed() }
    fun unequip(type: CosmeticType) { equipped.remove(type); wanted.remove(type); changed() }
    fun unequipAll() { equipped.clear(); wanted.clear(); changed() }
    private fun rebuild() { current=Loadout(equipped.values.toList()) }
    private fun changed() {
        rebuild()
        lastMessage = if (equipped.isEmpty()) "All cosmetics removed." else "Outfit saved. Press F5 in game to see it."
        val json = JsonObject();wanted.forEach { (type,id) -> json.addProperty(type.name,id) }
        runCatching { JsonFiles.write(config,json) }.onFailure { lastMessage="Equipped, but could not save the outfit to disk." }
    }
    @JvmStatic fun capture(entity: Entity, partialTick: Float): Loadout {
        val player = MinecraftClient.getInstance().player ?: return Loadout.EMPTY
        if (entity !== player || player.isInvisible || player.isSpectator) return Loadout.EMPTY
        // Suppress a cape/wing collision with elytra, and don't float a pet beside a bed.
        val items=current.items.filterNot {
            (player.isFallFlying && (it.type==CosmeticType.CAPE || it.type==CosmeticType.WINGS)) ||
                (player.isSleeping && it.type==CosmeticType.PET)
        }
//? if >=26.1 {
/*        return Loadout(items,player.tickCount+partialTick)
*///?} else {
        return Loadout(items,player.age+partialTick)
//?}
    }
    fun validatePng(bytes: ByteArray, cape: Boolean) {
        require(bytes.size >= 24 && bytes.take(8).toByteArray().contentEquals(byteArrayOf(-119,80,78,71,13,10,26,10))) { "Not a PNG" }
        val buffer=ByteBuffer.wrap(bytes,16,8)
        val w=buffer.int;val h=buffer.int
        require(w in 16..512 && h in 16..512) { "PNG dimensions must be 16–512 pixels" }
        require(!cape || w==h*2) { "Cape PNGs use the standard 2:1 Minecraft layout" }
    }
}
