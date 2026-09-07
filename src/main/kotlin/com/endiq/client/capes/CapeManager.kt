package com.endiq.client.capes

import com.endiq.TurtleClient
import com.endiq.client.accounts.AccountProfile
import com.endiq.client.accounts.Accounts
import com.endiq.client.compat.*
import com.endiq.client.config.JsonFiles
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest

/** Where a cape texture came from; also the filter used by the cape list. */
enum class CapeSource(val displayName: String) {
    MOJANG("Mojang"), LABYMOD("LabyMod"), NAMEMC("NameMC"), LOCAL("Local")
}

/**
 * Capes only, and only ones that exist: your Mojang profile cape, your LabyMod
 * cape, and NameMC's public cape index. Textures are downloaded to the instance
 * directory, so the list still works offline after the first download.
 *
 * Threading: network work runs on a daemon thread and only writes files. Texture
 * uploads happen in [loadFromDisk], which is always called on the client thread.
 */
object CapeManager {
    data class Cape(val id: String, val name: String, val source: CapeSource,
                    val texture: Identifier, val width: Int, val height: Int) {
        /** The 10x16 back panel of the standard cape layout, scaled to this texture. */
        val previewWidth: Int get() = (width * 10 / 64).coerceAtLeast(1)
        val previewHeight: Int get() = (height * 16 / 32).coerceAtLeast(1)
    }

    /** Snapshot carried by render state; deferred GPU callbacks never read live player state. */
    data class Outfit(val cape: Cape?, val age: Float = 0f) {
        companion object { @JvmField val EMPTY = Outfit(null) }
    }

    private data class Record(val id: String, val name: String, val source: CapeSource, val file: File)
    data class Size(val width: Int, val height: Int)

