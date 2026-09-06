package com.endiq.client.gui

import com.endiq.client.compat.*
import com.endiq.client.cosmetics.CosmeticManager
import com.endiq.client.cosmetics.CosmeticManager.CosmeticType
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import com.endiq.client.gui.settings.ModSettingsGui
import com.endiq.client.modules.Module
import com.endiq.client.modules.ModuleManager
import org.lwjgl.glfw.GLFW

class ClickGui(private val parent: Screen? = null, initialCosmetics: Boolean = false, private var favoritesOnly: Boolean = false) : ClientScreen("TurtleClient") {
    private var category = Module.Category.ALL
    private var cosmeticType = CosmeticType.CAPE
    private var cosmetics = initialCosmetics
    private var query = ""
    private var searchFocused = false
    private val moduleScroll = ScrollState()
    private val cosmeticScroll = ScrollState()
    private val scroll get() = if (cosmetics) cosmeticScroll else moduleScroll
    private var modules = emptyList<Module>()
    private var entries = emptyList<CosmeticManager.CosmeticEntry>()
    private var panel = UiRect(0, 0, 1, 1)
    private var viewport = panel
    private var track = panel
    private var searchBox = panel
    private var toolsButton = panel
    private var modsButton = panel
    private var cosmeticsButton = panel
    private var closeButton = panel
    private var sidebar: UiRect? = null
    private var grid = UiGrid(panel)
    private val tabs = mutableListOf<Pair<UiRect, Int>>()
    private val categories = listOf(Module.Category.ALL, Module.Category.HUD, Module.Category.PVP,
        Module.Category.RENDER, Module.Category.MOVEMENT, Module.Category.UTILITY,
        Module.Category.HYPIXEL, Module.Category.PERFORMANCE)
    private val types = CosmeticType.values().toList()
    private val version = gameVersion()

    override fun init() {
        moduleScroll.endDrag(); cosmeticScroll.endDrag()
        CosmeticManager.reload()
        refreshItems()
        layout() // Preserve scroll when returning from settings; only clamp after a resize.
    }

    private fun tabLabel(index: Int): String = if (cosmetics) types[index].displayName else when (categories[index]) {
        Module.Category.ALL -> if (favoritesOnly) "Favorites" else "All"
        Module.Category.MOVEMENT -> "Move"
        Module.Category.PERFORMANCE -> "Perf"
        else -> categories[index].displayName
    }

    private fun refreshItems() {
        modules = ModuleManager.getByCategory(category).filter { it.name.contains(query, true) && (!favoritesOnly || it.favorited) }
        entries = CosmeticManager.getByType(cosmeticType).filter { it.name.contains(query, true) }
    }

    private fun layout() {
        panel = centeredPanel(width, height, 680, 430)
        closeButton = UiRect(panel.right - 29, panel.y + 8, 21, 22)
        cosmeticsButton = UiRect(closeButton.x - 77, closeButton.y, 72, 22)
        modsButton = UiRect(cosmeticsButton.x - 47, closeButton.y, 42, 22)
        var x = panel.x + 8
        var y = panel.y + 40
        tabs.clear()
        for (i in 0 until if (cosmetics) types.size else categories.size) {
            val w = textRenderer.getWidth(tabLabel(i)) + 14
            if (x + w > panel.right - 8 && x > panel.x + 8) { x = panel.x + 8; y += 22 }
            tabs.add(UiRect(x, y, w, 19) to i)
            x += w + 4
        }
        val toolsY = y + 26
        toolsButton = UiRect(panel.right - 82, toolsY, 74, 22)
        searchBox = UiRect(panel.x + 8, toolsY, toolsButton.x - panel.x - 14, 22)
        val top = toolsY + 28
        val bottom = panel.bottom - 24
        val sidebarWidth = if (cosmetics && panel.width >= 520) 142 else 0
        sidebar = if (sidebarWidth > 0) UiRect(panel.right - sidebarWidth - 8, top, sidebarWidth, (bottom - top).coerceAtLeast(1)) else null
        track = UiRect((sidebar?.x ?: (panel.right - 3)) - 8, top + 4, 6, (bottom - top - 8).coerceAtLeast(1))
        viewport = UiRect(panel.x + 1, top, (track.x - panel.x - 3).coerceAtLeast(1), (bottom - top).coerceAtLeast(1))
        grid = UiGrid(viewport, minimumCardWidth = if (cosmetics) 116 else 132)
        updateBounds()
    }

