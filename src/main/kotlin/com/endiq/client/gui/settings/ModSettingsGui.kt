package com.endiq.client.gui.settings

import com.endiq.client.modules.Module
import com.endiq.client.modules.BoolSetting
import com.endiq.client.modules.SliderSetting
import com.endiq.client.modules.ColorSetting
import com.endiq.client.modules.DropdownSetting
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

class ModSettingsGui(
    private val mod: Module,
    private val parent: Screen
) : Screen(Text.literal("${mod.name} Settings")) {

    private val W = 310
    private val H = 300
    private var sx = 0
    private var sy = 0

    private var draggingSlider: SliderSetting? = null
    private var draggingTrackX = 0
    private var draggingTrackW = 0
    private var textFocusIndex = -1

    // Keybind state
    private var waitingForKey = false
    private var keyName = ""

    private val WHITE  = 0xFFFFFFFF.toInt()
    private val GRAY   = 0xFF888888.toInt()
    private val LGRAY  = 0xFF555555.toInt()
    private val BLACK  = 0xDD000000.toInt()
    private val RED    = 0xFFE05252.toInt()
    private val GREEN  = 0xFF3D9970.toInt()
    private val ACCENT = 0xFF5B9BD5.toInt()
    private val ORANGE = 0xFFFF8C00.toInt()
    private val LOGO   = Identifier.of("turtle-client", "textures/loading_icon.png")

    override fun init() {
        sx = (width  - W) / 2
        sy = (height - H) / 2
        textFocusIndex = -1
        draggingSlider = null
        keyName = getKeyName(mod.key)
    }

    override fun render(ctx: DrawContext, mx: Int, my: Int, delta: Float) {
        ctx.fill(0, 0, width, height, 0x88000000.toInt())

        // Window
        ctx.fill(sx, sy, sx + W, sy + H, 0xF2151515.toInt())
        ctx.fill(sx, sy, sx + W, sy + 26, 0xF2202020.toInt())
        ctx.fill(sx, sy + 25, sx + W, sy + 26, RED)

        // Logo
        try {
            ctx.drawTexture(RenderLayer::getGuiTextured, LOGO, sx + 4, sy + 3, 0f, 0f, 22, 22, 22, 22, -1)
        } catch (_: Exception) {}

        // Title
        ctx.drawTextWithShadow(textRenderer, "${mod.name} Settings", sx + 30, sy + 9, WHITE)

        // Close
        ctx.fill(sx + W - 18, sy + 5, sx + W - 4, sy + 21, 0xFFAA2222.toInt())
        ctx.drawTextWithShadow(textRenderer, "X", sx + W - 13, sy + 8, WHITE)

        // Enable toggle
        val enX = sx + 8; val enY = sy + 34
        ctx.fill(enX, enY, enX + 120, enY + 16, if (mod.enabled) GREEN else LGRAY)
        ctx.drawTextWithShadow(textRenderer, if (mod.enabled) "● Enabled" else "○ Disabled", enX + 6, enY + 4, WHITE)

        // Description
        ctx.drawTextWithShadow(textRenderer, mod.description, sx + 8, sy + 54, GRAY)

        // Divider
        ctx.fill(sx + 8, sy + 65, sx + W - 8, sy + 66, 0xFF2A2A2A.toInt())

        // ── Settings list ─────────────────────────────────────────────
        val trackX = sx + 8; val trackW = W - 16
        var rowY = sy + 72

        // Scissor to clip content
        ctx.enableScissor(sx, sy + 66, sx + W, sy + H - 44)

        mod.settings.forEach { setting ->
            if (rowY > sy + H - 44) return@forEach
            when (setting) {
                is SliderSetting -> {
                    // Label + value
                    ctx.drawTextWithShadow(textRenderer, setting.name, trackX, rowY, WHITE)
                    val valStr = if (setting.max >= 100f && setting.suffix.isEmpty())
                        setting.value.toInt().toString()
                    else "%.2f".format(setting.value) + if (setting.suffix.isNotEmpty()) setting.suffix else ""
                    ctx.drawTextWithShadow(textRenderer, valStr,
                        sx + W - textRenderer.getWidth(valStr) - 8, rowY, ACCENT)
                    rowY += 12
                    // Track
                    ctx.fill(trackX, rowY, trackX + trackW, rowY + 6, 0xFF222222.toInt())
                    val fillW = ((setting.value - setting.min) / (setting.max - setting.min) * trackW).toInt().coerceIn(0, trackW)
                    ctx.fill(trackX, rowY, trackX + fillW, rowY + 6, ACCENT)
                    // Thumb
                    val thumbX = trackX + fillW - 5
                    ctx.fill(thumbX, rowY - 3, thumbX + 10, rowY + 9, WHITE)
                    rowY += 18
                }
                is BoolSetting -> {
                    ctx.drawTextWithShadow(textRenderer, setting.name, trackX, rowY + 4, WHITE)
                    val togX = sx + W - 46
                    ctx.fill(togX, rowY, togX + 38, rowY + 14, if (setting.value) GREEN else LGRAY)
                    ctx.drawTextWithShadow(textRenderer, if (setting.value) "ON" else "OFF",
                        togX + if (setting.value) 8 else 6, rowY + 3, WHITE)
                    rowY += 20
                }
                is DropdownSetting -> {
                    ctx.drawTextWithShadow(textRenderer, setting.name, trackX, rowY + 3, WHITE)
                    val dw = textRenderer.getWidth(setting.value) + 22
                    val dx = sx + W - dw - 4
                    ctx.fill(dx, rowY, dx + dw, rowY + 14, 0xFF2A2A2A.toInt())
                    ctx.fill(dx, rowY, dx + 1, rowY + 14, ACCENT)
                    ctx.drawTextWithShadow(textRenderer, "◀ ${setting.value} ▶", dx + 4, rowY + 3, WHITE)
                    rowY += 20
                }
                is ColorSetting -> {
                    ctx.drawTextWithShadow(textRenderer, setting.name, trackX, rowY + 3, WHITE)
                    val swatch = setting.toArgb()
                    ctx.fill(sx + W - 24, rowY, sx + W - 8, rowY + 14, swatch)
                    ctx.fill(sx + W - 24, rowY, sx + W - 8, rowY + 14, 0x44000000.toInt())
                    rowY += 20
                }
                else -> rowY += 20
            }
        }

        ctx.disableScissor()

        // ── Keybind section (always at bottom) ───────────────────────
        val kbY = sy + H - 40
        ctx.fill(sx, kbY - 1, sx + W, kbY, 0xFF2A2A2A.toInt())
        ctx.fill(sx, kbY, sx + W, sy + H, 0xF2181818.toInt())

        ctx.drawTextWithShadow(textRenderer, "Keybind:", sx + 8, kbY + 8, GRAY)

        val btnX = sx + 70; val btnW = 100; val btnY = kbY + 4
        val isBinding = waitingForKey

        ctx.fill(btnX, btnY, btnX + btnW, btnY + 20,
            if (isBinding) 0xFF333300.toInt() else 0xFF252525.toInt())
        ctx.fill(btnX, btnY, btnX + 1, btnY + 20, if (isBinding) ORANGE else ACCENT)

        val keyLabel = when {
            isBinding -> "Press a key..."
            mod.key == GLFW.GLFW_KEY_UNKNOWN -> "None"
            else -> keyName
        }
        val keyColor = if (isBinding) ORANGE else WHITE
        ctx.drawTextWithShadow(textRenderer, keyLabel,
            btnX + btnW / 2 - textRenderer.getWidth(keyLabel) / 2, btnY + 6, keyColor)

        // Clear keybind button
        if (mod.key != GLFW.GLFW_KEY_UNKNOWN) {
            val clrX = btnX + btnW + 6
            ctx.fill(clrX, btnY, clrX + 30, btnY + 20, 0xFF2A1A1A.toInt())
            ctx.fill(clrX, btnY, clrX + 1, btnY + 20, RED)
            ctx.drawTextWithShadow(textRenderer, "Clear", clrX + 4, btnY + 6, RED)
        }

        // Hint
        ctx.drawTextWithShadow(textRenderer,
            if (isBinding) "ESC to cancel" else "Click to set",
            sx + W - 70, kbY + 12, LGRAY)

        super.render(ctx, mx, my, delta)
    }

    override fun mouseClicked(mx: Double, my: Double, btn: Int): Boolean {
        val imx = mx.toInt(); val imy = my.toInt()

        // If waiting for key, cancel on click
        if (waitingForKey) { waitingForKey = false; return true }

        // Close
        if (imx in (sx + W - 18)..(sx + W - 4) && imy in (sy + 5)..(sy + 21)) {
            MinecraftClient.getInstance().setScreen(parent); return true
        }

        // Enable toggle
        if (imx in (sx + 8)..(sx + 128) && imy in (sy + 34)..(sy + 50)) {
            mod.toggle(); return true
        }

        // Keybind button
        val kbY = sy + H - 40
        val btnX = sx + 70; val btnW = 100; val btnY = kbY + 4
        if (imx in btnX..(btnX + btnW) && imy in btnY..(btnY + 20)) {
            waitingForKey = true; return true
        }

        // Clear keybind
        val clrX = btnX + btnW + 6
        if (mod.key != GLFW.GLFW_KEY_UNKNOWN &&
            imx in clrX..(clrX + 30) && imy in btnY..(btnY + 20)) {
            mod.key = GLFW.GLFW_KEY_UNKNOWN; keyName = "None"; return true
        }

        // Settings
        val trackX = sx + 8; val trackW = W - 16
        var rowY = sy + 72

        mod.settings.forEach { setting ->
            if (rowY > sy + H - 44) return@forEach
            when (setting) {
                is SliderSetting -> {
                    rowY += 12
                    if (imx in trackX..(trackX + trackW) && imy in rowY..(rowY + 6)) {
                        draggingSlider = setting
                        draggingTrackX = trackX; draggingTrackW = trackW
                        val pct = (imx - trackX).toFloat() / trackW
                        setting.value = (setting.min + pct * (setting.max - setting.min)).coerceIn(setting.min, setting.max)
                        return true
                    }
                    rowY += 18
                }
                is BoolSetting -> {
                    val togX = sx + W - 46
                    if (imx in togX..(togX + 38) && imy in rowY..(rowY + 14)) {
                        setting.value = !setting.value; return true
                    }
                    rowY += 20
                }
                is DropdownSetting -> {
                    val dw = textRenderer.getWidth(setting.value) + 22
                    val dx = sx + W - dw - 4
                    if (imx in dx..(dx + dw) && imy in rowY..(rowY + 14)) {
                        setting.selected = (setting.selected + (if (btn == 0) 1 else -1) + setting.options.size).mod(setting.options.size)
                        return true
                    }
                    rowY += 20
                }
                else -> rowY += 20
            }
        }

        return super.mouseClicked(mx, my, btn)
    }

    override fun keyPressed(keyCode: Int, scan: Int, mods: Int): Boolean {
        if (waitingForKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForKey = false
            } else {
                mod.key = keyCode
                keyName = getKeyName(keyCode)
                waitingForKey = false
            }
            return true
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().setScreen(parent); return true
        }
        return super.keyPressed(keyCode, scan, mods)
    }

    override fun mouseDragged(mx: Double, my: Double, btn: Int, dx: Double, dy: Double): Boolean {
        draggingSlider?.let { s ->
            val pct = (mx.toInt() - draggingTrackX).toFloat() / draggingTrackW
            s.value = (s.min + pct * (s.max - s.min)).coerceIn(s.min, s.max)
        }
        return super.mouseDragged(mx, my, btn, dx, dy)
    }

    override fun mouseReleased(mx: Double, my: Double, btn: Int): Boolean {
        draggingSlider = null
        return super.mouseReleased(mx, my, btn)
    }

    override fun shouldPause() = false

    private fun getKeyName(keyCode: Int): String = when (keyCode) {
        GLFW.GLFW_KEY_UNKNOWN -> "None"
        GLFW.GLFW_KEY_SPACE -> "Space"
        GLFW.GLFW_KEY_LEFT_SHIFT -> "L.Shift"
        GLFW.GLFW_KEY_RIGHT_SHIFT -> "R.Shift"
        GLFW.GLFW_KEY_LEFT_CONTROL -> "L.Ctrl"
        GLFW.GLFW_KEY_RIGHT_CONTROL -> "R.Ctrl"
        GLFW.GLFW_KEY_LEFT_ALT -> "L.Alt"
        GLFW.GLFW_KEY_RIGHT_ALT -> "R.Alt"
        GLFW.GLFW_KEY_TAB -> "Tab"
        GLFW.GLFW_KEY_CAPS_LOCK -> "Caps"
        GLFW.GLFW_KEY_ESCAPE -> "ESC"
        GLFW.GLFW_KEY_F1 -> "F1"; GLFW.GLFW_KEY_F2 -> "F2"
        GLFW.GLFW_KEY_F3 -> "F3"; GLFW.GLFW_KEY_F4 -> "F4"
        GLFW.GLFW_KEY_F5 -> "F5"; GLFW.GLFW_KEY_F6 -> "F6"
        GLFW.GLFW_KEY_F7 -> "F7"; GLFW.GLFW_KEY_F8 -> "F8"
        GLFW.GLFW_KEY_F9 -> "F9"; GLFW.GLFW_KEY_F10 -> "F10"
        GLFW.GLFW_KEY_F11 -> "F11"; GLFW.GLFW_KEY_F12 -> "F12"
        else -> {
            val name = GLFW.glfwGetKeyName(keyCode, 0)
            name?.uppercase() ?: "Key $keyCode"
        }
    }
}
