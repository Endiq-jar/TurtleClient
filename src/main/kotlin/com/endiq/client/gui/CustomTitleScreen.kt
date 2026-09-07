package com.endiq.client.gui

import com.endiq.client.compat.*
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import com.endiq.client.modules.ModuleManager
import org.lwjgl.glfw.GLFW

/**
 * The home screen: one flat backdrop and a short column of bare text buttons.
 * There is no scene switcher, no panorama art and no panel chrome — the mark in the
 * corner, the account name and the menu are everything on screen.
 */
class CustomTitleScreen : ClientScreen("TurtleClient") {
    private data class Button(val bounds: UiRect, val label: String,
                              val enabled: () -> Boolean = { true }, val action: () -> Unit)

    private val buttons = mutableListOf<Button>()
    private var keyboardFocus = -1
    private var feedback = ""
    private var accountButton = UiRect(0, 0, 1, 1)
    private var favoritesButton: UiRect? = null
    private val favoritesLabel = "Favorites"

    private const val PANORAMA_WIDTH = 1024
    private const val PANORAMA_HEIGHT = 512
    private const val PAN_MS = 90_000L
    private const val PANORAMA_VEIL = 0x72000000

    override fun init() {
        buttons.clear()
        val gap = if (height < 260) 15 else 20
        var y = (height - gap * 5) / 2 - 6
        val centerX = width / 2
        fun add(label: String, enabled: () -> Boolean = { true }, action: () -> Unit) {
            buttons += Button(Theme.textButtonBounds(textRenderer.getWidth(label), centerX, y), label, enabled, action)
            y += gap
        }
        add("Singleplayer") { MinecraftClient.getInstance().setScreen(SelectWorldScreen(this)) }
        add("Multiplayer", { multiplayerAllowed() }) { MinecraftClient.getInstance().setScreen(MultiplayerScreen(this)) }
        add("Modules") { MinecraftClient.getInstance().setScreen(ClickGui(this)) }
        add("Capes") { MinecraftClient.getInstance().setScreen(ClickGui(this, initialCapes = true)) }
        add("Options") { MinecraftClient.getInstance().setScreen(optionsScreen(this)) }
        add("Quit") { MinecraftClient.getInstance().scheduleStop() }
        favoritesButton = if (ModuleManager.modules.any { it.favorited })
            UiRect(width - textRenderer.getWidth(favoritesLabel) - 15, height - 21, textRenderer.getWidth(favoritesLabel) + 10, 12)
        else null
        keyboardFocus = -1
    }

    /**
     * The internet-sourced backdrop, panned slowly left-to-right and veiled so the
     * bare text buttons stay readable. Cover-fits the screen; the pan ping-pongs so
     * no seam is ever visible.
     */
    private fun drawPanorama(ctx: GuiContext) {
        val aspect = width.toFloat() / height.coerceAtLeast(1)
        var srcH = PANORAMA_HEIGHT
        var srcW = (srcH * aspect).toInt()
        if (srcW > PANORAMA_WIDTH) { srcW = PANORAMA_WIDTH; srcH = (srcW / aspect).toInt() }
        val maxPan = (PANORAMA_WIDTH - srcW).coerceAtLeast(0)
        val cycle = (System.currentTimeMillis() % PAN_MS) / PAN_MS.toFloat()
        val tri = if (cycle < 0.5f) cycle * 2f else (1f - cycle) * 2f
        val u = (maxPan * tri).toInt()
        val v = (PANORAMA_HEIGHT - srcH) / 2
        ctx.drawTextureRegion(Theme.PANORAMA, 0, 0, width, height, u.toFloat(), v.toFloat(),
            srcW.coerceAtLeast(1), srcH.coerceAtLeast(1), PANORAMA_WIDTH, PANORAMA_HEIGHT)
        ctx.fill(0, 0, width, height, PANORAMA_VEIL)
    }

    override fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        super.renderGui(ctx, mx, my, delta)
        drawPanorama(ctx)
        ctx.drawTexture(Theme.LOGO, 12, 11, 16, 16)
        Theme.label(ctx, textRenderer, "TURTLE CLIENT", 34, 15, Theme.MUTED)

        val name = Theme.fit(textRenderer, playerName(), minOf(110, (width / 4).coerceAtLeast(40)))
        val account = UiRect(width - textRenderer.getWidth(name) - 15, 12, textRenderer.getWidth(name) + 10, 12)
        accountButton = account
        val accountHovered = account.contains(mx.toDouble(), my.toDouble())
        Theme.label(ctx, textRenderer, name, account.x + 5, account.y, if (accountHovered) Theme.ACCENT else Theme.MUTED)
        if (accountHovered) {
            ctx.fill(account.x + 5, account.bottom, account.right - 5, account.bottom + 1, Theme.ACCENT)
            Theme.tooltip(ctx, textRenderer, "Manage and switch accounts", mx, my, width, height)
        }

        buttons.forEachIndexed { index, button ->
            Theme.textButton(ctx, textRenderer, button.bounds, button.label,
                button.bounds.contains(mx.toDouble(), my.toDouble()) || keyboardFocus == index, button.enabled())
        }

        if (feedback.isNotEmpty()) {
            val message = Theme.fit(textRenderer, feedback, width - 24)
            Theme.label(ctx, textRenderer, message, (width - textRenderer.getWidth(message)) / 2,
                buttons.last().bounds.bottom + 16, Theme.DANGER)
        }
        favoritesButton?.let {
            val hovered = it.contains(mx.toDouble(), my.toDouble())
            Theme.label(ctx, textRenderer, favoritesLabel, it.x + 5, it.y, if (hovered) Theme.ACCENT else Theme.SUBTLE)
            if (hovered) ctx.fill(it.x + 5, it.bottom, it.right - 5, it.bottom + 1, Theme.ACCENT)
        }
    }

    override fun onMouseClicked(mx: Double, my: Double, button: Int): Boolean {
        if (button == 0) {
            if (accountButton.contains(mx, my)) { MinecraftClient.getInstance().setScreen(AccountsScreen(this)); return true }
            buttons.firstOrNull { it.bounds.contains(mx, my) }?.let {
                if (it.enabled()) it.action()
                else feedback = "Multiplayer is disabled by this account's or launcher's permissions."
                return true
            }
            if (favoritesButton?.contains(mx, my) == true) {
                MinecraftClient.getInstance().setScreen(ClickGui(this, favoritesOnly = true)); return true
            }
        }
        return super.onMouseClicked(mx, my, button)
    }

    override fun onKeyPressed(key: Int, scancode: Int, modifiers: Int): Boolean {
        if (key == GLFW.GLFW_KEY_TAB || key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_UP) {
            val step = if (key == GLFW.GLFW_KEY_UP || modifiers and GLFW.GLFW_MOD_SHIFT != 0) -1 else 1
            keyboardFocus = if (keyboardFocus < 0 && step < 0) buttons.lastIndex else Math.floorMod(keyboardFocus + step, buttons.size)
            return true
        }
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && keyboardFocus >= 0) {
            val selected = buttons[keyboardFocus]
            if (selected.enabled()) selected.action()
            else feedback = "Multiplayer is disabled by this account's or launcher's permissions."
            return true
        }
        return super.onKeyPressed(key, scancode, modifiers)
    }

    override fun closeGui() { /* The title menu has no screen to return to. */ }
}
