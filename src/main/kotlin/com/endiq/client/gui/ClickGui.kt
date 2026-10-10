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

/** Responsive module library: persistent category navigation, searchable cards, cosmetics and live settings. */
class ClickGui(private val parent: Screen? = null, initialCosmetics: Boolean = false, private var favoritesOnly: Boolean = false) : ClientScreen("TurtleClient") {
    private data class NavSpec(val label: String, val icon: String, val index: Int, val favorite: Boolean = false)
    private data class NavHit(val bounds: UiRect, val spec: NavSpec)

    private var category = Module.Category.ALL
    private var cosmeticType = CosmeticType.CAPE
    private var cosmetics = initialCosmetics
    private val searchInput = TextInput(96)
    private val query get() = searchInput.text
    private var feedback = ""
    private var searchFocused = false
    private var consumeQuickSearchSlash = false
    private var compact = false
    private val moduleScroll = ScrollState()
    private val cosmeticScroll = ScrollState()
    private val scroll get() = if (cosmetics) cosmeticScroll else moduleScroll
    private var modules = emptyList<Module>()
    private var entries = emptyList<CosmeticManager.CosmeticEntry>()
    private var panel = UiRect(0, 0, 1, 1)
    private var viewport = panel
    private var track = panel
    private var searchBox = panel
    private var clearSearchButton = panel
    private var themeButton = panel
    private var hudEditorButton = panel
    private var toolsButton = panel
    private var modsButton = panel
    private var cosmeticsButton = panel
    private var closeButton = panel
    private var sidebar: UiRect? = null
    private var outfitPanel: UiRect? = null
    private var grid = UiGrid(panel)
    private val tabs = mutableListOf<Pair<UiRect, Int>>()
    private val navHits = mutableListOf<NavHit>()
    private val toolButtons = mutableListOf<Pair<UiRect, String>>()
    private val categories = listOf(Module.Category.ALL, Module.Category.HUD, Module.Category.PVP,
        Module.Category.RENDER, Module.Category.MOVEMENT, Module.Category.UTILITY,
        Module.Category.HYPIXEL, Module.Category.PERFORMANCE)
    private val types = CosmeticType.values().toList()
    private val version = gameVersion()

    override fun init() {
        moduleScroll.endDrag(); cosmeticScroll.endDrag()
        CosmeticManager.reload()
        refreshItems()
        layout()
    }

    private fun categoryLabel(value: Module.Category) = when (value) {
        Module.Category.ALL -> "All"
        Module.Category.MOVEMENT -> "Movement"
        Module.Category.PERFORMANCE -> "Performance"
        else -> value.displayName
    }

    private fun moduleNav(): List<NavSpec> = buildList {
        add(NavSpec("All modules", "grid", 0))
        add(NavSpec("Favorites", "check", -1, favorite = true))
        categories.drop(1).forEachIndexed { offset, value ->
            val icon = when (value) {
                Module.Category.HUD -> "hud"
                Module.Category.PVP -> "pvp"
                Module.Category.RENDER -> "render"
                Module.Category.MOVEMENT -> "movement"
                Module.Category.UTILITY -> "utility"
                Module.Category.HYPIXEL -> "hypixel"
                Module.Category.PERFORMANCE -> "performance"
                else -> "grid"
            }
            add(NavSpec(categoryLabel(value), icon, offset + 1))
        }
    }

    private fun navSpecs(): List<NavSpec> = if (cosmetics) types.mapIndexed { index, type ->
        NavSpec(type.displayName, type.name.lowercase(java.util.Locale.ROOT), index)
    } else moduleNav()

    private fun tabLabel(index: Int): String = if (cosmetics) types[index].displayName else categoryLabel(categories[index])

    private fun refreshItems() {
        modules = ModuleManager.getByCategory(category)
            .filter { it.name.contains(query, true) && (!favoritesOnly || it.favorited) }
        entries = CosmeticManager.getByType(cosmeticType).filter { it.name.contains(query, true) }
    }

    private fun layout() {
        panel = centeredPanel(width, height, 790, 460)
        compact = panel.width < 630 || panel.height < 330
        closeButton = UiRect(panel.right - 29, panel.y + 8, 21, 22)
        cosmeticsButton = UiRect(closeButton.x - if (compact) 72 else 82, closeButton.y, if (compact) 67 else 76, 22)
        modsButton = UiRect(cosmeticsButton.x - if (compact) 49 else 51, closeButton.y, if (compact) 44 else 46, 22)
        hudEditorButton = UiRect(modsButton.x - if (compact) 59 else 84, closeButton.y, if (compact) 54 else 78, 22)
        themeButton = UiRect(hudEditorButton.x - if (compact) 34 else 102, closeButton.y, if (compact) 29 else 96, 22)
        sidebar = null
        outfitPanel = null
        navHits.clear(); tabs.clear(); toolButtons.clear()

        if (compact) layoutCompact() else layoutWide()
        updateBounds()
    }

