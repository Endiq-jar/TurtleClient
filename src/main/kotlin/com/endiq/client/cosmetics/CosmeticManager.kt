package com.endiq.client.cosmetics

import com.endiq.client.compat.*
import java.io.File

// Registry + equip-state for custom cosmetics.
//
// Cosmetics are PNG files dropped by the user into:
//   <run_dir>/custom_cosmetics/<folderName>/  (any PNG in that folder)
//
// This manager only tracks the registry and equip state. Actual rendering
// (cape/hat/wings meshes, texture upload) is a separate concern per
// COSMETICS_README.txt and is not implemented here.
object CosmeticManager {

    enum class CosmeticType(val displayName: String, val icon: String, val folderName: String) {
        CAPE("Cape", "🧣", "capes"),
        HAT("Hat", "🎩", "hats"),
        WINGS("Wings", "🪽", "wings"),
        MASK("Mask", "🎭", "masks"),
        SUIT("Suit", "👔", "suits"),
        PET("Pet", "🐾", "pets")
    }

    data class CosmeticEntry(
        val type: CosmeticType,
        val name: String,
        val file: File
    )

    private val registry = linkedMapOf<CosmeticType, MutableList<CosmeticEntry>>()
    val equipped = linkedMapOf<CosmeticType, CosmeticEntry?>()

    init {
        for (type in CosmeticType.values()) {
            registry[type] = mutableListOf()
            equipped[type] = null
        }
    }

    private fun baseDir(): File =
        File(MinecraftClient.getInstance().runDirectory, "custom_cosmetics")

    /** Rescans disk for cosmetic PNGs, preserving current equip state where the file still exists. */
    fun reload() {
        val base = baseDir()
        for (type in CosmeticType.values()) {
            val folder = File(base, type.folderName)
            if (!folder.exists()) folder.mkdirs()

            val found = folder.listFiles { f -> f.isFile && f.extension.equals("png", ignoreCase = true) }
                ?.sortedBy { it.name }
                ?.map { CosmeticEntry(type, it.nameWithoutExtension, it) }
                ?: emptyList()

            registry[type] = found.toMutableList()

            val current = equipped[type]
            if (current != null && found.none { it.file == current.file }) {
                equipped[type] = null
            }
        }
    }

    fun getByType(type: CosmeticType): List<CosmeticEntry> = registry[type] ?: emptyList()

    fun getEquipped(type: CosmeticType): CosmeticEntry? = equipped[type]

    fun isEquipped(entry: CosmeticEntry): Boolean = equipped[entry.type]?.file == entry.file

    fun equip(entry: CosmeticEntry) {
        equipped[entry.type] = entry
    }

    fun unequip(type: CosmeticType) {
        equipped[type] = null
    }
}
