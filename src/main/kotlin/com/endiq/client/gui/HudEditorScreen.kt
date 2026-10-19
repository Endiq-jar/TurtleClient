package com.endiq.client.gui

import com.endiq.client.compat.*
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import com.endiq.client.modules.Module
import com.endiq.client.modules.ModuleManager
import com.endiq.client.modules.SliderSetting
import com.endiq.client.modules.impl.hud.*
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/** Direct manipulation editor for the client HUD. Positions remain percentage-based across resolutions. */
class HudEditorScreen(private val parent: Screen? = null) : ClientScreen("Turtle HUD Editor") {
    private enum class XAnchor { LEFT, CENTER, RIGHT, TIMER }
    private enum class YAnchor { TOP, TIMER }
    private data class Position(val x: SliderSetting, val y: SliderSetting)
    private data class Widget(
        val module: Module, val position: Position, val rect: UiRect, val preview: String,
        val scale: Float, val contentWidth: Float, val xAnchor: XAnchor, val yAnchor: YAnchor
    )

    private val search = TextInput(64)
    private val scroll = ScrollState()
    private val startPositions = linkedMapOf<Module, Pair<Float, Float>>()
    private var hasSnapshot = false
    private var candidates = emptyList<Module>()
    private var selected: Module? = null
    private var dragging: Widget? = null
    private var grabX = 0.0
    private var grabY = 0.0
    private var panel = UiRect(0, 0, 1, 1)
    private var viewport = panel
    private var track = panel
    private var searchBox = panel
    private var closeButton = panel
    private var revertButton = panel
    private var doneButton = panel
    private var snapButton = panel
    private var gridButton = panel
    private var snapToGrid = true
    private var gridSize = 4
    private var hideButton = panel
    private var sidebarHidden = false
    private var compactToolbar = false
    private var searchFocused = false
    private var feedback = "Drag a HUD item to move it. Right Alt toggles this editor."

    override fun init() {
        candidates = ModuleManager.modules.filter { position(it) != null && it.unavailableReason == null }
            .sortedWith(compareBy<Module>({ it.category.ordinal }, { it.name.lowercase(java.util.Locale.ROOT) }))
        if (!hasSnapshot) {
            startPositions.clear()
            candidates.forEach { module ->
                val p = position(module) ?: return@forEach
                startPositions[module] = p.x.value to p.y.value
            }
            hasSnapshot = true
        }
        selected = selected?.takeIf { it in candidates } ?: candidates.firstOrNull()
        layout()
        updateBounds()
    }

    private fun layout() {
        val sidebarWidth = if (sidebarHidden) 0 else minOf(204, (width * 0.42).toInt().coerceAtLeast(148),
            (width - 20).coerceAtLeast(1))
        panel = UiRect(10, 10, sidebarWidth, (height - 20).coerceAtLeast(1))
        val top = 10
        val right = width - 10
        compactToolbar = width < 560
        closeButton = UiRect(right - 25, top, 23, 23)
        // Bottom toolbar (06): Snap / Grid / Revert / Done, always in one place.
        var tx = ((width - 348) / 2).coerceAtLeast(8)
        val ty = height - 40
        snapButton = UiRect(tx, ty, 104, 23); tx += 110
        gridButton = UiRect(tx, ty, 76, 23); tx += 82
        revertButton = UiRect(tx, ty, 76, 23); tx += 82
        doneButton = UiRect(tx, ty, 68, 23)
        hideButton = UiRect(if (sidebarHidden) panel.x else panel.right - 28, top, 23, 23)
        if (!sidebarHidden) {
            searchBox = UiRect(panel.x + 8, panel.y + 38, panel.width - 16, 22)
            viewport = UiRect(panel.x + 6, searchBox.bottom + 7, panel.width - 20,
                (panel.height - 105).coerceAtLeast(1))
            track = UiRect(panel.right - 11, viewport.y + 2, 5, (viewport.height - 4).coerceAtLeast(1))
        } else {
            searchBox = UiRect(0, 0, 0, 0)
            viewport = UiRect(0, 0, 0, 0)
            track = UiRect(0, 0, 0, 0)
        }
    }