    private const val MAX_PNG = 2 * 1024 * 1024
    private const val MAX_LOCAL = 32
    private val PNG_MAGIC = byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10)

    private val textures = linkedMapOf<String, Identifier>()
    @Volatile private var capes: List<Cape> = emptyList()
    @Volatile private var outfit = Outfit.EMPTY
    @Volatile private var pendingSync = false
    @Volatile private var summary: String? = null
    @Volatile var busy = false
        private set
    @Volatile var status = "Download capes to list your own plus NameMC's index."
        private set
    private var equippedId: String? = null
    private var initialized = false
    private val config get() = FabricLoader.getInstance().configDir.resolve("turtle-client/capes.json")

    fun cacheDir() = File(MinecraftClient.getInstance().runDirectory, "turtle-client/capes")
    fun localDir() = File(cacheDir(), "local")

    /** Client thread. Reads the cached index, uploads textures and restores the saved choice. */
    fun initialize() {
        if (initialized) return
        initialized = true
        runCatching { JsonFiles.read(config).get("equipped")?.asString?.let { equippedId = it } }
            .onFailure { TurtleClient.LOGGER.warn("Could not read the saved cape; starting with none") }
        loadFromDisk()
    }

    /** Client thread: (re)reads the cache and uploads any texture that changed. */
    fun loadFromDisk() {
        pendingSync = false
        val loaded = mutableListOf<Cape>()
        val used = mutableSetOf<String>()
        var skipped = 0
        for (record in readIndex() + scanLocal()) {
            val bytes = runCatching {
                require(record.file.isFile && record.file.length() in 24..MAX_PNG)
                record.file.readBytes()
            }.getOrNull()
            val size = bytes?.let { runCatching { validatePng(it) }.getOrNull() }
            if (bytes == null || size == null) { skipped++; continue }
            val digest = sha(bytes)
            used += digest
            val texture = textures.getOrPut(digest) {
                identifier("turtle-client", "capes/$digest").also { CapeTextures.upload(it, bytes) }
            }
            loaded += Cape(record.id, record.name, record.source, texture, size.width, size.height)
        }
        for (digest in textures.keys.toList()) if (digest !in used) textures.remove(digest)?.let { CapeTextures.release(it) }
        capes = loaded
        val equipped = loaded.firstOrNull { it.id == equippedId }
        equippedId = equipped?.id
        outfit = Outfit(equipped)
        if (!busy) {
            status = summary ?: when {
                loaded.isEmpty() -> "No capes yet. Use Download to fetch them from the internet."
                else -> "${loaded.size} capes ready${if (skipped > 0) " · $skipped invalid file(s) skipped" else ""}."
            }
            summary = null
        }
    }

    /** Cheap client-thread hook so a finished download applies without reopening a screen. */
    fun syncIfPending() { if (pendingSync) loadFromDisk() }

    fun bySource(source: CapeSource?): List<Cape> = if (source == null) capes else capes.filter { it.source == source }
    fun isEquipped(cape: Cape) = equippedId == cape.id

    fun equip(cape: Cape) {
        equippedId = cape.id; outfit = Outfit(cape)
        status = "${cape.name} equipped. Press F5 in a world to see it."
        save()
    }

    fun unequip() {
        equippedId = null; outfit = Outfit.EMPTY
        status = "No cape equipped."
        save()
    }

    /** Starts a bounded background download; repeated calls while busy are ignored. */
    fun downloadAsync() {
        if (busy) return
        busy = true
        status = "Contacting Mojang, LabyMod and NameMC..."
        Thread({
            try { download(CapeCatalog.Http) }
            catch (problem: Exception) { summary = "Cape download failed: ${problem.message ?: "no connection"}" }
            finally { busy = false; pendingSync = true }
        }, "Turtle cape download").apply { isDaemon = true }.start()
    }

    private fun currentProfile(): AccountProfile? =
        Accounts.active ?: runCatching { SessionBridge.launcher().profile }.getOrNull()

    /** Network thread: fills the cache directory and rewrites its index. */
    internal fun download(transport: CapeCatalog.Transport, dir: File = cacheDir(), profile: () -> AccountProfile? = ::currentProfile) {
        val records = linkedMapOf<String, Record>()
        readIndex(dir).forEach { records[it.id] = it }
        val mine = mutableListOf<String>()
        val account = profile()

        if (account != null && account.kind != AccountProfile.Kind.OFFLINE) {
            val url = transport.text(CapeCatalog.mojangProfile(account.id))?.let { CapeCatalog.mojangCapeUrl(it) }
            if (url != null) store(records, transport, dir, "mojang:${url.substringAfterLast('/')}", "My Mojang Cape",
                CapeSource.MOJANG, url)?.let { mine += it.name }
        }
        if (account != null) {
            store(records, transport, dir, "labymod:${account.id}", "My LabyMod Cape", CapeSource.LABYMOD,
                CapeCatalog.labymodCape(account.id))?.let { mine += it.name }
        }

        val index = transport.text(CapeCatalog.NAME_MC_INDEX)
        val catalogue = index?.let { CapeCatalog.nameMcCapes(it) }.orEmpty()
        for (reference in catalogue) {
            store(records, transport, dir, "namemc:${reference.hash}", reference.name, CapeSource.NAMEMC,
                CapeCatalog.nameMcTexture(reference.hash))
        }
        writeIndex(records.values, dir)

        val count = records.size
        summary = when {
            index == null && count > 0 -> "No connection. Showing $count cached capes."
            index == null && count == 0 -> "NameMC and your capes could not be reached."
            else -> "$count capes ready${if (mine.isEmpty()) "" else " · ${mine.size} yours"}."
        }
    }

    /** NameMC textures are content-addressed, so a cached copy is reused instead of refetched. */
    private fun store(records: MutableMap<String, Record>, transport: CapeCatalog.Transport, dir: File,
                      id: String, name: String, source: CapeSource, url: String): Record? {
        val existing = records[id]
        if (existing != null && source == CapeSource.NAMEMC && existing.file.isFile) return existing
        val relative = "${source.name.lowercase()}/${id.substringAfter(':').take(64).replace(Regex("[^A-Za-z0-9._-]"), "_")}.png"
        val target = File(dir, relative)
        val bytes = transport.get(url) ?: return existing
        runCatching { validatePng(bytes) }.getOrElse { return existing }
        runCatching {
            target.parentFile.mkdirs()
            target.writeBytes(bytes)
        }.onFailure { return existing }
        val record = Record(id, name, source, target)
        records[id] = record
        return record
    }

    private fun readIndex(dir: File = cacheDir()): List<Record> = runCatching {
        val file = File(dir, "index.json")
        if (!file.isFile) return@runCatching emptyList()
        JsonParser.parseReader(file.reader()).asJsonObject["capes"]?.asJsonArray?.mapNotNull { element ->
            runCatching {
                val item = element.asJsonObject
                val target = File(dir, item["file"].asString)
                require(target.canonicalFile.toPath().startsWith(dir.canonicalFile.toPath()))
                Record(item["id"].asString, item["name"].asString, CapeSource.valueOf(item["source"].asString), target)
            }.getOrNull()
        }.orEmpty()
    }.getOrDefault(emptyList())

    private fun scanLocal(): List<Record> {
        val folder = localDir()
        folder.mkdirs()
        return runCatching {
            folder.listFiles()?.filter { it.isFile && it.extension.equals("png", true) }?.sortedBy { it.name }
                ?.take(MAX_LOCAL)?.map { Record("local:${it.name}", it.nameWithoutExtension, CapeSource.LOCAL, it) }
                .orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun writeIndex(records: Collection<Record>, dir: File) {
        val array = JsonArray()
        for (record in records) {
            val item = JsonObject()
            item.addProperty("id", record.id)
            item.addProperty("name", record.name)
            item.addProperty("source", record.source.name)
            item.addProperty("file", record.file.toRelativeString(dir).replace(File.separatorChar, '/'))
            array.add(item)
        }
        val json = JsonObject()
        json.addProperty("schema", 1)
        json.add("capes", array)
        runCatching { JsonFiles.write(File(dir, "index.json").toPath(), json) }
            .onFailure { status = "Capes downloaded, but the index could not be saved." }
    }

    private fun save() {
        val json = JsonObject()
        equippedId?.let { json.addProperty("equipped", it) }
        runCatching { JsonFiles.write(config, json) }
            .onFailure { status = "Equipped, but the choice could not be saved to disk." }
    }

    /** Header-only validation: no decoding, and capes must use the 2:1 Minecraft layout. */
    fun validatePng(bytes: ByteArray): Size {
        require(bytes.size in 24..MAX_PNG && bytes.copyOfRange(0, 8).contentEquals(PNG_MAGIC)) { "Not a PNG" }
        require(ByteBuffer.wrap(bytes, 8, 4).int == 13 &&
            bytes.copyOfRange(12, 16).contentEquals("IHDR".toByteArray())) { "Missing PNG header" }
        val header = ByteBuffer.wrap(bytes, 16, 8)
        val width = header.int
        val height = header.int
        require(width in 16..1024 && height in 8..512) { "Cape PNGs are 16-1024 pixels wide and half as tall" }
        require(width == height * 2) { "Cape PNGs use the standard 2:1 Minecraft cape layout" }
        return Size(width, height)
    }

    private fun sha(bytes: ByteArray) =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }

    /** Client thread: your own cape, on your own player, only while it can be seen. */
    @JvmStatic
    fun capture(entity: Entity, partialTick: Float): Outfit {
        val cape = outfit.cape ?: return Outfit.EMPTY
        val player = MinecraftClient.getInstance().player ?: return Outfit.EMPTY
        if (entity !== player || player.isInvisible || player.isSpectator) return Outfit.EMPTY
        // A cape must not clip through elytra.
//? if >=1.21.2 && <26.1 {
        if (player.isGliding) return Outfit.EMPTY
//?} else {
/*        if (player.isFallFlying) return Outfit.EMPTY
*///?}
//? if >=26.1 {
/*        return Outfit(cape, player.tickCount + partialTick)
*///?} else {
        return Outfit(cape, player.age + partialTick)
//?}
    }
}
