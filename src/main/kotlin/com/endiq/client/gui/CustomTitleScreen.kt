package com.endiq.client.gui

import com.endiq.TurtleClient
import com.endiq.client.compat.*
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import org.lwjgl.glfw.GLFW
import com.endiq.client.modules.ModuleManager

/** One aspect-correct background and a compact navigation card, even at large GUI scales. */
class CustomTitleScreen : ClientScreen("TurtleClient") {
    private data class Button(val bounds: UiRect, val label: String, val primary: Boolean = false, val enabled:()->Boolean={true}, val action: () -> Unit)
    private val buttons = mutableListOf<Button>()
    private var forest = false
    private var keyboardFocus = -1
    private var card = UiRect(0, 0, 1, 1)
    private var compact = false
    private var wide = false
    private var feedback=""
    private var sceneButton = card
    private var quitButton = card
    private var favoritesButton: UiRect? = null
    private var accountButton=card
    private val version = gameVersion()

    override fun init() {
        buttons.clear()
        compact = height < 330
        wide = width >= 760 && height >= 400
        val header = if (compact) 38 else if (wide) 52 else 82
        val primaryHeight = if (compact) 24 else 30
        val secondaryHeight = if (compact) 22 else 26
        val cardHeight = 24 + header + primaryHeight * 2 + secondaryHeight * 2 + 18
        val cardWidth = minOf(280, (width - 36).coerceAtLeast(1))
        val left = when {
            wide -> width - cardWidth - (width * 0.11).toInt()
            width >= 680 -> (width * 0.12).toInt()
            else -> (width - cardWidth) / 2
        }
        card = UiRect(left.coerceAtLeast(0), (height - cardHeight) / 2, cardWidth, cardHeight)
        val x = card.x + 12
        val w = card.width - 24
        var y = card.y + 12 + header
        buttons += Button(UiRect(x, y, w, primaryHeight), "Singleplayer", true) { MinecraftClient.getInstance().setScreen(SelectWorldScreen(this)) }
        y += primaryHeight + 6
        buttons += Button(UiRect(x, y, w, primaryHeight), "Multiplayer",enabled={multiplayerAllowed()}) { MinecraftClient.getInstance().setScreen(MultiplayerScreen(this)) }
        y += primaryHeight + 6
        val half = (w - 6) / 2
        buttons += Button(UiRect(x, y, half, secondaryHeight), "Modules") { MinecraftClient.getInstance().setScreen(ClickGui(this)) }
        buttons += Button(UiRect(x + half + 6, y, w - half - 6, secondaryHeight), "Cosmetics") { MinecraftClient.getInstance().setScreen(ClickGui(this, true)) }
        y += secondaryHeight + 6
        buttons += Button(UiRect(x, y, half, secondaryHeight), "Options") { MinecraftClient.getInstance().setScreen(optionsScreen(this)) }
        buttons += Button(UiRect(x + half + 6, y, w - half - 6, secondaryHeight), "Screenshots") { openFolder("screenshots") }
        quitButton = UiRect(12, height - 30, 54, 20)
        sceneButton = UiRect(width - 100, height - 30, 88, 20)
        favoritesButton = if (ModuleManager.modules.any { it.favorited }) UiRect(72, height - 30, 74, 20) else null
        keyboardFocus = -1
    }