    private fun position(module: Module): Position? {
        val sliders = module.visibleSettings.filterIsInstance<SliderSetting>()
        val x = sliders.firstOrNull { it.name == "Position X" } ?: return null
        val y = sliders.firstOrNull { it.name == "Position Y" } ?: return null
        return Position(x, y)
    }

    private fun visibleModules() = candidates.filter { it.name.contains(search.text, ignoreCase = true) }

    private fun updateBounds() {
        val rowHeight = 29
        scroll.update(visibleModules().size * rowHeight + 8, viewport.height)
    }

    private fun setting(module: Module, name: String) = module.visibleSettings.firstOrNull { it.name == name }
    private fun slider(module: Module, name: String, default: Float) =
        (setting(module, name) as? SliderSetting)?.value ?: default
    private fun bool(module: Module, name: String, default: Boolean) =
        (setting(module, name) as? com.endiq.client.modules.BoolSetting)?.value ?: default

    private fun previewText(module: Module): String = when (module.javaClass.simpleName) {
        "FpsModule" -> "FPS: ${clientFps()}"
        "CpsModule" -> "LMB 4  ·  RMB 2"
        "CoordinatesModule" -> runCatching { (module as com.endiq.client.modules.impl.hud.CoordinatesModule).getText() }
            .getOrDefault("XYZ: 128, 64, -32").ifBlank { "XYZ: 128, 64, -32" }
        "PingModule" -> "Ping: ${clientPing() ?: 42}ms"
        "SpeedHudModule" -> "Speed: 4.20 m/s"
        "MemoryHudModule" -> "RAM: 1024/4096 MB"
        "ClockHudModule" -> "Time: 12:34"
        "DayCounterModule" -> "Day: 12"
        "HealthDisplayModule" -> "HP: 20.0/20"
        "DirectionHudModule" -> "N 0°"
        "ServerAddressModule" -> "Server: example.net"
        "PackDisplayModule" -> "Pack: Turtle Default"
        "ReachDisplayModule" -> "Reach: 3.00m"
        "ToggleSprintModule" -> "Sprinting"
        "BlockIndicatorModule" -> "Diamond Ore"
        "AttackIndicatorModule" -> "Attack cooldown"
        "ArmorStatusModule" -> "Armor 100%  92%"
        "ArmorBarModule" -> "Armor"
        "KeystrokesModule" -> "W  A  S  D  SPACE"
        "PotionStatusModule" -> "Speed II  1:24"
        "UhcOverlayModule" -> "HP: 20.0  SAT: 5.0"
        "PvpInfoModule" -> "Nearby: Alex  18hp  4.2m"
        "ComboCounterModule" -> "3x COMBO"
        "NetGraphModule" -> "PING HISTORY"
        "TeamViewModule" -> "Team: Alex  12m"
        "TimersModule" -> "Timer 0:45 / running"
        "WaypointsModule" -> "Spawn 42m"
        "PopupEventsModule" -> "Turtle notification"
        else -> module.name
    }

    private fun xAnchor(module: Module): XAnchor = when (module.javaClass.simpleName) {
        "ArmorBarModule", "AttackIndicatorModule", "BlockIndicatorModule", "ComboCounterModule",
        "UhcOverlayModule", "PopupEventsModule" -> XAnchor.CENTER
        "PotionStatusModule" -> XAnchor.RIGHT
        "TimersModule" -> XAnchor.TIMER
        else -> when ((setting(module, "Alignment") as? com.endiq.client.modules.DropdownSetting)?.selected ?: 0) {
            1 -> XAnchor.CENTER
            2 -> XAnchor.RIGHT
            else -> XAnchor.LEFT
        }
    }

    private fun yAnchor(module: Module): YAnchor = when (module.javaClass.simpleName) {
        "TimersModule" -> YAnchor.TIMER
        else -> YAnchor.TOP
    }