    private fun updateBounds() = scroll.update(grid.contentHeight(if (cosmetics) entries.size else modules.size), viewport.height)

    override fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        ctx.fill(0, 0, width, height, 0xB0071013.toInt())
        Theme.panel(ctx, UiRect(panel.x - 3, panel.y + 4, panel.width + 6, panel.height), 0x55000000, 0x11000000, 10)
        Theme.panel(ctx, panel)
        ctx.drawTexture(Theme.LOGO, panel.x + 9, panel.y + 8, 25, 25)
        if (modsButton.x - panel.x > 135) {
            ctx.drawTexture(Theme.WORDMARK, panel.x + 40, panel.y + 12, 96, 18)
        } else Theme.label(ctx, textRenderer, "TURTLE", panel.x + 39, panel.y + 15, Theme.TEXT, modsButton.x - panel.x - 44)
        Theme.button(ctx, textRenderer, modsButton, "Mods", modsButton.contains(mx.toDouble(), my.toDouble()), !cosmetics)
        Theme.button(ctx, textRenderer, cosmeticsButton, "Cosmetics", cosmeticsButton.contains(mx.toDouble(), my.toDouble()), cosmetics)
        iconButton(ctx, closeButton, "close", mx, my)
        for ((rect, index) in tabs) {
            val selected = if (cosmetics) types[index] == cosmeticType else categories[index] == category
            if (selected || rect.contains(mx.toDouble(), my.toDouble())) Theme.rounded(ctx, rect, if (selected) Theme.ACTIVE else Theme.CARD, 4)
            Theme.label(ctx, textRenderer, tabLabel(index), rect.x + 7, rect.y + 6, if (selected) Theme.ACCENT else Theme.MUTED)
        }
        Theme.panel(ctx, searchBox, Theme.BACKGROUND, if (searchFocused) Theme.ACCENT else Theme.BORDER, 5)
        ctx.drawTexture(Theme.icon("search"), searchBox.x + 6, searchBox.y + 4, 14, 14, Theme.MUTED)
        val placeholder = if (cosmetics) "Search cosmetics" else "Search modules"
        val caret = if (searchFocused && System.currentTimeMillis() / 500 % 2 == 0L) "_" else ""
        Theme.label(ctx, textRenderer, if (query.isEmpty() && !searchFocused) placeholder else query + caret,
            searchBox.x + 25, searchBox.y + 7, if (query.isEmpty()) Theme.MUTED else Theme.TEXT, searchBox.width - 44)
        if (query.isNotEmpty()) ctx.drawTexture(Theme.icon("close"), searchBox.right - 18, searchBox.y + 5, 12, 12, Theme.MUTED)
        if (cosmetics) Theme.button(ctx, textRenderer, toolsButton, "Reload files", toolsButton.contains(mx.toDouble(), my.toDouble()))
        else Theme.label(ctx, textRenderer, "${modules.size} modules", toolsButton.x + 4, toolsButton.y + 7, Theme.MUTED, toolsButton.width)