    private fun layoutCompact() {
        val startX = panel.x + 8
        var x = startX
        var y = panel.y + 39
        val limit = panel.right - 8
        if (cosmetics) {
            types.forEachIndexed { index, type ->
                val label = type.displayName
                val w = (textRenderer.getWidth(label) + 14).coerceAtLeast(39)
                if (x + w > limit && x > startX) { x = startX; y += 22 }
                tabs += UiRect(x, y, w, 19) to index
                x += w + 4
            }
        } else {
            val itemIndexes = categories.indices.toList() + -1
            itemIndexes.forEach { index ->
                val label = if (index == -1) "Favorites" else tabLabel(index)
                val w = (textRenderer.getWidth(label) + 14).coerceAtLeast(39)
                if (x + w > limit && x > startX) { x = startX; y += 22 }
                tabs += UiRect(x, y, w, 19) to index
                x += w + 4
            }
        }
        val toolsWidth = if (cosmetics) 84 else 0
        val rowY = y + 24
        searchBox = UiRect(panel.x + 8, rowY, (panel.width - 24 - toolsWidth).coerceAtLeast(80), 22)
        clearSearchButton = UiRect(searchBox.right - 19, searchBox.y + 1, 18, 20)
        if (cosmetics) {
            toolsButton = UiRect(searchBox.right + 5, rowY, 78, 22)
            toolButtons += UiRect(toolsButton.x, rowY, 22, 22) to "refresh"
            toolButtons += UiRect(toolsButton.x + 27, rowY, 22, 22) to "folder"
            toolButtons += UiRect(toolsButton.x + 54, rowY, 22, 22) to "delete"
        } else toolsButton = UiRect(panel.right - 8, rowY, 0, 22)
        val top = rowY + 28
        val bottom = panel.bottom - 24
        track = UiRect(panel.right - 9, top + 3, 5, (bottom - top - 6).coerceAtLeast(1))
        viewport = UiRect(panel.x + 1, top, (track.x - panel.x - 4).coerceAtLeast(1), (bottom - top).coerceAtLeast(1))
        grid = UiGrid(viewport, minimumCardWidth = if (cosmetics) 116 else 132)
    }

    private fun layoutWide() {
        val left = UiRect(panel.x + 8, panel.y + 47, 142, (panel.height - 73).coerceAtLeast(1))
        sidebar = left
        val specs = navSpecs()
        var rowY = left.y + 29
        val footerTop = left.bottom - 34
        val rowStep = minOf(25, ((footerTop - 4 - rowY) / specs.size).coerceAtLeast(18))
        specs.forEach { spec ->
            val rect = UiRect(left.x + 6, rowY, left.width - 12, (rowStep - 2).coerceAtLeast(16))
            navHits += NavHit(rect, spec)
            rowY += rowStep
        }

        val contentX = left.right + 12
        outfitPanel = if (cosmetics && panel.width >= 720) {
            UiRect(panel.right - 140, panel.y + 80, 132, (panel.bottom - panel.y - 108).coerceAtLeast(1))
        } else null
        val contentRight = (outfitPanel?.x ?: (panel.right - 10)) - 8
        val contentWidth = (contentRight - contentX).coerceAtLeast(120)
        val toolSpace = if (cosmetics) 89 else 0
        searchBox = UiRect(contentX, panel.y + 78, (contentWidth - toolSpace).coerceAtLeast(100), 23)
        clearSearchButton = UiRect(searchBox.right - 20, searchBox.y + 1, 18, 21)
        if (cosmetics) {
            toolsButton = UiRect(searchBox.right + 5, searchBox.y, 84, 23)
            toolButtons += UiRect(toolsButton.x, toolsButton.y, 24, 23) to "refresh"
            toolButtons += UiRect(toolsButton.x + 29, toolsButton.y, 24, 23) to "folder"
            toolButtons += UiRect(toolsButton.x + 58, toolsButton.y, 24, 23) to "delete"
        } else toolsButton = UiRect(contentRight, panel.y + 78, 0, 23)
        val top = searchBox.bottom + 8
        val bottom = panel.bottom - 25
        track = UiRect(contentRight - 6, top + 3, 5, (bottom - top - 6).coerceAtLeast(1))
        viewport = UiRect(contentX, top, (track.x - contentX - 5).coerceAtLeast(1), (bottom - top).coerceAtLeast(1))
        grid = UiGrid(viewport, minimumCardWidth = if (cosmetics) 116 else 138)
    }

    private fun updateBounds() = scroll.update(grid.contentHeight(if (cosmetics) entries.size else modules.size), viewport.height)