    private fun makeWidget(module: Module): Widget? {
        val position = position(module) ?: return null
        val name = module.javaClass.simpleName
        val baseScale = slider(module, "Scale", 1f).coerceIn(.3f, 4f)
        val scale = if (name == "ArmorStatusModule")
            baseScale.coerceIn(.5f, 3f) * slider(module, "Icon Size", 16f).coerceIn(8f, 32f) / 16f
        else baseScale
        val preview = previewText(module).ifBlank { module.name }
        val font = textRenderer
        val previewLines = preview.split('\n')
        val textWidth = previewLines.maxOf { font.getWidth(it) }.toFloat()
        val hudTextWidth = when (module) {
            is FpsModule -> if (module.showGraph.value) maxOf(textWidth, module.history.size.toFloat()) else textWidth
            is CpsModule -> if (module.showBar.value) maxOf(textWidth, module.barWidth.value) else textWidth
            is MemoryHudModule -> if (module.showBar.value) maxOf(textWidth, module.barWidth.value) else textWidth
            is PingModule -> if (module.showBar.value) textWidth + 16f else textWidth
            else -> textWidth
        }
        val contentWidth = when (name) {
            "NetGraphModule" -> slider(module, "Width", 60f).coerceAtLeast(20f)
            "AttackIndicatorModule", "ArmorBarModule" -> slider(module, "Bar Width", 80f).coerceAtLeast(20f)
            "ArmorStatusModule" -> 76f
            "KeystrokesModule" -> {
                val keySize = slider(module, "Key Size", 16f).toInt().coerceIn(8, 40)
                val gap = slider(module, "Key Gap", 2f).toInt().coerceIn(0, 8)
                (keySize * 3 + gap * 2 + if (bool(module, "Show Space", true)) keySize + gap else 0).toFloat()
            }
            else -> (hudTextWidth + 10f).coerceAtLeast(36f)
        }
        val hudExtraHeight = when (module) {
            is FpsModule -> if (module.showGraph.value) module.graphHeight.value + 2f else 0f
            is CpsModule -> if (module.showBar.value) 5f else 0f
            is MemoryHudModule -> if (module.showBar.value) 5f else 0f
            else -> 0f
        }
        val contentHeight = when (name) {
            "NetGraphModule" -> slider(module, "Height", 20f).coerceAtLeast(10f)
            "AttackIndicatorModule" -> slider(module, "Bar Height", 5f).coerceAtLeast(3f) + 8f
            "ArmorBarModule" -> slider(module, "Bar Height", 4f).coerceAtLeast(2f)
            "ArmorStatusModule" -> 30f
            "KeystrokesModule" -> {
                val keySize = slider(module, "Key Size", 16f).toInt().coerceIn(8, 40)
                val gap = slider(module, "Key Gap", 2f).toInt().coerceIn(0, 8)
                (keySize * 3 + gap * 2).toFloat()
            }
            "PotionStatusModule" -> 36f
            else -> (previewLines.size * 10 + hudExtraHeight).coerceAtLeast(10f)
        }
        val pixelWidth = (contentWidth * scale).roundToInt().coerceAtLeast(24)
        val pixelHeight = (contentHeight * scale).roundToInt().coerceAtLeast(14)
        val xAnchor = xAnchor(module)
        val yAnchor = yAnchor(module)
        val rawAnchorX = width * position.x.value / 100f
        val rawAnchorY = height * position.y.value / 100f
        val left = when (xAnchor) {
            XAnchor.LEFT -> rawAnchorX
            XAnchor.CENTER -> rawAnchorX - pixelWidth / 2f
            XAnchor.RIGHT -> rawAnchorX - pixelWidth
            XAnchor.TIMER -> position.x.value / 100f * (width - contentWidth * scale - 8f).coerceAtLeast(0f) + 4f
        }.coerceIn(0f, (width - pixelWidth).coerceAtLeast(0).toFloat()).roundToInt()
        val top = when (yAnchor) {
            YAnchor.TOP -> rawAnchorY
            YAnchor.TIMER -> position.y.value / 100f * (height - 14f * scale).coerceAtLeast(0f) + 3f
        }.coerceIn(0f, (height - pixelHeight).coerceAtLeast(0).toFloat()).roundToInt()
        return Widget(module, position, UiRect(left, top, pixelWidth, pixelHeight), preview, scale,
            contentWidth, xAnchor, yAnchor)
    }