        updateBounds()
        ctx.enableScissor(viewport.x, viewport.y, viewport.right, viewport.bottom)
        try {
            if (cosmetics) entries.forEachIndexed { i, entry -> drawCosmetic(ctx, entry, grid.card(i, scroll.pixels), mx, my) }
            else modules.forEachIndexed { i, module -> drawModule(ctx, module, grid.card(i, scroll.pixels), mx, my) }
            if ((cosmetics && entries.isEmpty()) || (!cosmetics && modules.isEmpty())) {
                val message = if (query.isNotEmpty()) "No matches. Try another search." else if (cosmetics) "No local ${cosmeticType.displayName.lowercase()} files yet." else "No modules in this category."
                Theme.label(ctx, textRenderer, message, viewport.x + 12, viewport.y + 20, Theme.MUTED, viewport.width - 24)
                if (cosmetics && query.isEmpty()) Theme.label(ctx, textRenderer, "Add PNGs to custom_cosmetics/${cosmeticType.folderName}", viewport.x + 12, viewport.y + 36, Theme.SUBTLE, viewport.width - 24)
            }
        } finally { ctx.disableScissor() }
        Theme.scrollbar(ctx, scroll, track, mx, my)
        sidebar?.let { drawCosmeticSummary(ctx, it, mx, my) }
        ctx.fill(panel.x + 8, panel.bottom - 24, panel.right - 8, panel.bottom - 23, Theme.BORDER)
        val hint = if (cosmetics) "Click to equip  /  Wheel to scroll" else "Right-click: settings  /  Wheel: scroll"
        val footer = "MC $version"
        val reserved = textRenderer.getWidth(footer) + 20
        Theme.label(ctx, textRenderer, hint, panel.x + 10, panel.bottom - 15, Theme.MUTED, panel.width - reserved - 20)
        Theme.label(ctx, textRenderer, footer, panel.right - reserved + 8, panel.bottom - 15, Theme.SUBTLE)
        super.renderGui(ctx, mx, my, delta)
    }

    private fun drawModule(ctx: GuiContext, module: Module, rect: UiRect, mx: Int, my: Int) {
        if (rect.bottom <= viewport.y || rect.y >= viewport.bottom) return
        val hover = viewport.contains(mx.toDouble(), my.toDouble()) && rect.contains(mx.toDouble(), my.toDouble())
        Theme.panel(ctx, rect, if (hover) Theme.HOVER else Theme.CARD, if (module.enabled) Theme.ACTIVE else Theme.BORDER, 6)
        val categoryIcon = if (module.category == Module.Category.ALL) "grid" else module.category.name.lowercase()
        ctx.drawTexture(Theme.icon(categoryIcon), rect.x + 10, rect.y + 9, 18, 18, if (module.enabled) Theme.ACCENT else Theme.MUTED)
        if (module.isNew) Theme.label(ctx, textRenderer, "NEW", rect.x + 35, rect.y + 14, Theme.ACCENT)
        val settings = UiRect(rect.right - 29, rect.y + 7, 22, 22)
        iconButton(ctx, settings, "settings", mx, my)
        val nameY = if (rect.height < 68) 30 else 34
        Theme.label(ctx, textRenderer, module.name, rect.x + 10, rect.y + nameY, Theme.TEXT, rect.width - 20)
        if (rect.height >= 78) Theme.label(ctx, textRenderer, module.description, rect.x + 10, rect.y + 47, Theme.MUTED, rect.width - 20)
        Theme.label(ctx, textRenderer, if (module.enabled) "Enabled" else "Disabled", rect.x + 10, rect.bottom - 15,
            if (module.enabled) Theme.ACCENT else Theme.SUBTLE, rect.width - 52)
        val switch = UiRect(rect.right - 38, rect.bottom - 21, 28, 13)
        Theme.rounded(ctx, switch, if (module.enabled) Theme.ACTIVE else Theme.BACKGROUND, 6)
        Theme.rounded(ctx, UiRect(switch.x + if (module.enabled) 16 else 3, switch.y + 2, 9, 9), if (module.enabled) Theme.ACCENT else Theme.SUBTLE, 4)
    }

    private fun drawCosmetic(ctx: GuiContext, entry: CosmeticManager.CosmeticEntry, rect: UiRect, mx: Int, my: Int) {
        if (rect.bottom <= viewport.y || rect.y >= viewport.bottom) return
        val equipped = CosmeticManager.isEquipped(entry)
        val hover = viewport.contains(mx.toDouble(), my.toDouble()) && rect.contains(mx.toDouble(), my.toDouble())
        Theme.panel(ctx, rect, if (hover) Theme.HOVER else Theme.CARD, if (equipped) Theme.ACCENT else Theme.BORDER, 6)
        ctx.drawTexture(Theme.icon(entry.type.name.lowercase()), rect.x + 10, rect.y + 10, 22, 22, if (equipped) Theme.ACCENT else Theme.MUTED)
        Theme.label(ctx, textRenderer, entry.name, rect.x + 10, rect.y + if (rect.height < 68) 32 else 40, Theme.TEXT, rect.width - 20)
        Theme.label(ctx, textRenderer, if (equipped) "Equipped" else "Click to equip", rect.x + 10, rect.bottom - 16, if (equipped) Theme.ACCENT else Theme.SUBTLE, rect.width - 20)
    }

    private fun drawCosmeticSummary(ctx: GuiContext, rect: UiRect, mx: Int, my: Int) {
        Theme.panel(ctx, rect, Theme.BACKGROUND)
        ctx.drawTexture(Theme.LOGO, rect.x + (rect.width - 34) / 2, rect.y + 10, 34, 34)
        Theme.label(ctx, textRenderer, "LOCAL COSMETICS", rect.x + 12, rect.y + 52, Theme.ACCENT, rect.width - 24)
        var y = rect.y + 72
        for (type in types) {
            val entry = CosmeticManager.getEquipped(type) ?: continue
            if (y + 12 >= rect.bottom - 64) break
            Theme.label(ctx, textRenderer, "${type.displayName}: ${entry.name}", rect.x + 10, y, Theme.MUTED, rect.width - 20)
            y += 16
        }
        if (rect.height > 168) Theme.label(ctx, textRenderer, "In-world preview: not yet", rect.x + 10, rect.bottom - 69, Theme.SUBTLE, rect.width - 20)
        if (rect.height > 114) {
            val folder = UiRect(rect.x + 8, rect.bottom - 54, rect.width - 16, 21)
            val clear = UiRect(rect.x + 8, rect.bottom - 28, rect.width - 16, 21)
            Theme.button(ctx, textRenderer, folder, "Open folder", folder.contains(mx.toDouble(), my.toDouble()))
            Theme.button(ctx, textRenderer, clear, "Unequip all", clear.contains(mx.toDouble(), my.toDouble()))
        }
    }

    private fun iconButton(ctx: GuiContext, rect: UiRect, icon: String, mx: Int, my: Int) {
        val hover = rect.contains(mx.toDouble(), my.toDouble())
        if (hover) Theme.rounded(ctx, rect, Theme.HOVER, 4)
        ctx.drawTexture(Theme.icon(icon), rect.x + 4, rect.y + 4, rect.width - 8, rect.height - 8, if (hover) Theme.ACCENT else Theme.MUTED)
    }

    private fun changeView(value: Boolean) {
        cosmetics = value; favoritesOnly = false; query = ""; searchFocused = false
        moduleScroll.endDrag(); cosmeticScroll.endDrag()
        refreshItems(); layout()
    }

    private fun changedSearch() { scroll.reset(); refreshItems(); updateBounds() }

    override fun onMouseClicked(mx: Double, my: Double, button: Int): Boolean {
        updateBounds()
        if (button == 0) {
            if (closeButton.contains(mx, my)) { closeGui(); return true }
            if (modsButton.contains(mx, my)) { changeView(false); return true }
            if (cosmeticsButton.contains(mx, my)) { changeView(true); return true }
            for ((rect, index) in tabs) if (rect.contains(mx, my)) {
                if (cosmetics) cosmeticType = types[index] else category = categories[index]
                scroll.reset(); refreshItems(); updateBounds(); return true
            }
            searchFocused = searchBox.contains(mx, my)
            if (searchFocused) {
                if (mx >= searchBox.right - 22 && query.isNotEmpty()) { query = ""; changedSearch() }
                return true
            }
            if (cosmetics && toolsButton.contains(mx, my)) { CosmeticManager.reload(); refreshItems(); updateBounds(); return true }
            if (scroll.beginDrag(mx, my, track)) return true
            sidebar?.let { rect ->
                if (rect.height > 114 && UiRect(rect.x + 8, rect.bottom - 54, rect.width - 16, 21).contains(mx, my)) {
                    val folder = MinecraftClient.getInstance().runDirectory.resolve("custom_cosmetics/${cosmeticType.folderName}")
                    folder.mkdirs(); openPath(folder); return true
                }
                if (rect.height > 114 && UiRect(rect.x + 8, rect.bottom - 28, rect.width - 16, 21).contains(mx, my)) {
                    types.forEach { CosmeticManager.unequip(it) }; return true
                }
            }
        }
        // Clipped rows and the footer are deliberately not clickable.
        if (!viewport.contains(mx, my)) return super.onMouseClicked(mx, my, button)
        val index = grid.hit(mx, my, if (cosmetics) entries.size else modules.size, scroll.pixels)
            ?: return super.onMouseClicked(mx, my, button)
        if (cosmetics && button == 0) {
            val entry = entries[index]
            if (CosmeticManager.isEquipped(entry)) CosmeticManager.unequip(entry.type) else CosmeticManager.equip(entry)
            return true
        }
        if (!cosmetics) {
            val module = modules[index]
            val rect = grid.card(index, scroll.pixels)
            if (button == 1 || (button == 0 && UiRect(rect.right - 29, rect.y + 7, 22, 22).contains(mx, my))) {
                MinecraftClient.getInstance().setScreen(ModSettingsGui(module, this)); return true
            }
            if (button == 0) { module.toggle(); return true }
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

    override fun closeGui() { MinecraftClient.getInstance().setScreen(parent) }
}
