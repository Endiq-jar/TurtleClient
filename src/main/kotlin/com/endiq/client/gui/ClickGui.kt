package com.endiq.client.gui

import com.endiq.client.capes.CapeManager
import com.endiq.client.capes.CapeSource
import com.endiq.client.compat.*
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import com.endiq.client.gui.settings.ModSettingsGui
import com.endiq.client.modules.Module
import com.endiq.client.modules.ModuleManager
import org.lwjgl.glfw.GLFW

/**
 * Sidebar library (redesign 03/04): categories and cape sources live in a
 * fixed left rail with counts; the grid, search and actions never reflow
 * when a tab label changes. Flat fills only — see TurtleTheme.
 */
class ClickGui(private val parent: Screen? = null, initialCapes: Boolean = false, private var favoritesOnly: Boolean = false) : ClientScreen("TurtleClient") {
    private var category = Module.Category.ALL
    private var source: CapeSource? = null
    private var capes = initialCapes
    private var query = ""
    private var feedback = ""
    private var searchFocused = false
    private val moduleScroll = ScrollState()
    private val capeScroll = ScrollState()
    private val scroll get() = if (capes) capeScroll else moduleScroll
    private var modules = emptyList<Module>()
    private var entries = emptyList<CapeManager.Cape>()
    private var panel = UiRect(0, 0, 1, 1)
    private var sidebar = UiRect(0, 0, 1, 1)
    private var viewport = panel
    private var track = panel
    private var searchBox = panel
    private var fpsBoostButton = panel
    private var themeDots = emptyList<UiRect>()
    private var hudEditorButton = panel
    private var modsButton = panel
    private var capesButton = panel
    private var closeButton = panel
    private var capeTools = emptyList<UiRect>()
    private var grid = UiGrid(panel)
    private val rows = mutableListOf<Pair<UiRect, Int>>()
    private val categories = listOf(Module.Category.ALL, Module.Category.HUD, Module.Category.PVP,
        Module.Category.RENDER, Module.Category.MOVEMENT, Module.Category.UTILITY,
        Module.Category.HYPIXEL, Module.Category.PERFORMANCE)
    /** Where a cape came from doubles as the list filter; null means every source. */
    private val sources: List<CapeSource?> = listOf(null) + CapeSource.values().toList()
    private val version = gameVersion()

    override fun init() {
        moduleScroll.endDrag(); capeScroll.endDrag()
        CapeManager.loadFromDisk()
        refreshItems()
        layout() // Preserve scroll when returning from settings; only clamp after a resize.
    }

    private fun rowIcon(index: Int): String? = if (capes) null else when (categories[index]) {
        Module.Category.ALL -> "grid"
        Module.Category.HUD -> "hud"
        Module.Category.PVP -> "pvp"
        Module.Category.RENDER -> "render"
        Module.Category.MOVEMENT -> "movement"
        Module.Category.UTILITY -> "utility"
        Module.Category.HYPIXEL -> "hypixel"
        Module.Category.PERFORMANCE -> "performance"
    }

    private fun rowLabel(index: Int): String = if (capes) sources[index]?.displayName ?: "All Capes" else when {
        index == categories.size -> "Favorites"
        categories[index] == Module.Category.MOVEMENT -> "Move"
        categories[index] == Module.Category.PERFORMANCE -> "Perf"
        else -> categories[index].displayName
    }

    private fun rowCount(index: Int): String = if (capes) CapeManager.bySource(sources[index]).size.toString()
        else if (index == categories.size) ModuleManager.modules.count { it.favorited }.toString()
        else ModuleManager.getByCategory(categories[index]).size.toString()

    private fun rowCountTotal() = if (capes) sources.size else categories.size + 1

    private fun refreshItems() {
        modules = ModuleManager.getByCategory(category).filter { it.name.contains(query, true) && (!favoritesOnly || it.favorited) }
        entries = CapeManager.bySource(source).filter { it.name.contains(query, true) }
    }