    override fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        super.renderGui(ctx, mx, my, delta)
        ctx.fill(0, 0, width, height, (0xB0 shl 24) or (Theme.BACKGROUND and 0x00FFFFFF))
        Theme.panel(ctx, UiRect(panel.x - 3, panel.y + 4, panel.width + 6, panel.height), 0x55000000, 0x11000000, 10)
        Theme.panel(ctx, panel)
        Theme.rounded(ctx, UiRect(panel.x + 1, panel.y + 1, panel.width - 2, 38), Theme.BACKGROUND, 7)
        Theme.divider(ctx, panel.x + 8, panel.y + 39, panel.width - 16)
        drawBrand(ctx)
        Theme.button(ctx, textRenderer, modsButton, "Mods", modsButton.contains(mx.toDouble(), my.toDouble()), !cosmetics)
        Theme.button(ctx, textRenderer, cosmeticsButton, "Cosmetics", cosmeticsButton.contains(mx.toDouble(), my.toDouble()), cosmetics)
        Theme.button(ctx, textRenderer, hudEditorButton, if (compact) "HUD" else "HUD Editor",
            hudEditorButton.contains(mx.toDouble(), my.toDouble()), enabled = true)
        Theme.button(ctx, textRenderer, themeButton, if (compact) "●" else "Theme: ${Theme.paletteName}",
            themeButton.contains(mx.toDouble(), my.toDouble()), enabled = true)
        iconButton(ctx, closeButton, "close", mx, my)

        sidebar?.let { drawSidebar(ctx, it, mx, my) }
        if (compact) drawCompactTabs(ctx, mx, my)
        drawContentHeading(ctx)
        drawSearch(ctx)
        if (cosmetics) {
            toolButtons.forEach { (rect, icon) -> iconButton(ctx, rect, icon, mx, my) }
        }

        updateBounds()
        ctx.enableScissor(viewport.x, viewport.y, viewport.right, viewport.bottom)
        try {
            if (cosmetics) entries.forEachIndexed { i, entry -> drawCosmetic(ctx, entry, grid.card(i, scroll.pixels), mx, my) }
            else modules.forEachIndexed { i, module -> drawModule(ctx, module, grid.card(i, scroll.pixels), mx, my) }
            if ((cosmetics && entries.isEmpty()) || (!cosmetics && modules.isEmpty())) {
                val message = when {
                    query.isNotEmpty() -> "No matches. Try another search."
                    cosmetics -> "No local ${cosmeticType.displayName.lowercase()} files yet."
                    favoritesOnly -> "No favorites yet. Use the star on any module card."
                    else -> "No modules in this category."
                }
                Theme.label(ctx, textRenderer, message, viewport.x + 12, viewport.y + 20, Theme.MUTED, viewport.width - 24)
                if (cosmetics && query.isEmpty()) Theme.label(ctx, textRenderer,
                    "Add PNGs to custom_cosmetics/${cosmeticType.folderName}", viewport.x + 12, viewport.y + 36,
                    Theme.SUBTLE, viewport.width - 24)
            }
        } finally { ctx.disableScissor() }
        Theme.scrollbar(ctx, scroll, track, mx, my)
        outfitPanel?.let { drawCosmeticSummary(ctx, it, mx, my) }
        Theme.divider(ctx, panel.x + 8, panel.bottom - 23, panel.width - 16)
        val hint = if (cosmetics) CosmeticManager.lastMessage
            else feedback.ifEmpty { "Click: toggle  ·  Right-click: settings  ·  /: search" }
        val footer = "MC $version"
        val reserved = textRenderer.getWidth(footer) + 18
        Theme.label(ctx, textRenderer, hint, panel.x + 10, panel.bottom - 15, Theme.MUTED,
            panel.width - reserved - 20)
        Theme.label(ctx, textRenderer, footer, panel.right - reserved + 8, panel.bottom - 15, Theme.SUBTLE)

