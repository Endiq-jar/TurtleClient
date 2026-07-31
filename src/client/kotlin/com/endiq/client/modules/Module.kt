package com.endiq.client.modules

import org.lwjgl.glfw.GLFW

abstract class Module(
    val name: String,
    val description: String,
    val category: Category,
    var key: Int = GLFW.GLFW_KEY_UNKNOWN
) {
    var enabled: Boolean = false
        private set
    var keyWasDown: Boolean = false
    var favorited: Boolean = false
    var isNew:     Boolean = false

    val settings = mutableListOf<Setting>()

    // Helpers to register settings
    protected fun bool(name: String, desc: String = "", default: Boolean) =
        BoolSetting(name, desc, default).also { settings.add(it) }
    protected fun slider(name: String, desc: String = "", default: Float, min: Float, max: Float, suffix: String = "") =
        SliderSetting(name, desc, default, min, max, suffix).also { settings.add(it) }
    protected fun color(name: String, desc: String = "", r: Int = 255, g: Int = 255, b: Int = 255, a: Int = 255) =
        ColorSetting(name, desc, r, g, b, a).also { settings.add(it) }
    protected fun dropdown(name: String, desc: String = "", vararg options: String, default: Int = 0) =
        DropdownSetting(name, desc, options.toList(), default).also { settings.add(it) }

    fun toggle() { enabled = !enabled; if (enabled) onEnable() else onDisable() }
    fun enable()  { if (!enabled) toggle() }
    fun disable() { if (enabled)  toggle() }

    open fun onEnable()  {}
    open fun onDisable() {}

    enum class Category(val displayName: String) {
        ALL("All"), HUD("HUD"), HYPIXEL("Hypixel"),
        PVP("PvP"), RENDER("Render"), MOVEMENT("Movement"), UTILITY("Utility")
    }
}