    private fun layout() {
        panel = centeredPanel(width, height, 780, 470)
        val sidebarWidth = if (panel.width < 560) 122 else 158
        sidebar = UiRect(panel.x + 1, panel.y + 1, sidebarWidth, panel.height - 2)
        val mainX = sidebar.right + 8
        closeButton = UiRect(panel.right - 29, panel.y + 8, 21, 22)
        capesButton = UiRect(closeButton.x - 62, closeButton.y, 57, 22)
        modsButton = UiRect(capesButton.x - 47, closeButton.y, 42, 22)
        hudEditorButton = UiRect(modsButton.x - 68, closeButton.y, 62, 22)
        searchBox = UiRect(mainX, panel.y + 8, (hudEditorButton.x - mainX - 6).coerceAtLeast(60), 22)
        rows.clear()
        var ry = panel.y + 58
        // Shrink rows before they can spill into the theme dots on short screens.
        val rowStep = ((panel.height - 118) / rowCountTotal().coerceAtLeast(1)).coerceIn(19, 28)
        for (i in 0 until rowCountTotal()) {
            rows.add(UiRect(sidebar.x + 7, ry, sidebar.width - 14, (rowStep - 3).coerceAtLeast(16)) to i)
            ry += rowStep
        }
        fpsBoostButton = UiRect(sidebar.x + 7, ry + 4, sidebar.width - 14, 23)
        themeDots = listOf(0, 1, 2).map { UiRect(sidebar.x + 10 + it * 22, panel.bottom - 24, 13, 13) }
        capeTools = if (capes) listOf(0, 1, 2).map {
            UiRect(mainX + it * 118, panel.y + 38, 112, 22)
        } else emptyList()
        val top = panel.y + if (capes) 68 else 38
        val bottom = panel.bottom - 24
        track = UiRect(panel.right - 11, top + 4, 6, (bottom - top - 8).coerceAtLeast(1))
        viewport = UiRect(mainX - 1, top, (track.x - mainX - 2).coerceAtLeast(1), (bottom - top).coerceAtLeast(1))
        grid = UiGrid(viewport, minimumCardWidth = if (capes) 116 else 132)
        updateBounds()
    }

    private fun updateBounds() = scroll.update(grid.contentHeight(if (capes) entries.size else modules.size), viewport.height)

    override fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        super.renderGui(ctx, mx, my, delta)
        CapeManager.syncIfPending()
        ctx.fill(0, 0, width, height, 0xB00B1210.toInt())
        Theme.panel(ctx, panel)
        drawSidebar(ctx, mx, my)
        Theme.button(ctx, textRenderer, modsButton, "Mods", modsButton.contains(mx.toDouble(), my.toDouble()), !capes)
        Theme.button(ctx, textRenderer, capesButton, "Capes", capesButton.contains(mx.toDouble(), my.toDouble()), capes)
        Theme.button(ctx, textRenderer, hudEditorButton, "HUD Edit",
            hudEditorButton.contains(mx.toDouble(), my.toDouble()), enabled = true)
        iconButton(ctx, closeButton, "close", mx, my)
        Theme.panel(ctx, searchBox, Theme.BACKGROUND, if (searchFocused) Theme.ACCENT else Theme.BORDER, 5)
        ctx.drawTexture(Theme.icon("search"), searchBox.x + 6, searchBox.y + 4, 14, 14, Theme.MUTED)
        val placeholder = if (capes) "Search capes" else "Search modules"
        val caret = if (searchFocused && System.currentTimeMillis() / 500 % 2 == 0L) "_" else ""
        Theme.label(ctx, textRenderer, if (query.isEmpty() && !searchFocused) placeholder else query + caret,
            searchBox.x + 25, searchBox.y + 7, if (query.isEmpty()) Theme.MUTED else Theme.TEXT, searchBox.width - 44)
        if (query.isNotEmpty()) ctx.drawTexture(Theme.icon("close"), searchBox.right - 18, searchBox.y + 5, 12, 12, Theme.MUTED)
        if (capes) {
            val labels = listOf("Download", "Folder", "Unequip")
            capeTools.forEachIndexed { i, rect ->
                Theme.button(ctx, textRenderer, rect, labels[i], rect.contains(mx.toDouble(), my.toDouble()), i == 0)
            }
        }