        if (!cosmetics && viewport.contains(mx.toDouble(), my.toDouble())) {
            grid.hit(mx.toDouble(), my.toDouble(), modules.size, scroll.pixels)?.let { index ->
                val card = grid.card(index, scroll.pixels)
                val star = UiRect(card.right - 50, card.y + 6, 18, 18)
                val gear = UiRect(card.right - 28, card.y + 6, 20, 20)
                when {
                    gear.contains(mx.toDouble(), my.toDouble()) -> Theme.tooltip(ctx, textRenderer, "Open module settings", mx, my, width, height)
                    star.contains(mx.toDouble(), my.toDouble()) -> Theme.tooltip(ctx, textRenderer,
                        if (modules[index].favorited) "Remove from favorites" else "Add to favorites", mx, my, width, height)
                    else -> Theme.tooltip(ctx, textRenderer,
                        com.endiq.client.modules.ModulePresentation.note(modules[index]), mx, my, width, height)
                }
            }
        }
        if (cosmetics) {
            if (toolsButton.contains(mx.toDouble(), my.toDouble())) {
                val labels = listOf("Reload custom PNGs", "Open cosmetic folder", "Unequip all cosmetics")
                labels.getOrNull((mx - toolsButton.x) / if (compact) 27 else 29)?.let {
                    Theme.tooltip(ctx, textRenderer, it, mx, my, width, height)
                }
            }
            outfitPanel?.let { rect ->
                if (rect.contains(mx.toDouble(), my.toDouble())) {
                    if (UiRect(rect.x + 8, rect.bottom - 54, rect.width - 16, 21).contains(mx, my))
                        Theme.tooltip(ctx, textRenderer, "Open the local cosmetic folder", mx, my, width, height)
                    if (UiRect(rect.x + 8, rect.bottom - 28, rect.width - 16, 21).contains(mx, my))
                        Theme.tooltip(ctx, textRenderer, "Unequip every cosmetic", mx, my, width, height)
                }
            }
        }
        if (themeButton.contains(mx.toDouble(), my.toDouble()))
            Theme.tooltip(ctx, textRenderer, "Change TurtleClient color theme · ${Theme.paletteName}", mx, my, width, height)
        if (hudEditorButton.contains(mx.toDouble(), my.toDouble()))
            Theme.tooltip(ctx, textRenderer, "Open the drag-and-drop HUD layout editor", mx, my, width, height)
    }

    private fun drawBrand(ctx: GuiContext) {
        ctx.drawTexture(Theme.LOGO, panel.x + 9, panel.y + 7, 24, 24)
        val available = themeButton.x - panel.x - 47
        if (available >= 88) {
            ctx.drawTexture(Theme.WORDMARK, panel.x + 39, panel.y + 11, minOf(94, available), 18)
        } else {
            Theme.label(ctx, textRenderer, "TURTLE", panel.x + 38, panel.y + 15, Theme.TEXT, available.coerceAtLeast(30))
        }
    }

    private fun activeNav(spec: NavSpec): Boolean = if (cosmetics) {
        spec.index >= 0 && types.getOrNull(spec.index) == cosmeticType
    } else when {
        spec.favorite -> favoritesOnly
        favoritesOnly -> false
        else -> categories.getOrNull(spec.index) == category
    }

    private fun navCount(spec: NavSpec): Int = if (cosmetics) {
        CosmeticManager.getByType(types[spec.index]).size
    } else if (spec.favorite) {
        ModuleManager.modules.count { it.favorited }
    } else {
        ModuleManager.getByCategory(categories[spec.index]).size
    }

    private fun drawSidebar(ctx: GuiContext, rect: UiRect, mx: Int, my: Int) {
        Theme.panel(ctx, rect, Theme.BACKGROUND, Theme.BORDER, 6)
        Theme.label(ctx, textRenderer, if (cosmetics) "COLLECTION" else "LIBRARY", rect.x + 10, rect.y + 9, Theme.SUBTLE)
        for (hit in navHits) {
            val row = hit.bounds
            val selected = activeNav(hit.spec)
            val hovered = row.contains(mx.toDouble(), my.toDouble())
            if (selected) {
                Theme.rounded(ctx, row, Theme.ACTIVE, 5)
                Theme.rounded(ctx, UiRect(row.x, row.y + 5, 2, row.height - 10), Theme.ACCENT, 1)
            } else if (hovered) Theme.rounded(ctx, row, Theme.HOVER, 5)
            val icon = if (cosmetics) types[hit.spec.index].name.lowercase(java.util.Locale.ROOT) else hit.spec.icon
            ctx.drawTexture(Theme.icon(icon), row.x + 8, row.y + 5, 13, 13, if (selected) Theme.ACCENT else Theme.MUTED)
            Theme.label(ctx, textRenderer, hit.spec.label, row.x + 28, row.y + 8,
                if (selected) Theme.TEXT else Theme.MUTED, row.width - 53)
            val count = navCount(hit.spec).toString()
            Theme.label(ctx, textRenderer, count, row.right - textRenderer.getWidth(count) - 7, row.y + 8,
                if (selected) Theme.ACCENT else Theme.SUBTLE)
        }
        val footerY = rect.bottom - 34
        Theme.divider(ctx, rect.x + 9, footerY, rect.width - 18)
        if (cosmetics) {
            Theme.label(ctx, textRenderer, "${CosmeticManager.getEquippedCount()} equipped", rect.x + 10, footerY + 9, Theme.MUTED, rect.width - 20)
        } else {
            val enabled = ModuleManager.getEnabled().count { it.unavailableReason == null }
            Theme.label(ctx, textRenderer, "$enabled enabled", rect.x + 10, footerY + 8, Theme.ACCENT, rect.width - 20)
            Theme.label(ctx, textRenderer, "${ModuleManager.modules.size} modules total", rect.x + 10, footerY + 19, Theme.SUBTLE, rect.width - 20)
        }
    }

    private fun drawCompactTabs(ctx: GuiContext, mx: Int, my: Int) {
        for ((rect, index) in tabs) {
            val selected = if (cosmetics) types.getOrNull(index) == cosmeticType
                else if (index == -1) favoritesOnly else !favoritesOnly && categories.getOrNull(index) == category
            if (selected || rect.contains(mx.toDouble(), my.toDouble()))
                Theme.rounded(ctx, rect, if (selected) Theme.ACTIVE else Theme.CARD, 4)
            val label = if (!cosmetics && index == -1) "Favorites" else tabLabel(index)
            Theme.label(ctx, textRenderer, label, rect.x + 7, rect.y + 6,
                if (selected) Theme.ACCENT else Theme.MUTED, rect.width - 14)
        }
    }

    private fun drawContentHeading(ctx: GuiContext) {
        if (compact) return
        val title = if (cosmetics) cosmeticType.displayName else when {
            favoritesOnly -> "Favorites"
            category == Module.Category.ALL -> "Module library"
            else -> category.displayName
        }
        val count = if (cosmetics) entries.size else modules.size
        val enabled = if (cosmetics) CosmeticManager.getEquippedCount() else modules.count { it.enabled }
        val x = (sidebar?.right ?: panel.x) + 12
        Theme.label(ctx, textRenderer, title, x, panel.y + 51, Theme.TEXT, searchBox.width.coerceAtLeast(100))
        val caption = if (cosmetics) "$count items · $enabled equipped" else "$count modules · $enabled enabled"
        Theme.label(ctx, textRenderer, caption, x, panel.y + 64, Theme.SUBTLE, searchBox.width)
    }

    private fun visibleSearchStart(): Int {
        var start = 0
        val available = (searchBox.width - 47).coerceAtLeast(1)
        while (start < searchInput.cursor && textRenderer.getWidth(query.substring(start, searchInput.cursor)) > available)
            start = query.offsetByCodePoints(start, 1)
        return start
    }

    private fun drawSearch(ctx: GuiContext) {
        Theme.panel(ctx, searchBox, Theme.BACKGROUND, if (searchFocused) Theme.ACCENT else Theme.BORDER, 5)
        ctx.drawTexture(Theme.icon("search"), searchBox.x + 6, searchBox.y + 4, 14, 14, Theme.MUTED)
        if (query.isEmpty() && !searchFocused) {
            Theme.label(ctx, textRenderer, if (cosmetics) "Search cosmetics" else "Search modules",
                searchBox.x + 25, searchBox.y + 7, Theme.SUBTLE, searchBox.width - 45)
            return
        }
        val start = visibleSearchStart()
        val availableRight = if (query.isNotEmpty()) clearSearchButton.x else searchBox.right - 5
        ctx.enableScissor(searchBox.x + 24, searchBox.y + 2, availableRight.coerceAtLeast(searchBox.x + 25), searchBox.bottom - 2)
        try {
            val lo = searchInput.selection.first.coerceIn(start, query.length)
            val hi = searchInput.selection.last.coerceIn(start, query.length)
            if (hi > lo) {
                val left = searchBox.x + 25 + textRenderer.getWidth(query.substring(start, lo))
                val right = searchBox.x + 25 + textRenderer.getWidth(query.substring(start, hi))
                ctx.fill(left, searchBox.y + 5, right, searchBox.bottom - 5, Theme.ACTIVE)
            }
            Theme.label(ctx, textRenderer, query.substring(start), searchBox.x + 25, searchBox.y + 7, Theme.TEXT)
            if (searchFocused && System.currentTimeMillis() / 500 % 2 == 0L) {
                val cx = searchBox.x + 25 + textRenderer.getWidth(query.substring(start, searchInput.cursor))
                ctx.fill(cx, searchBox.y + 5, cx + 1, searchBox.bottom - 5, Theme.ACCENT)
            }
        } finally { ctx.disableScissor() }
        if (query.isNotEmpty()) ctx.drawTexture(Theme.icon("close"), clearSearchButton.x + 4, clearSearchButton.y + 4, 10, 10, Theme.MUTED)
    }

    private fun drawModule(ctx: GuiContext, module: Module, rect: UiRect, mx: Int, my: Int) {
        if (rect.bottom <= viewport.y || rect.y >= viewport.bottom) return
        val hover = viewport.contains(mx.toDouble(), my.toDouble()) && rect.contains(mx.toDouble(), my.toDouble())
        Theme.panel(ctx, rect, if (hover) Theme.HOVER else Theme.CARD,
            if (module.enabled && module.unavailableReason == null) Theme.ACCENT else Theme.BORDER, 6)
        val iconSize = if (rect.height < 68) 18 else 22
        val iconBounds = UiRect(rect.x + 8, rect.y + 8, iconSize + 5, iconSize + 5)
        Theme.rounded(ctx, iconBounds, if (module.enabled && module.unavailableReason == null) Theme.ACTIVE else Theme.BACKGROUND, 5)
        ctx.drawTexture(Theme.moduleIcon(module), iconBounds.x + 2, iconBounds.y + 2, iconSize, iconSize,
            if (module.unavailableReason != null) Theme.SUBTLE else if (module.enabled) Theme.ACCENT else Theme.MUTED)
        val settings = UiRect(rect.right - 27, rect.y + 5, 20, 20)
        val favorite = UiRect(rect.right - 49, rect.y + 6, 18, 18)
        iconButton(ctx, settings, "settings", mx, my)
        Theme.label(ctx, textRenderer, if (module.favorited) "★" else "☆", favorite.x + 2, favorite.y + 2,
            if (module.favorited) Theme.ACCENT else if (favorite.contains(mx.toDouble(), my.toDouble())) Theme.TEXT else Theme.SUBTLE)
        if (rect.height >= 68) {
            Theme.label(ctx, textRenderer, module.name, rect.x + 10, rect.y + 35, Theme.TEXT, rect.width - 20)
            val description = module.description.ifBlank { "No description available." }
            Theme.label(ctx, textRenderer, description, rect.x + 10, rect.y + 47,
                if (module.unavailableReason != null) Theme.SUBTLE else Theme.MUTED, rect.width - 20)
            Theme.divider(ctx, rect.x + 9, rect.bottom - 20, rect.width - 18)
            val state = when {
                module.unavailableReason != null -> "Unavailable"
                module.enabled -> if (module.isNew) "New · Enabled" else "Enabled"
                else -> if (module.isNew) "New · Disabled" else "Disabled"
            }
            Theme.rounded(ctx, UiRect(rect.x + 9, rect.bottom - 16, 6, 6),
                if (module.enabled && module.unavailableReason == null) Theme.ACCENT else Theme.SUBTLE, 3)
            Theme.label(ctx, textRenderer, state, rect.x + 20, rect.bottom - 16,
                if (module.enabled && module.unavailableReason == null) Theme.ACCENT else Theme.SUBTLE, rect.width - 34)
        } else {
            Theme.label(ctx, textRenderer, module.name, rect.x + 10, rect.y + 30, Theme.TEXT, rect.width - 20)
            val state = when {
                module.unavailableReason != null -> "Unavailable"
                module.enabled -> "ON"
                else -> "OFF"
            }
            Theme.label(ctx, textRenderer, state, rect.x + 10, rect.bottom - 14,
                if (module.enabled && module.unavailableReason == null) Theme.ACCENT else Theme.SUBTLE, rect.width - 20)
        }
    }

    private fun drawCosmetic(ctx: GuiContext, entry: CosmeticManager.CosmeticEntry, rect: UiRect, mx: Int, my: Int) {
        if (rect.bottom <= viewport.y || rect.y >= viewport.bottom) return
        val equipped = CosmeticManager.isEquipped(entry)
        val hover = viewport.contains(mx.toDouble(), my.toDouble()) && rect.contains(mx.toDouble(), my.toDouble())
        Theme.panel(ctx, rect, if (hover) Theme.HOVER else Theme.CARD, if (equipped) Theme.ACCENT else Theme.BORDER, 6)
        if (entry.file != null && entry.type == CosmeticType.CAPE) {
            ctx.drawTextureRegion(entry.texture, rect.x + 14, rect.y + 7, 16, 26, 1f, 1f, 10, 16, 64, 32)
        } else ctx.drawTexture(entry.preview, rect.x + 8, rect.y + 6, 28, 28)
        Theme.label(ctx, textRenderer, entry.name, rect.x + 10, rect.y + if (rect.height < 68) 32 else 40, Theme.TEXT, rect.width - 20)
        Theme.label(ctx, textRenderer, if (equipped) "Equipped" else "Click to equip", rect.x + 10, rect.bottom - 16,
            if (equipped) Theme.ACCENT else Theme.SUBTLE, rect.width - 20)
    }

    private fun drawCosmeticSummary(ctx: GuiContext, rect: UiRect, mx: Int, my: Int) {
        Theme.panel(ctx, rect, Theme.BACKGROUND, Theme.BORDER, 6)
        ctx.drawTexture(Theme.LOGO, rect.x + (rect.width - 34) / 2, rect.y + 10, 34, 34)
        Theme.label(ctx, textRenderer, "YOUR OUTFIT", rect.x + 12, rect.y + 52, Theme.ACCENT, rect.width - 24)
        var y = rect.y + 72
        for (type in types) {
            val entry = CosmeticManager.getEquipped(type) ?: continue
            if (y + 12 >= rect.bottom - 64) break
            Theme.label(ctx, textRenderer, "${type.displayName}: ${entry.name}", rect.x + 10, y, Theme.MUTED, rect.width - 20)
            y += 16
        }
        if (rect.height > 168) Theme.label(ctx, textRenderer, "Client-side / F5 to view", rect.x + 10, rect.bottom - 69, Theme.SUBTLE, rect.width - 20)
        if (rect.height > 114) {
            val folder = UiRect(rect.x + 8, rect.bottom - 54, rect.width - 16, 21)
            val clear = UiRect(rect.x + 8, rect.bottom - 28, rect.width - 16, 21)
            Theme.button(ctx, textRenderer, folder, "Open folder", folder.contains(mx.toDouble(), my.toDouble()))
            Theme.button(ctx, textRenderer, clear, "Unequip all", clear.contains(mx.toDouble(), my.toDouble()), danger = true)
        }
    }

    private fun iconButton(ctx: GuiContext, rect: UiRect, icon: String, mx: Int, my: Int) {
        val hover = rect.contains(mx.toDouble(), my.toDouble())
        if (hover) Theme.rounded(ctx, rect, Theme.HOVER, 4)
        ctx.drawTexture(Theme.icon(icon), rect.x + 4, rect.y + 4, rect.width - 8, rect.height - 8,
            if (hover) Theme.ACCENT else Theme.MUTED)
    }

    private fun changeView(value: Boolean) {
        cosmetics = value
        favoritesOnly = false
        searchInput.set("")
        searchFocused = false
        moduleScroll.endDrag(); cosmeticScroll.endDrag()
        refreshItems(); layout()
    }

    private fun changedSearch() {
        scroll.reset(); refreshItems(); updateBounds()
    }

    private fun selectTab(index: Int) {
        if (cosmetics) cosmeticType = types[index]
        else if (index == -1) { favoritesOnly = true; category = Module.Category.ALL }
        else { category = categories[index]; favoritesOnly = false }
        scroll.reset(); refreshItems(); updateBounds()
    }

    private fun searchCaretAt(mx: Double): Int {
        val start = visibleSearchStart()
        var at = start
        val target = mx - (searchBox.x + 25)
        while (at < query.length && textRenderer.getWidth(query.substring(start, query.offsetByCodePoints(at, 1))) < target)
            at = query.offsetByCodePoints(at, 1)
        return at
    }

    private fun toggleFavorite(module: Module) {
        module.favorited = !module.favorited
        feedback = if (module.favorited) "${module.name} added to favorites." else "${module.name} removed from favorites."
        refreshItems(); updateBounds()
        com.endiq.client.config.ModulePreferences.save()
    }

    private fun openSettings(module: Module) {
        MinecraftClient.getInstance().setScreen(ModSettingsGui(module, this))
    }

    private fun onCosmeticTool(index: Int) {
        when (index) {
            0 -> { CosmeticManager.reload(); refreshItems(); updateBounds() }
            1 -> { val folder = java.io.File(CosmeticManager.baseDir(), cosmeticType.folderName); folder.mkdirs(); openPath(folder) }
            2 -> CosmeticManager.unequipAll()
        }
    }

    override fun onMouseClicked(mx: Double, my: Double, button: Int): Boolean {
        updateBounds()
        if (button == 0) {
            if (!searchBox.contains(mx, my)) searchFocused = false
            if (closeButton.contains(mx, my)) { closeGui(); return true }
            if (themeButton.contains(mx, my)) {
                val theme = Theme.cyclePalette()
                feedback = "${theme} theme selected."
                com.endiq.client.config.ModulePreferences.save()
                return true
            }
            if (modsButton.contains(mx, my)) { changeView(false); return true }
            if (cosmeticsButton.contains(mx, my)) { changeView(true); return true }
            if (hudEditorButton.contains(mx, my)) { MinecraftClient.getInstance().setScreen(HudEditorScreen(this)); return true }
            for ((rect, index) in tabs) if (rect.contains(mx, my)) { selectTab(index); return true }
            navHits.firstOrNull { it.bounds.contains(mx, my) }?.let { selectTab(it.spec.index); return true }
            if (searchBox.contains(mx, my)) {
                searchFocused = true
                if (query.isNotEmpty() && clearSearchButton.contains(mx, my)) {
                    searchInput.set(""); changedSearch()
                } else searchInput.place(searchCaretAt(mx))
                return true
            }
            searchFocused = false
            if (cosmetics && toolsButton.contains(mx, my)) {
                val index = ((mx - toolsButton.x) / if (compact) 27.0 else 29.0).toInt()
                onCosmeticTool(index)
                return true
            }
            if (scroll.beginDrag(mx, my, track)) return true
            outfitPanel?.let { rect ->
                if (rect.height > 114 && UiRect(rect.x + 8, rect.bottom - 54, rect.width - 16, 21).contains(mx, my)) {
                    val folder = MinecraftClient.getInstance().runDirectory.resolve("custom_cosmetics/${cosmeticType.folderName}")
                    folder.mkdirs(); openPath(folder); return true
                }
                if (rect.height > 114 && UiRect(rect.x + 8, rect.bottom - 28, rect.width - 16, 21).contains(mx, my)) {
                    CosmeticManager.unequipAll(); return true
                }
            }
        }
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
            val favorite = UiRect(rect.right - 49, rect.y + 6, 18, 18)
            val settings = UiRect(rect.right - 27, rect.y + 5, 20, 20)
            if (button == 0 && favorite.contains(mx, my)) { toggleFavorite(module); return true }
            if (button == 1 || (button == 0 && settings.contains(mx, my))) { openSettings(module); return true }
            if (button == 0) {
                if (module.unavailableReason != null) feedback = module.unavailableReason!!
                else { module.toggle(); feedback = "${module.name}: ${if (module.enabled) "enabled" else "disabled"}" }
                return true
            }
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
        if (consumeQuickSearchSlash && key != GLFW.GLFW_KEY_SLASH) consumeQuickSearchSlash = false
        val ctrl = modifiers and (GLFW.GLFW_MOD_CONTROL or GLFW.GLFW_MOD_SUPER) != 0
        val shift = modifiers and GLFW.GLFW_MOD_SHIFT != 0
        if (searchFocused) {
            when {
                key == GLFW.GLFW_KEY_ESCAPE -> { searchFocused = false; return true }
                key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER -> { searchFocused = false; return true }
                ctrl && key == GLFW.GLFW_KEY_A -> { searchInput.selectAll(); return true }
                ctrl && key == GLFW.GLFW_KEY_V -> { searchInput.insert(clipboardText()); changedSearch(); return true }
                ctrl && key == GLFW.GLFW_KEY_C -> { copyToClipboard(searchInput.selectedText()); return true }
                ctrl && key == GLFW.GLFW_KEY_X -> { copyToClipboard(searchInput.selectedText()); searchInput.insert(""); changedSearch(); return true }
                key == GLFW.GLFW_KEY_BACKSPACE -> { searchInput.backspace(); changedSearch(); return true }
                key == GLFW.GLFW_KEY_DELETE -> { searchInput.delete(); changedSearch(); return true }
                key == GLFW.GLFW_KEY_LEFT -> { searchInput.move(-1, shift); return true }
                key == GLFW.GLFW_KEY_RIGHT -> { searchInput.move(1, shift); return true }
                key == GLFW.GLFW_KEY_HOME -> { searchInput.home(shift); return true }
                key == GLFW.GLFW_KEY_END -> { searchInput.end(shift); return true }
            }
        }
        if ((ctrl && key == GLFW.GLFW_KEY_F) || (!searchFocused && key == GLFW.GLFW_KEY_SLASH)) {
            searchFocused = true
            searchInput.end()
            consumeQuickSearchSlash = key == GLFW.GLFW_KEY_SLASH
            return true
        }
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT || key == GLFW.GLFW_KEY_ESCAPE) { closeGui(); return true }
        if (!searchFocused && (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN)) {
            val step = if (key == GLFW.GLFW_KEY_UP) -1 else 1
            if (cosmetics) {
                cosmeticType = types[Math.floorMod(types.indexOf(cosmeticType) + step, types.size)]
                cosmeticScroll.reset(); refreshItems(); updateBounds()
            } else {
                val order = categories.indices.toList() + -1
                val current = if (favoritesOnly) -1 else categories.indexOf(category)
                val next = order[Math.floorMod(order.indexOf(current) + step, order.size)]
                selectTab(next)
            }
            return true
        }
        if (!searchFocused) when (key) {
            GLFW.GLFW_KEY_PAGE_DOWN -> { scroll.moveBy(viewport.height * 0.85); return true }
            GLFW.GLFW_KEY_PAGE_UP -> { scroll.moveBy(-viewport.height * 0.85); return true }
            GLFW.GLFW_KEY_HOME -> { scroll.moveTo(0.0); return true }
            GLFW.GLFW_KEY_END -> { scroll.moveTo(scroll.maximum); return true }
            GLFW.GLFW_KEY_F -> {
                favoritesOnly = !favoritesOnly
                if (favoritesOnly) category = Module.Category.ALL
                refreshItems(); scroll.reset(); updateBounds(); return true
            }
        }
        return super.onKeyPressed(key, scancode, modifiers)
    }

    override fun onCharTyped(text: String): Boolean {
        if (consumeQuickSearchSlash) { consumeQuickSearchSlash = false; return true }
        if (!searchFocused) return super.onCharTyped(text)
        searchInput.insert(text)
        changedSearch()
        return true
    }

    override fun closeGui() {
        com.endiq.client.config.ModulePreferences.save()
        MinecraftClient.getInstance().setScreen(parent)
    }
}
