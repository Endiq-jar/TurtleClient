package com.endiq.client.gui.settings

import com.endiq.client.compat.*
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import com.endiq.client.modules.*
import org.lwjgl.glfw.GLFW
import java.util.Locale

class ModSettingsGui(private val mod: Module, private val parent: Screen) : ClientScreen("${mod.name} Settings") {
    private val scroll = ScrollState()
    private var panel = UiRect(0, 0, 1, 1)
    private var viewport = panel
    private var scrollbar = panel
    private var closeButton = panel
    private var enableButton = panel
    private var favoriteButton = panel
    private var keyButton = panel
    private var clearButton = panel
    private var waitingForKey = false
    private var draggingSlider: SliderSetting? = null
    private var sliderTrack = panel
    private val palette = listOf(0x86E8BC, 0xFFFFFF, 0x7FC9FF, 0xB7A0EF, 0xF2AC9B, 0xF3D38A)

    private fun rowHeight(setting: Setting) = when (setting) {
        is SliderSetting -> 42
        is BoolSetting -> 30
        is DropdownSetting -> 42
        is ColorSetting -> 38
    }

    private fun updateBounds() = scroll.update(mod.settings.sumOf { rowHeight(it) } + 16, viewport.height)

    private fun rows(): List<Pair<Setting, UiRect>> {
        var y = viewport.y + 8 - scroll.pixels
        return mod.settings.map { setting ->
            (setting to UiRect(viewport.x + 10, y, (viewport.width - 20).coerceAtLeast(1), rowHeight(setting))).also { y += it.second.height }
        }
    }

    override fun init() {
        scroll.endDrag()
        panel = centeredPanel(width, height, 380, 386)
        closeButton = UiRect(panel.right - 30, panel.y + 9, 22, 22)
        enableButton = UiRect(panel.x + 10, panel.y + 43, 100, 23)
        favoriteButton = UiRect(panel.right - 106, panel.y + 43, 96, 23)
        viewport = UiRect(panel.x + 1, panel.y + 88, panel.width - 14, (panel.height - 140).coerceAtLeast(1))
        scrollbar = UiRect(panel.right - 10, viewport.y + 4, 6, (viewport.height - 8).coerceAtLeast(1))
        keyButton = UiRect(panel.x + 60, panel.bottom - 40, panel.width - 130, 23)
        clearButton = UiRect(panel.right - 62, keyButton.y, 52, 23)
        draggingSlider = null
        updateBounds()
    }

