package com.endiq.client.gui

import com.endiq.TurtleClient
import com.endiq.client.compat.*
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import org.lwjgl.glfw.GLFW

/** One aspect-correct background and a compact navigation card, even at large GUI scales. */
class CustomTitleScreen : ClientScreen("TurtleClient") {
    private data class Button(val bounds: UiRect, val label: String, val primary: Boolean = false, val action: () -> Unit)
    private val buttons = mutableListOf<Button>()
    private var forest = false
    private var keyboardFocus = -1
    private var card = UiRect(0, 0, 1, 1)
    private var compact = false
    private var sceneButton = card
    private var quitButton = card
    private val version = gameVersion()

    override fun init() {
        buttons.clear()
        compact = height < 330
        val header = if (compact) 38 else 82
        val primaryHeight = if (compact) 24 else 30
        val secondaryHeight = if (compact) 22 else 26
        val cardHeight = 24 + header + primaryHeight * 2 + secondaryHeight * 2 + 18
        val cardWidth = minOf(268, (width - 36).coerceAtLeast(1))
        val left = if (width >= 680) (width * 0.12).toInt() else (width - cardWidth) / 2
        card = UiRect(left, (height - cardHeight) / 2, cardWidth, cardHeight)
        val x = card.x + 12
        val w = card.width - 24
        var y = card.y + 12 + header
        buttons += Button(UiRect(x, y, w, primaryHeight), "Singleplayer", true) { MinecraftClient.getInstance().setScreen(SelectWorldScreen(this)) }
        y += primaryHeight + 6
        buttons += Button(UiRect(x, y, w, primaryHeight), "Multiplayer") { MinecraftClient.getInstance().setScreen(MultiplayerScreen(this)) }
        y += primaryHeight + 6
        val half = (w - 6) / 2
        buttons += Button(UiRect(x, y, half, secondaryHeight), "Modules") { MinecraftClient.getInstance().setScreen(ClickGui(this)) }
        buttons += Button(UiRect(x + half + 6, y, w - half - 6, secondaryHeight), "Cosmetics") { MinecraftClient.getInstance().setScreen(ClickGui(this, true)) }
        y += secondaryHeight + 6
        buttons += Button(UiRect(x, y, half, secondaryHeight), "Options") { MinecraftClient.getInstance().setScreen(optionsScreen(this)) }
        buttons += Button(UiRect(x + half + 6, y, w - half - 6, secondaryHeight), "Screenshots") { openFolder("screenshots") }
        quitButton = UiRect(12, height - 30, 54, 20)
        sceneButton = UiRect(width - 100, height - 30, 88, 20)
        keyboardFocus = -1
    }

    override fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        Theme.backdrop(ctx, width, height, if (forest) Theme.FOREST else Theme.COAST)
        // Minimal corner chrome, not a row of overlapping unlabeled glyphs.
        ctx.drawTexture(Theme.LOGO, 12, 10, 18, 18)
        Theme.label(ctx, textRenderer, "TURTLE CLIENT", 36, 15, Theme.TEXT)
        val name = Theme.fit(textRenderer, playerName(), minOf(100, width / 4))
        val account = UiRect(width - textRenderer.getWidth(name) - 26, 10, textRenderer.getWidth(name) + 14, 20)
        Theme.panel(ctx, account, 0xBF101C20.toInt(), 0x65385855, 5)
        Theme.label(ctx, textRenderer, name, account.x + 7, account.y + 6, Theme.MUTED)
        Theme.panel(ctx, card, 0xEC101C20.toInt(), 0xCC36514A.toInt(), 9)
        if (compact) {
            val brandX = card.x + (card.width - 168) / 2
            ctx.drawTexture(Theme.LOGO, brandX, card.y + 13, 30, 30)
            ctx.drawTexture(Theme.WORDMARK, brandX + 40, card.y + 19, 128, 24)
        } else {
            ctx.drawTexture(Theme.LOGO, card.x + (card.width - 40) / 2, card.y + 12, 40, 40)
            ctx.drawTexture(Theme.WORDMARK, card.x + (card.width - 144) / 2, card.y + 57, 144, 27)
        }
        buttons.forEachIndexed { index, button ->
            Theme.button(ctx, textRenderer, button.bounds, button.label,
                button.bounds.contains(mx.toDouble(), my.toDouble()) || keyboardFocus == index, button.primary)
        }
        Theme.button(ctx, textRenderer, quitButton, "Quit", quitButton.contains(mx.toDouble(), my.toDouble()))
        Theme.button(ctx, textRenderer, sceneButton, if (forest) "Scene: Forest" else "Scene: Coast", sceneButton.contains(mx.toDouble(), my.toDouble()))
        if (width >= 680) {
            val line = "YOUR WORLD. YOUR PACE."
            Theme.label(ctx, textRenderer, line, card.right + 32, card.bottom - 18, Theme.TEXT, width - card.right - 48)
        }
        val footer = "Fabric  /  Minecraft $version"
        if (width > 390) Theme.label(ctx, textRenderer, footer, (width - textRenderer.getWidth(footer)) / 2, height - 23, Theme.MUTED)
        super.renderGui(ctx, mx, my, delta)
    }

    override fun onMouseClicked(mx: Double, my: Double, button: Int): Boolean {
        if (button == 0) {
            buttons.firstOrNull { it.bounds.contains(mx, my) }?.let { it.action(); return true }
            if (quitButton.contains(mx, my)) { MinecraftClient.getInstance().scheduleStop(); return true }
            if (sceneButton.contains(mx, my)) { forest = !forest; return true }
        }
        return super.onMouseClicked(mx, my, button)
    }

    override fun onKeyPressed(key: Int, scancode: Int, modifiers: Int): Boolean {
        if (key == GLFW.GLFW_KEY_TAB || key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_UP) {
            val step = if (key == GLFW.GLFW_KEY_UP || modifiers and GLFW.GLFW_MOD_SHIFT != 0) -1 else 1
            keyboardFocus = Math.floorMod(keyboardFocus + step, buttons.size)
            return true
        }
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && keyboardFocus >= 0) {
            buttons[keyboardFocus].action(); return true
        }
        return super.onKeyPressed(key, scancode, modifiers)
    }

    override fun closeGui() { /* The title menu has no screen to return to. */ }

    private fun openFolder(name: String) {
        runCatching {
            val folder = MinecraftClient.getInstance().runDirectory.resolve(name)
            folder.mkdirs()
            openPath(folder)
        }.onFailure { TurtleClient.LOGGER.warn("Unable to open the $name folder", it) }
    }
}