    private fun widgets(): List<Widget> = candidates.mapNotNull(::makeWidget)
        .filter { it.module.enabled || it.module == selected }

    override fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        super.renderGui(ctx, mx, my, delta)
        updateBounds()
        ctx.fill(0, 0, width, height, 0x18000000)

        if (dragging != null) {
            // Alignment guides while dragging (06): two 1px fills, no texture.
            ctx.fill(width / 2, 0, width / 2 + 1, height, 0x5538D2A8)
            ctx.fill(0, height / 2, width, height / 2 + 1, 0x5538D2A8)
        }
        val widgetList = widgets()
        widgetList.forEach { widget -> drawWidget(ctx, widget, mx, my) }
        drawTopBar(ctx, mx, my)
        if (!sidebarHidden) drawSidebar(ctx, mx, my)

        drawBottomBar(ctx, mx, my)
        selected?.let { module ->
            val info = "${module.name}  ·  ${if (module.enabled) "enabled" else "preview only"}"
            Theme.label(ctx, textRenderer, info, panel.right + 14, height - 56, Theme.MUTED, width - panel.right - 28)
        }
    }

    private fun drawWidget(ctx: GuiContext, widget: Widget, mx: Int, my: Int) {
        val bounds = widget.rect
        val selectedWidget = widget.module == selected
        val hovered = bounds.contains(mx.toDouble(), my.toDouble())
        val fill = if (widget.module.enabled) Theme.ACTIVE else Theme.CARD
        val edge = if (selectedWidget || hovered) Theme.ACCENT else Theme.BORDER
        Theme.panel(ctx, bounds, fill, edge, 5)
        if (selectedWidget) Theme.dashed(ctx, bounds.inset(-2), Theme.ACCENT)
        else if (hovered) Theme.dashed(ctx, bounds, Theme.ACCENT)
        val color = if (widget.module.enabled) Theme.TEXT else Theme.MUTED
        Theme.label(ctx, textRenderer, widget.preview, bounds.x + 5, bounds.y + (bounds.height - 8) / 2,
            color, bounds.width - 10)
        if (selectedWidget || hovered) {
            val name = widget.module.name
            Theme.chip(ctx, textRenderer, UiRect(bounds.x, (bounds.y - 19).coerceAtLeast(0),
                (textRenderer.getWidth(name) + 14).coerceAtMost(width), 16), name, selectedWidget)
            if (selectedWidget) {
                Theme.rounded(ctx, UiRect(bounds.right - 4, bounds.bottom - 4, 8, 8), Theme.ACCENT, 4)
            }
        }
    }

    private fun drawTopBar(ctx: GuiContext, mx: Int, my: Int) {
        val bar = UiRect(if (sidebarHidden) 42 else panel.right + 8, 10,
            (width - (if (sidebarHidden) 84 else panel.right + 28)).coerceAtLeast(90), 27)
        Theme.panel(ctx, bar, Theme.PANEL, Theme.BORDER, 6)
        ctx.drawTexture(Theme.LOGO, bar.x + 7, bar.y + 4, 18, 18)
        Theme.label(ctx, textRenderer, if (compactToolbar) "HUD" else "HUD EDITOR", bar.x + 31, bar.y + 9, Theme.TEXT,
            if (compactToolbar) bar.width - 62 else Int.MAX_VALUE)
        Theme.button(ctx, textRenderer, hideButton, if (sidebarHidden) "+" else "-",
            hideButton.contains(mx.toDouble(), my.toDouble()))
        iconButton(ctx, closeButton, "close", mx, my)
    }

    private fun drawBottomBar(ctx: GuiContext, mx: Int, my: Int) {
        val bar = UiRect(snapButton.x - 8, snapButton.y - 6, (doneButton.right - snapButton.x) + 16, 35)
        Theme.panel(ctx, bar, Theme.PANEL, Theme.BORDER, 6)
        Theme.button(ctx, textRenderer, snapButton, if (snapToGrid) "Snap: On" else "Snap: Off",
            snapButton.contains(mx.toDouble(), my.toDouble()), snapToGrid)
        Theme.button(ctx, textRenderer, gridButton, "Grid: ${gridSize}px",
            gridButton.contains(mx.toDouble(), my.toDouble()))
        Theme.button(ctx, textRenderer, revertButton, "Revert", revertButton.contains(mx.toDouble(), my.toDouble()))
        Theme.button(ctx, textRenderer, doneButton, "Done", doneButton.contains(mx.toDouble(), my.toDouble()), true)
        if (revertButton.contains(mx.toDouble(), my.toDouble()))
            Theme.tooltip(ctx, textRenderer, "Revert HUD positions", mx, my, width, height)
        if (doneButton.contains(mx.toDouble(), my.toDouble()))
            Theme.tooltip(ctx, textRenderer, "Save and close", mx, my, width, height)
    }

    private fun drawSidebar(ctx: GuiContext, mx: Int, my: Int) {
        Theme.panel(ctx, panel, Theme.PANEL, Theme.BORDER, 7)
        ctx.drawTexture(Theme.LOGO, panel.x + 10, panel.y + 8, 22, 22)
        Theme.label(ctx, textRenderer, "TURTLE HUD", panel.x + 38, panel.y + 12, Theme.TEXT, panel.width - 46)
        Theme.label(ctx, textRenderer, "DRAG TO REPOSITION", panel.x + 10, panel.y + 27, Theme.SUBTLE, panel.width - 20)
        Theme.panel(ctx, searchBox, Theme.BACKGROUND, if (searchFocused) Theme.ACCENT else Theme.BORDER, 5)
        ctx.drawTexture(Theme.icon("search"), searchBox.x + 5, searchBox.y + 4, 13, 13, Theme.MUTED)
        Theme.label(ctx, textRenderer, search.text.ifEmpty { "Find a HUD item" }, searchBox.x + 24,
            searchBox.y + 7, if (search.text.isEmpty()) Theme.SUBTLE else Theme.TEXT, searchBox.width - 30)

        ctx.enableScissor(viewport.x, viewport.y, viewport.right, viewport.bottom)
        try {
            visibleModules().forEachIndexed { index, module ->
                val y = viewport.y + 4 + index * 29 - scroll.pixels
                val row = UiRect(viewport.x + 1, y, viewport.width - 3, 25)
                if (row.bottom <= viewport.y || row.y >= viewport.bottom) return@forEachIndexed
                val isSelected = module == selected
                if (isSelected || row.contains(mx.toDouble(), my.toDouble()))
                    Theme.rounded(ctx, row, if (isSelected) Theme.ACTIVE else Theme.HOVER, 4)
                ctx.drawTexture(Theme.moduleIcon(module), row.x + 5, row.y + 5, 15, 15,
                    if (module.enabled) Theme.ACCENT else Theme.SUBTLE)
                Theme.label(ctx, textRenderer, module.name, row.x + 25, row.y + 8, Theme.TEXT, row.width - 57)
                val toggle = UiRect(row.right - 23, row.y + 4, 18, 17)
                Theme.rounded(ctx, toggle, if (module.enabled) Theme.ACTIVE else Theme.CARD, 5)
                Theme.label(ctx, textRenderer, if (module.enabled) "●" else "○", toggle.x + 4, toggle.y + 4,
                    if (module.enabled) Theme.ACCENT else Theme.SUBTLE)
            }
        } finally { ctx.disableScissor() }
        Theme.scrollbar(ctx, scroll, track, mx, my)
        Theme.divider(ctx, panel.x + 8, panel.bottom - 48, panel.width - 16)
        Theme.label(ctx, textRenderer, "${candidates.count { it.enabled }} active · ${candidates.size} movable",
            panel.x + 10, panel.bottom - 38, Theme.MUTED, panel.width - 20)
        Theme.label(ctx, textRenderer, feedback, panel.x + 10, panel.bottom - 24, Theme.SUBTLE, panel.width - 20)
    }

    private fun iconButton(ctx: GuiContext, rect: UiRect, icon: String, mx: Int, my: Int) {
        if (rect.contains(mx.toDouble(), my.toDouble())) Theme.rounded(ctx, rect, Theme.HOVER, 4)
        ctx.drawTexture(Theme.icon(icon), rect.x + 4, rect.y + 4, rect.width - 8, rect.height - 8,
            if (rect.contains(mx.toDouble(), my.toDouble())) Theme.ACCENT else Theme.MUTED)
    }

    private fun setWidgetPosition(widget: Widget, mx: Double, my: Double) {
        var x = (mx - grabX).coerceIn(0.0, (width - widget.rect.width).coerceAtLeast(0).toDouble())
        var y = (my - grabY).coerceIn(0.0, (height - widget.rect.height).coerceAtLeast(0).toDouble())
        if (snapToGrid && gridSize > 1) {
            x = kotlin.math.round(x / gridSize) * gridSize
            y = kotlin.math.round(y / gridSize) * gridSize
        }
        val anchorX = when (widget.xAnchor) {
            XAnchor.LEFT -> x
            XAnchor.CENTER -> x + widget.rect.width / 2.0
            XAnchor.RIGHT -> x + widget.rect.width
            XAnchor.TIMER -> ((x - 4.0) / (width - widget.contentWidth * widget.scale - 8.0).coerceAtLeast(1.0)) * 100.0
        }
        val anchorY = when (widget.yAnchor) {
            YAnchor.TOP -> y
            YAnchor.TIMER -> ((y - 3.0) / (height - 14.0 * widget.scale).coerceAtLeast(1.0)) * 100.0
        }
        widget.position.x.value = (if (widget.xAnchor == XAnchor.TIMER) anchorX else anchorX / width * 100.0)
            .toFloat().coerceIn(widget.position.x.min, widget.position.x.max)
        widget.position.y.value = (if (widget.yAnchor == YAnchor.TIMER) anchorY else anchorY / height * 100.0)
            .toFloat().coerceIn(widget.position.y.min, widget.position.y.max)
    }

    private fun revert() {
        startPositions.forEach { (module, saved) ->
            position(module)?.let { it.x.value = saved.first; it.y.value = saved.second }
        }
        feedback = "Layout restored to the values from when the editor opened."
        com.endiq.client.config.ModulePreferences.save()
    }

    override fun onMouseClicked(mx: Double, my: Double, button: Int): Boolean {
        if (button == 0) {
            if (closeButton.contains(mx, my)) { closeGui(); return true }
            if (doneButton.contains(mx, my)) { closeGui(); return true }
            if (revertButton.contains(mx, my)) { revert(); return true }
            if (snapButton.contains(mx, my)) { snapToGrid = !snapToGrid; feedback = if (snapToGrid) "Snapping to ${gridSize}px grid." else "Snapping off."; return true }
            if (gridButton.contains(mx, my)) { gridSize = when (gridSize) { 2 -> 4; 4 -> 8; 8 -> 16; else -> 2 }; return true }
            if (hideButton.contains(mx, my)) { sidebarHidden = !sidebarHidden; layout(); return true }
            if (!sidebarHidden) {
                if (searchBox.contains(mx, my)) { searchFocused = true; search.end(); return true }
                if (scroll.beginDrag(mx, my, track)) return true
                if (viewport.contains(mx, my)) {
                    val modules = visibleModules()
                    val index = ((my - viewport.y - 4 + scroll.pixels) / 29).toInt()
                    modules.getOrNull(index)?.let { module ->
                        selected = module
                        searchFocused = false
                        val row = UiRect(viewport.x + 1, viewport.y + 4 + index * 29 - scroll.pixels, viewport.width - 3, 25)
                        val toggle = UiRect(row.right - 23, row.y + 4, 18, 17)
                        if (toggle.contains(mx, my)) {
                            if (module.unavailableReason == null) module.toggle()
                            feedback = if (module.enabled) "${module.name} preview enabled." else "${module.name} disabled."
                            com.endiq.client.config.ModulePreferences.save()
                        }
                        return true
                    }
                }
            }
            val widget = widgets().lastOrNull { it.rect.contains(mx, my) }
            if (widget != null) {
                selected = widget.module
                dragging = widget
                grabX = mx - widget.rect.x
                grabY = my - widget.rect.y
                searchFocused = false
                return true
            }
            searchFocused = false
        }
        if (button == 1) {
            widgets().lastOrNull { it.rect.contains(mx, my) }?.let { widget ->
                MinecraftClient.getInstance().setScreen(com.endiq.client.gui.settings.ModSettingsGui(widget.module, this))
                return true
            }
        }
        return super.onMouseClicked(mx, my, button)
    }

    override fun onMouseScrolled(mx: Double, my: Double, horizontal: Double, vertical: Double): Boolean {
        updateBounds()
        if (!sidebarHidden && (viewport.contains(mx, my) || track.contains(mx, my)) && scroll.wheel(vertical, horizontal)) return true
        return super.onMouseScrolled(mx, my, horizontal, vertical)
    }

    override fun onMouseDragged(mx: Double, my: Double, button: Int, dx: Double, dy: Double): Boolean {
        if (button == 0 && scroll.drag(my, track)) return true
        val active = dragging
        if (button == 0 && active != null) {
            makeWidget(active.module)?.let { setWidgetPosition(it, mx, my) }
            return true
        }
        return super.onMouseDragged(mx, my, button, dx, dy)
    }

    override fun onMouseReleased(mx: Double, my: Double, button: Int): Boolean {
        val wasDragging = button == 0 && dragging != null
        val handled = dragging != null || scroll.dragging
        dragging = null
        scroll.endDrag()
        if (wasDragging) com.endiq.client.config.ModulePreferences.save()
        return handled || super.onMouseReleased(mx, my, button)
    }

    override fun onCharTyped(text: String): Boolean {
        if (!searchFocused) return super.onCharTyped(text)
        search.insert(text)
        scroll.reset(); updateBounds()
        return true
    }

    override fun onKeyPressed(key: Int, scancode: Int, modifiers: Int): Boolean {
        val control = modifiers and (GLFW.GLFW_MOD_CONTROL or GLFW.GLFW_MOD_SUPER) != 0
        val shift = modifiers and GLFW.GLFW_MOD_SHIFT != 0
        if (key == GLFW.GLFW_KEY_RIGHT_ALT) { closeGui(); return true }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (searchFocused) { searchFocused = false; return true }
            closeGui(); return true
        }
        if (key == GLFW.GLFW_KEY_TAB) { sidebarHidden = !sidebarHidden; layout(); return true }
        if (searchFocused) {
            when {
                key == GLFW.GLFW_KEY_BACKSPACE -> { search.backspace(); scroll.reset(); updateBounds(); return true }
                key == GLFW.GLFW_KEY_DELETE -> { search.delete(); scroll.reset(); updateBounds(); return true }
                key == GLFW.GLFW_KEY_LEFT -> { search.move(-1, shift); return true }
                key == GLFW.GLFW_KEY_RIGHT -> { search.move(1, shift); return true }
                key == GLFW.GLFW_KEY_HOME -> { search.home(shift); return true }
                key == GLFW.GLFW_KEY_END -> { search.end(shift); return true }
                control && key == GLFW.GLFW_KEY_A -> { search.selectAll(); return true }
                control && key == GLFW.GLFW_KEY_C -> { copyToClipboard(search.selectedText()); return true }
                control && key == GLFW.GLFW_KEY_V -> { search.insert(clipboardText()); scroll.reset(); updateBounds(); return true }
            }
        } else when (key) {
            GLFW.GLFW_KEY_PAGE_DOWN -> { scroll.moveBy(viewport.height * .85); return true }
            GLFW.GLFW_KEY_PAGE_UP -> { scroll.moveBy(-viewport.height * .85); return true }
            GLFW.GLFW_KEY_HOME -> { scroll.moveTo(0.0); return true }
            GLFW.GLFW_KEY_END -> { scroll.moveTo(scroll.maximum); return true }
            GLFW.GLFW_KEY_SLASH -> { searchFocused = !sidebarHidden; return true }
        }
        return super.onKeyPressed(key, scancode, modifiers)
    }

    override fun closeGui() {
        com.endiq.client.config.ModulePreferences.save()
        MinecraftClient.getInstance().setScreen(parent)
    }
}