        updateBounds()
        ctx.enableScissor(viewport.x, viewport.y, viewport.right, viewport.bottom)
        try {
            if (capes) entries.forEachIndexed { i, entry -> drawCape(ctx, entry, grid.card(i, scroll.pixels), mx, my) }
            else modules.forEachIndexed { i, module -> drawModule(ctx, module, grid.card(i, scroll.pixels), mx, my) }
            if ((capes && entries.isEmpty()) || (!capes && modules.isEmpty())) {
                val message = when {
                    query.isNotEmpty() -> "No matches. Try another search."
                    favoritesOnly -> "No favorites yet. Star a module in its settings."
                    capes && CapeManager.busy -> "Downloading capes..."
                    capes -> "No capes yet."
                    else -> "No modules in this category."
                }
                Theme.label(ctx, textRenderer, message, viewport.x + 12, viewport.y + 20, Theme.MUTED, viewport.width - 24)
                if (capes && query.isEmpty() && !CapeManager.busy)
                    Theme.label(ctx, textRenderer, "Use Download, or drop PNGs into turtle-client/capes/local",
                        viewport.x + 12, viewport.y + 36, Theme.SUBTLE, viewport.width - 24)
            }
        } finally { ctx.disableScissor() }
        Theme.scrollbar(ctx, scroll, track, mx, my)
        ctx.fill(sidebar.right + 8, panel.bottom - 24, panel.right - 8, panel.bottom - 23, Theme.BORDER)
        val hint = if (capes) CapeManager.status else feedback.ifEmpty { "Right-click: settings  /  Wheel: scroll" }
        val footer = "${ModuleManager.getEnabled().size} active  ·  MC $version"
        val reserved = textRenderer.getWidth(footer) + 20
        Theme.label(ctx, textRenderer, hint, sidebar.right + 10, panel.bottom - 15, Theme.MUTED, panel.width - sidebar.width - reserved - 20)
        Theme.label(ctx, textRenderer, footer, panel.right - reserved + 8, panel.bottom - 15, Theme.SUBTLE)
        if (!capes && viewport.contains(mx.toDouble(), my.toDouble())) {
            grid.hit(mx.toDouble(), my.toDouble(), modules.size, scroll.pixels)?.let { index ->
                Theme.tooltip(ctx, textRenderer, com.endiq.client.modules.ModulePresentation.note(modules[index]), mx, my, width, height)
            }
        }
        if (hudEditorButton.contains(mx.toDouble(), my.toDouble()))
            Theme.tooltip(ctx, textRenderer, "Open the drag-and-drop HUD editor", mx, my, width, height)
        if (fpsBoostButton.contains(mx.toDouble(), my.toDouble()) && !capes)
            Theme.tooltip(ctx, textRenderer, "FPS Boost: enable Culling, Dynamic Render Distance and Adaptive VSync", mx, my, width, height)
        themeDots.firstOrNull { it.contains(mx.toDouble(), my.toDouble()) }?.let {
            Theme.tooltip(ctx, textRenderer, "Theme: ${Theme.paletteName} - click a dot to switch", mx, my, width, height)
        }
    }

    private fun drawSidebar(ctx: GuiContext, mx: Int, my: Int) {
        ctx.fill(sidebar.x, sidebar.y, sidebar.right, sidebar.bottom, Theme.BACKGROUND)
        ctx.fill(sidebar.right - 1, sidebar.y + 6, sidebar.right, sidebar.bottom - 6, Theme.BORDER)
        ctx.drawTexture(Theme.LOGO, sidebar.x + 9, sidebar.y + 9, 25, 25)
        if (sidebar.width > 140) {
            ctx.drawTexture(Theme.WORDMARK, sidebar.x + 39, sidebar.y + 13, 96, 18)
        } else Theme.label(ctx, textRenderer, "TURTLE", sidebar.x + 39, sidebar.y + 16, Theme.TEXT, sidebar.width - 44)
        for ((rect, index) in rows) {
            val selected = if (capes) (index == sources.indexOf(source))
                else if (index == categories.size) favoritesOnly
                else !favoritesOnly && categories[index] == category
            Theme.sidebarRow(ctx, textRenderer, rect, rowIcon(index), rowLabel(index), rowCount(index),
                selected, rect.contains(mx.toDouble(), my.toDouble()), mx)
        }
        if (!capes) Theme.button(ctx, textRenderer, fpsBoostButton, "FPS Boost",
            fpsBoostButton.contains(mx.toDouble(), my.toDouble()), primary = false)
        val dotColors = listOf(0xFF8FE3B0.toInt(), 0xFF78D2C0.toInt(), 0xFFF0B783.toInt())
        themeDots.forEachIndexed { i, rect ->
            if (Theme.paletteName == Theme.paletteNames[i])
                Theme.rounded(ctx, UiRect(rect.x - 2, rect.y - 2, rect.width + 4, rect.height + 4), Theme.TEXT, 8)
            Theme.rounded(ctx, rect, dotColors[i], 6)
        }
        Theme.label(ctx, textRenderer, Theme.paletteName, themeDots.last().right + 8, panel.bottom - 21, Theme.SUBTLE, sidebar.width - 80)
    }

    private fun drawModule(ctx: GuiContext, module: Module, rect: UiRect, mx: Int, my: Int) {
        if (rect.bottom <= viewport.y || rect.y >= viewport.bottom) return
        val hover = viewport.contains(mx.toDouble(), my.toDouble()) && rect.contains(mx.toDouble(), my.toDouble())
        Theme.panel(ctx, rect, if (hover) Theme.HOVER else Theme.CARD, if (module.enabled) Theme.ACCENT else Theme.BORDER, 6)
        val settings = UiRect(rect.right - 29, rect.y + 7, 22, 22)
        iconButton(ctx, settings, "settings", mx, my)
        if (module.isNew) Theme.label(ctx, textRenderer, "NEW", rect.x + 8, rect.y + 12, Theme.ACCENT)
        if (module.favorited) Theme.label(ctx, textRenderer, "*", rect.x + 8, rect.bottom - 32, Theme.ACCENT)
        val tile = UiRect(rect.x + (rect.width - 38) / 2, rect.y + 7, 38, 38)
        Theme.rounded(ctx, tile, if (module.enabled) Theme.ACTIVE else Theme.BACKGROUND, 6)
        ctx.drawTexture(Theme.moduleIcon(module), tile.x + 6, tile.y + 6, 26, 26,
            if (module.unavailableReason != null) Theme.SUBTLE else if (module.enabled) Theme.ACCENT else Theme.MUTED)
        val name = Theme.fit(textRenderer, module.name, rect.width - 16)
        Theme.label(ctx, textRenderer, name, rect.x + (rect.width - textRenderer.getWidth(name)) / 2, rect.y + 47, Theme.TEXT)
        ctx.fill(rect.x + 8, rect.bottom - 21, rect.right - 8, rect.bottom - 20, Theme.BORDER)
        Theme.label(ctx, textRenderer, if (module.unavailableReason != null) "Unavailable" else if (module.enabled) "Enabled" else "Disabled",
            rect.x + 10, rect.bottom - 14, if (module.enabled) Theme.ACCENT else Theme.SUBTLE, rect.width - 52)
        Theme.switch(ctx, UiRect(rect.right - 38, rect.bottom - 19, 28, 13), module.enabled, module.unavailableReason == null)
    }

    /** The cape's own back panel, sampled from the real texture: no separate preview art. */
    private fun drawCape(ctx: GuiContext, cape: CapeManager.Cape, rect: UiRect, mx: Int, my: Int) {
        if (rect.bottom <= viewport.y || rect.y >= viewport.bottom) return
        val equipped = CapeManager.isEquipped(cape)
        val hover = viewport.contains(mx.toDouble(), my.toDouble()) && rect.contains(mx.toDouble(), my.toDouble())
        Theme.panel(ctx, rect, if (hover) Theme.HOVER else Theme.CARD, if (equipped) Theme.ACCENT else Theme.BORDER, 6)
        ctx.drawTextureRegion(cape.texture, rect.x + (rect.width - 16) / 2, rect.y + 6, 16, 26, 1f, 1f,
            cape.previewWidth, cape.previewHeight, cape.width, cape.height)
        val name = Theme.fit(textRenderer, cape.name, rect.width - 16)
        Theme.label(ctx, textRenderer, name, rect.x + (rect.width - textRenderer.getWidth(name)) / 2,
            rect.y + if (rect.height < 68) 34 else 38, Theme.TEXT)
        ctx.fill(rect.x + 8, rect.bottom - 24, rect.right - 8, rect.bottom - 23, Theme.BORDER)
        Theme.label(ctx, textRenderer, if (equipped) "Equipped" else cape.source.displayName, rect.x + 10, rect.bottom - 15,
            if (equipped) Theme.ACCENT else Theme.SUBTLE, rect.width - 20)
        if (equipped) Theme.chip(ctx, textRenderer, UiRect(rect.right - 52, rect.y + 6, 44, 14), "Active", true)
    }

    private fun iconButton(ctx: GuiContext, rect: UiRect, icon: String, mx: Int, my: Int) {
        val hover = rect.contains(mx.toDouble(), my.toDouble())
        if (hover) Theme.rounded(ctx, rect, Theme.HOVER, 4)
        ctx.drawTexture(Theme.icon(icon), rect.x + 4, rect.y + 4, rect.width - 8, rect.height - 8, if (hover) Theme.ACCENT else Theme.MUTED)
    }

    private fun changeView(value: Boolean) {
        capes = value; favoritesOnly = false; query = ""; searchFocused = false
        moduleScroll.endDrag(); capeScroll.endDrag()
        refreshItems(); layout()
    }

    private fun applyFpsBoost() {
        var enabled = 0
        for (name in listOf("Culling", "Dynamic Render Distance", "Adaptive VSync")) {
            val module = ModuleManager.getByName(name) ?: continue
            if (module.unavailableReason == null && !module.enabled) { module.enable(); enabled++ }
        }
        feedback = if (enabled > 0) "FPS Boost: $enabled performance module(s) enabled."
            else "FPS Boost: performance modules are already on."
        com.endiq.client.config.ModulePreferences.save()
    }

    private fun changedSearch() { scroll.reset(); refreshItems(); updateBounds() }

    override fun onMouseClicked(mx: Double, my: Double, button: Int): Boolean {
        updateBounds()
        if (button == 0) {
            if (closeButton.contains(mx, my)) { closeGui(); return true }
            if (modsButton.contains(mx, my)) { changeView(false); return true }
            if (capesButton.contains(mx, my)) { changeView(true); return true }
            if (hudEditorButton.contains(mx, my)) {
                MinecraftClient.getInstance().setScreen(HudEditorScreen(this)); return true
            }
            themeDots.forEachIndexed { i, rect -> if (rect.contains(mx, my)) {
                Theme.setPalette(Theme.paletteNames[i]); com.endiq.client.config.ModulePreferences.save(); return true
            } }
            if (!capes && fpsBoostButton.contains(mx, my)) { applyFpsBoost(); return true }
            for ((rect, index) in rows) if (rect.contains(mx, my)) {
                if (capes) source = sources[index]
                else if (index == categories.size) favoritesOnly = true
                else { favoritesOnly = false; category = categories[index] }
                scroll.reset(); refreshItems(); updateBounds(); return true
            }
            searchFocused = searchBox.contains(mx, my)
            if (searchFocused) {
                if (mx >= searchBox.right - 22 && query.isNotEmpty()) { query = ""; changedSearch() }
                return true
            }
            if (capes) capeTools.forEachIndexed { i, rect -> if (rect.contains(mx, my)) {
                when (i) {
                    0 -> CapeManager.downloadAsync()
                    1 -> { CapeManager.localDir().mkdirs(); openPath(CapeManager.localDir()) }
                    2 -> CapeManager.unequip()
                }
                return true
            } }
            if (scroll.beginDrag(mx, my, track)) return true
        }
        // Clipped rows and the footer are deliberately not clickable.
        if (!viewport.contains(mx, my)) return super.onMouseClicked(mx, my, button)
        val index = grid.hit(mx, my, if (capes) entries.size else modules.size, scroll.pixels)
            ?: return super.onMouseClicked(mx, my, button)
        if (capes && button == 0) {
            val cape = entries[index]
            if (CapeManager.isEquipped(cape)) CapeManager.unequip() else CapeManager.equip(cape)
            return true
        }
        if (!capes) {
            val module = modules[index]
            val rect = grid.card(index, scroll.pixels)
            if (button == 1 || (button == 0 && UiRect(rect.right - 29, rect.y + 7, 22, 22).contains(mx, my))) {
                MinecraftClient.getInstance().setScreen(ModSettingsGui(module, this)); return true
            }
            if (button == 0) { if (module.unavailableReason != null) feedback = module.unavailableReason!! else { module.toggle(); feedback = "${module.name}: ${if (module.enabled) "enabled" else "disabled"}" }; return true }
        }
        return super.onMouseClicked(mx, my, button)
    }

    override fun onMouseScrolled(mx: Double, my: Double, horizontal: Double, vertical: Double): Boolean {
        updateBounds()
        if ((viewport.contains(mx, my) || track.contains(mx, my)) && scroll.wheel(vertical, horizontal)) return true
        return super.onMouseScrolled(mx, my, horizontal, vertical)
    }

    override fun onMouseDragged(mx: Double, my: Double, button: Int, dx: Double, dy: Double): Boolean =
        (button == 0 && scroll.drag(my, track)) || super.onMouseDragged(mx, my, button, dx, dy)

    override fun onMouseReleased(mx: Double, my: Double, button: Int): Boolean =
        scroll.endDrag() || super.onMouseReleased(mx, my, button)

    override fun onKeyPressed(key: Int, scancode: Int, modifiers: Int): Boolean {
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) { closeGui(); return true }
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true }
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (query.isNotEmpty()) query = query.substring(0, query.offsetByCodePoints(query.length, -1))
                changedSearch(); return true
            }
        } else when (key) {
            GLFW.GLFW_KEY_PAGE_DOWN -> { scroll.moveBy(viewport.height * 0.85); return true }
            GLFW.GLFW_KEY_PAGE_UP -> { scroll.moveBy(-viewport.height * 0.85); return true }
            GLFW.GLFW_KEY_HOME -> { scroll.moveTo(0.0); return true }
            GLFW.GLFW_KEY_END -> { scroll.moveTo(scroll.maximum); return true }
        }
        return super.onKeyPressed(key, scancode, modifiers)
    }

    override fun onCharTyped(text: String): Boolean {
        if (!searchFocused) return super.onCharTyped(text)
        query = (query + text.filterNot { it.isISOControl() }).take(96)
        changedSearch(); return true
    }

    override fun closeGui() {
        com.endiq.client.config.ModulePreferences.save(); MinecraftClient.getInstance().setScreen(parent) }
}