    override fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        super.renderGui(ctx, mx, my, delta)
        Theme.backdrop(ctx, width, height, if (forest) Theme.FOREST else Theme.COAST)
        // Minimal corner chrome, not a row of overlapping unlabeled glyphs.
        ctx.drawTexture(Theme.LOGO, 12, 10, 18, 18)
        Theme.label(ctx, textRenderer, "TURTLE CLIENT", 36, 15, Theme.TEXT)
        val name = Theme.fit(textRenderer, playerName(), minOf(100, width / 4))
        val account = UiRect(width - textRenderer.getWidth(name) - 44, 10, textRenderer.getWidth(name) + 32, 22)
        accountButton=account
        Theme.button(ctx,textRenderer,account,"",account.contains(mx.toDouble(),my.toDouble()))
        ctx.drawTexture(Theme.icon("account"),account.x+5,account.y+4,14,14,Theme.ACCENT)
        Theme.label(ctx, textRenderer, name, account.x + 23, account.y + 7, Theme.TEXT)
        if(account.contains(mx.toDouble(),my.toDouble()))Theme.tooltip(ctx,textRenderer,"Manage and switch accounts",mx,my,width,height)
        Theme.panel(ctx, card, Theme.PANEL, Theme.BORDER, 8)
        if (wide) {
            Theme.rounded(ctx, UiRect(card.x + 1, card.y + 1, card.width - 2, 51), Theme.BACKGROUND, 7)
            ctx.drawTexture(Theme.LOGO, card.x + 12, card.y + 12, 25, 25)
            Theme.label(ctx, textRenderer, "QUICK PLAY", card.x + 45, card.y + 13, Theme.TEXT)
            Theme.label(ctx, textRenderer, "Choose a world or server", card.x + 45, card.y + 27, Theme.SUBTLE, card.width - 58)
        } else if (compact) {
            val brandX = card.x + (card.width - 168) / 2
            ctx.drawTexture(Theme.LOGO, brandX, card.y + 13, 30, 30)
            ctx.drawTexture(Theme.WORDMARK, brandX + 40, card.y + 19, 128, 24)
        } else {
            ctx.drawTexture(Theme.LOGO, card.x + (card.width - 40) / 2, card.y + 12, 40, 40)
            ctx.drawTexture(Theme.WORDMARK, card.x + (card.width - 144) / 2, card.y + 57, 144, 27)
        }
        if (wide) drawHero(ctx)
        buttons.forEachIndexed { index, button ->
            Theme.button(ctx, textRenderer, button.bounds, button.label,
                button.bounds.contains(mx.toDouble(), my.toDouble()) || keyboardFocus == index, button.primary,button.enabled())
        }
        Theme.button(ctx, textRenderer, quitButton, "Quit", quitButton.contains(mx.toDouble(), my.toDouble()))
        Theme.button(ctx, textRenderer, sceneButton, if (forest) "Scene: Forest" else "Scene: Coast", sceneButton.contains(mx.toDouble(), my.toDouble()))
        favoritesButton?.let { Theme.button(ctx, textRenderer, it, "Favorites", it.contains(mx.toDouble(), my.toDouble())) }
        if(feedback.isNotEmpty())Theme.label(ctx,textRenderer,feedback,card.x,card.bottom+8,Theme.DANGER,card.width)
        val footer = "Fabric  /  Minecraft $version"
        if (width > 390 && (favoritesButton == null || width >= 560)) Theme.label(ctx, textRenderer, footer, (width - textRenderer.getWidth(footer)) / 2, height - 23, Theme.MUTED)
    }

    private fun drawHero(ctx: GuiContext) {
        val x = (width * 0.11).toInt()
        val heroWidth = (card.x - x - 26).coerceAtLeast(160).coerceAtMost(560)
        val y = card.y + 22
        ctx.drawTexture(Theme.LOGO, x, y + 1, 60, 60)
        val wordX = x + 72
        val wordWidth = (heroWidth - 76).coerceAtLeast(110)
        ctx.drawTexture(Theme.WORDMARK, wordX, y + 9, minOf(wordWidth, 250), 45)
        Theme.label(ctx, textRenderer, "A calmer, cleaner Minecraft client.", x, y + 72, Theme.TEXT, heroWidth)
        Theme.label(ctx, textRenderer, "HUD tools  ·  performance  ·  Turtle cosmetics", x, y + 87, Theme.MUTED, heroWidth)

        val summary = UiRect(x, y + 114, heroWidth, 72)
        Theme.panel(ctx, summary, Theme.PANEL, Theme.BORDER, 7)
        Theme.label(ctx, textRenderer, "CLIENT OVERVIEW", summary.x + 10, summary.y + 8, Theme.SUBTLE)
        val columns = listOf(
            ModuleManager.modules.size.toString() to "MODULES",
            ModuleManager.getEnabled().count { it.unavailableReason == null }.toString() to "ENABLED",
            ModuleManager.modules.count { it.favorited }.toString() to "FAVORITES"
        )
        val columnWidth = (summary.width - 20) / columns.size
        columns.forEachIndexed { index, (value, label) ->
            val cx = summary.x + 10 + index * columnWidth
            Theme.label(ctx, textRenderer, value, cx, summary.y + 26, Theme.ACCENT)
            Theme.label(ctx, textRenderer, label, cx, summary.y + 42, Theme.MUTED, columnWidth - 6)
        }
        Theme.chip(ctx, textRenderer, UiRect(x, summary.bottom + 10, minOf(126, heroWidth), 21), "Theme · ${Theme.paletteName}", true)
        Theme.label(ctx, textRenderer, "Right Shift opens the module library", x, summary.bottom + 42, Theme.SUBTLE, heroWidth)
    }

    override fun onMouseClicked(mx: Double, my: Double, button: Int): Boolean {
        if (button == 0) {
            if(accountButton.contains(mx,my)) { MinecraftClient.getInstance().setScreen(AccountsScreen(this));return true }
            buttons.firstOrNull { it.bounds.contains(mx, my) }?.let { if(it.enabled())it.action() else feedback="Multiplayer is disabled by this account's or launcher's permissions.";return true }
            if (quitButton.contains(mx, my)) { MinecraftClient.getInstance().scheduleStop(); return true }
            if (sceneButton.contains(mx, my)) { forest = !forest; return true }
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
            val selected=buttons[keyboardFocus];if(selected.enabled())selected.action() else feedback="Multiplayer is disabled by this account's or launcher's permissions.";return true
        }
        return super.onKeyPressed(key, scancode, modifiers)
    }

    override fun closeGui() { /* The title menu has no screen to return to. */ }

    private fun openFolder(name: String) {
        runCatching {
            val folder = MinecraftClient.getInstance().runDirectory.resolve(name)
            folder.mkdirs()
            openPath(folder)
        }.onFailure { feedback="Could not open the $name folder. Check file permissions." }
    }
}
