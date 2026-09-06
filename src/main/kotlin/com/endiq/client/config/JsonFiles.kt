package com.endiq.client.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/** Atomic, bounded JSON persistence. Callers never pass authentication secrets. */
object JsonFiles {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    fun read(path: Path): JsonObject {
        if (!Files.isRegularFile(path)) return JsonObject()
        require(Files.size(path) <= 1_048_576) { "Configuration file is too large" }
        return Files.newBufferedReader(path).use { JsonParser.parseReader(it).asJsonObject }
    }
    @Synchronized fun write(path: Path, value: JsonObject) {
        Files.createDirectories(path.parent)
        val temporary = Files.createTempFile(path.parent, ".turtle-", ".json.tmp")
        try {
            Files.writeString(temporary, gson.toJson(value))
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING) }
            catch (_: java.nio.file.AtomicMoveNotSupportedException) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING) }
        } finally { Files.deleteIfExists(temporary) }
    }
}