    override fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        ctx.fill(0, 0, width, height, 0xB8071013.toInt())
        Theme.panel(ctx, panel)
        ctx.drawTexture(Theme.LOGO, panel.x + 10, panel.y + 9, 25, 25)
        Theme.label(ctx, textRenderer, mod.name, panel.x + 43, panel.y + 12, Theme.TEXT, panel.width - 84)
        Theme.label(ctx, textRenderer, "MODULE SETTINGS", panel.x + 43, panel.y + 25, Theme.SUBTLE, panel.width - 84)
        if (closeButton.contains(mx.toDouble(), my.toDouble())) Theme.rounded(ctx, closeButton, Theme.HOVER)
        ctx.drawTexture(Theme.icon("close"), closeButton.x + 4, closeButton.y + 4, 14, 14, Theme.MUTED)
        Theme.button(ctx, textRenderer, enableButton, if (mod.enabled) "Enabled" else "Disabled", enableButton.contains(mx.toDouble(), my.toDouble()), mod.enabled)
        Theme.button(ctx, textRenderer, favoriteButton, if (mod.favorited) "Favorited" else "Favorite", favoriteButton.contains(mx.toDouble(), my.toDouble()))
        Theme.label(ctx, textRenderer, mod.description, panel.x + 10, panel.y + 74, Theme.MUTED, panel.width - 20)
        ctx.fill(panel.x + 10, viewport.y, panel.right - 10, viewport.y + 1, Theme.BORDER)
        updateBounds()
        ctx.enableScissor(viewport.x, viewport.y, viewport.right, viewport.bottom)
        try {
            for ((setting, rect) in rows()) {
                if (rect.bottom <= viewport.y || rect.y >= viewport.bottom) continue
                drawSetting(ctx, setting, rect)
            }
            if (mod.settings.isEmpty()) Theme.label(ctx, textRenderer, "No extra settings for this module.", viewport.x + 10, viewport.y + 18, Theme.MUTED, viewport.width - 20)
        } finally { ctx.disableScissor() }
        Theme.scrollbar(ctx, scroll, scrollbar, mx, my)
        ctx.fill(panel.x + 10, panel.bottom - 52, panel.right - 10, panel.bottom - 51, Theme.BORDER)
        Theme.label(ctx, textRenderer, "Keybind", panel.x + 10, keyButton.y + 8, Theme.MUTED)
        val keyLabel = if (waitingForKey) "Press a key..." else keyName(mod.key)
        Theme.button(ctx, textRenderer, keyButton, keyLabel, keyButton.contains(mx.toDouble(), my.toDouble()), waitingForKey)
        Theme.button(ctx, textRenderer, clearButton, "Clear", clearButton.contains(mx.toDouble(), my.toDouble()))
        Theme.label(ctx, textRenderer, if (waitingForKey) "Escape: cancel  /  Delete: clear" else "Wheel / drag to scroll  /  Escape: back",
            panel.x + 10, panel.bottom - 12, Theme.SUBTLE, panel.width - 20)
        super.renderGui(ctx, mx, my, delta)
    }

    private fun drawSetting(ctx: GuiContext, setting: Setting, rect: UiRect) {
        when (setting) {
            is SliderSetting -> {
                val value = String.format(Locale.ROOT, if (setting.max >= 100f && setting.suffix.isEmpty()) "%.0f" else "%.2f", setting.value) + setting.suffix
                Theme.label(ctx, textRenderer, setting.name, rect.x, rect.y + 3, Theme.TEXT, rect.width - textRenderer.getWidth(value) - 10)
                Theme.label(ctx, textRenderer, value, rect.right - textRenderer.getWidth(value), rect.y + 3, Theme.ACCENT)
                val track = UiRect(rect.x, rect.y + 21, rect.width, 5)
                val fraction = if (setting.max > setting.min) ((setting.value - setting.min) / (setting.max - setting.min)).coerceIn(0f, 1f) else 0f
                val filled = (fraction * track.width).toInt()
                Theme.rounded(ctx, track, Theme.CARD, 2)
                Theme.rounded(ctx, UiRect(track.x, track.y, filled, track.height), Theme.ACTIVE, 2)
                Theme.rounded(ctx, UiRect(track.x + filled - 4, track.y - 2, 8, 9), Theme.ACCENT, 3)
            }
            is BoolSetting -> {
                Theme.label(ctx, textRenderer, setting.name, rect.x, rect.y + 7, Theme.TEXT, rect.width - 48)
                val toggle = UiRect(rect.right - 34, rect.y + 3, 34, 17)
                Theme.rounded(ctx, toggle, if (setting.value) Theme.ACTIVE else Theme.CARD, 8)
                Theme.rounded(ctx, UiRect(toggle.x + if (setting.value) 19 else 3, toggle.y + 3, 11, 11), if (setting.value) Theme.ACCENT else Theme.SUBTLE, 5)
            }
            is DropdownSetting -> {
                Theme.label(ctx, textRenderer, setting.name, rect.x, rect.y + 2, Theme.TEXT, rect.width)
                val field = UiRect(rect.x, rect.y + 15, rect.width, 20)
                Theme.panel(ctx, field, Theme.CARD, Theme.BORDER, 4)
                Theme.label(ctx, textRenderer, "<", field.x + 7, field.y + 6, Theme.ACCENT)
                Theme.label(ctx, textRenderer, ">", field.right - 12, field.y + 6, Theme.ACCENT)
                val value = Theme.fit(textRenderer, setting.options.getOrNull(setting.selected) ?: "None", field.width - 42)
                Theme.label(ctx, textRenderer, value, field.x + (field.width - textRenderer.getWidth(value)) / 2, field.y + 6)
            }
            is ColorSetting -> {
                Theme.label(ctx, textRenderer, setting.name, rect.x, rect.y + 3, Theme.TEXT, rect.width - 35)
                Theme.label(ctx, textRenderer, "Click swatch to cycle color", rect.x, rect.y + 17, Theme.SUBTLE, rect.width - 35)
                val swatch = UiRect(rect.right - 25, rect.y + 3, 25, 23)
                Theme.panel(ctx, swatch, setting.toArgb(), Theme.BORDER, 4)
            }
        }
    }

    private fun updateSlider(mx: Double) {
        val setting = draggingSlider ?: return
        val percent = ((mx - sliderTrack.x) / sliderTrack.width.coerceAtLeast(1)).coerceIn(0.0, 1.0).toFloat()
        setting.value = setting.min + percent * (setting.max - setting.min)
    }

    override fun onMouseClicked(mx: Double, my: Double, button: Int): Boolean {
        if (waitingForKey) { waitingForKey = false; return true }
        if (button == 0) {
            if (closeButton.contains(mx, my)) { closeGui(); return true }
            if (enableButton.contains(mx, my)) { mod.toggle(); return true }
            if (favoriteButton.contains(mx, my)) { mod.favorited = !mod.favorited; return true }
            if (keyButton.contains(mx, my)) { waitingForKey = true; return true }
            if (clearButton.contains(mx, my)) { mod.key = GLFW.GLFW_KEY_UNKNOWN; return true }
            updateBounds()
            if (scroll.beginDrag(mx, my, scrollbar)) return true
        }
        if (!viewport.contains(mx, my)) return super.onMouseClicked(mx, my, button)
        for ((setting, rect) in rows()) {
            if (!rect.contains(mx, my)) continue
            when (setting) {
                is SliderSetting -> if (button == 0 && UiRect(rect.x - 4, rect.y + 15, rect.width + 8, 17).contains(mx, my)) {
                    draggingSlider = setting; sliderTrack = UiRect(rect.x, rect.y + 21, rect.width, 5)
                    updateSlider(mx); return true
                }
                is BoolSetting -> if (button == 0) { setting.value = !setting.value; return true }
                is DropdownSetting -> if (setting.options.isNotEmpty() && my >= rect.y + 15 && (button == 0 || button == 1)) {
                    val step = if (button == 1 || mx < rect.x + 24) -1 else 1
                    setting.selected = Math.floorMod(setting.selected + step, setting.options.size); return true
                }
                is ColorSetting -> if (button == 0 && UiRect(rect.right - 25, rect.y + 3, 25, 23).contains(mx, my)) {
                    val current = (setting.r shl 16) or (setting.g shl 8) or setting.b
                    val next = palette[(palette.indexOf(current) + 1).mod(palette.size)]
                    setting.r = next ushr 16 and 255; setting.g = next ushr 8 and 255; setting.b = next and 255
                    return true
                }
            }
        }
        return super.onMouseClicked(mx, my, button)
    }

    override fun onMouseScrolled(mx: Double, my: Double, horizontal: Double, vertical: Double): Boolean {
        updateBounds()
        if (viewport.contains(mx, my) || scrollbar.contains(mx, my)) {
            draggingSlider = null
            if (scroll.wheel(vertical, horizontal)) return true
        }
        return super.onMouseScrolled(mx, my, horizontal, vertical)
    }

    override fun onMouseDragged(mx: Double, my: Double, button: Int, dx: Double, dy: Double): Boolean {
        if (button == 0 && scroll.drag(my, scrollbar)) return true
        if (button == 0 && draggingSlider != null) { updateSlider(mx); return true }
        return super.onMouseDragged(mx, my, button, dx, dy)
    }

    override fun onMouseReleased(mx: Double, my: Double, button: Int): Boolean {
        val consumed = draggingSlider != null || scroll.dragging
        draggingSlider = null; scroll.endDrag()
        return consumed || super.onMouseReleased(mx, my, button)
    }

    override fun onKeyPressed(key: Int, scancode: Int, modifiers: Int): Boolean {
        if (waitingForKey) {
            when (key) {
                GLFW.GLFW_KEY_ESCAPE -> Unit
                GLFW.GLFW_KEY_DELETE, GLFW.GLFW_KEY_BACKSPACE -> mod.key = GLFW.GLFW_KEY_UNKNOWN
                else -> { mod.key = key; mod.keyWasDown = true }
            }
            waitingForKey = false; return true
        }
        when (key) {
            GLFW.GLFW_KEY_PAGE_DOWN -> { scroll.moveBy(viewport.height * 0.85); return true }
            GLFW.GLFW_KEY_PAGE_UP -> { scroll.moveBy(-viewport.height * 0.85); return true }
            GLFW.GLFW_KEY_HOME -> { scroll.moveTo(0.0); return true }
            GLFW.GLFW_KEY_END -> { scroll.moveTo(scroll.maximum); return true }
            GLFW.GLFW_KEY_RIGHT_SHIFT -> { closeGui(); return true }
        }
        return super.onKeyPressed(key, scancode, modifiers)
    }

    override fun closeGui() { MinecraftClient.getInstance().setScreen(parent) }

    private fun keyName(key: Int): String = when (key) {
        GLFW.GLFW_KEY_UNKNOWN -> "None"
        GLFW.GLFW_KEY_SPACE -> "Space"
        GLFW.GLFW_KEY_LEFT_SHIFT -> "Left Shift"
        GLFW.GLFW_KEY_RIGHT_SHIFT -> "Right Shift"
        GLFW.GLFW_KEY_LEFT_CONTROL -> "Left Ctrl"
        GLFW.GLFW_KEY_RIGHT_CONTROL -> "Right Ctrl"
        GLFW.GLFW_KEY_LEFT_ALT -> "Left Alt"
        GLFW.GLFW_KEY_RIGHT_ALT -> "Right Alt"
        GLFW.GLFW_KEY_TAB -> "Tab"
        GLFW.GLFW_KEY_CAPS_LOCK -> "Caps Lock"
        in GLFW.GLFW_KEY_F1..GLFW.GLFW_KEY_F12 -> "F${key - GLFW.GLFW_KEY_F1 + 1}"
        else -> GLFW.glfwGetKeyName(key, 0)?.uppercase() ?: "Key $key"
    }
}
