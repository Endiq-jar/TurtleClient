package com.endiq.client.gui

import com.endiq.client.compat.*
import com.endiq.client.modules.Module
import com.endiq.client.modules.ModuleManager
import java.net.URI
import kotlin.math.sqrt

/**
 * Turtle Client main menu. Layout: account pill (top-left), icon dock +
 * settings gear (top-right), logo + primary nav list (center), favorited
 * module quick-toggles (bottom-left), version footer (bottom-center).
 *
 * Buttons are drawn procedurally (rounded rects, not texture-backed) so the
 * menu doesn't depend on shipping matching button art per theme -- only the
 * logo, panorama, and small icon glyphs are textures.
 */
class CustomTitleScreen : ClientScreen("Turtle Client") {

    // ── Palette (Turtle Client slate-blue identity) ──────────────────
    private val COL_BG        = 0xE60D1117.toInt()
    private val COL_PANEL     = 0xCC161B22.toInt()
    private val COL_PANEL_HI  = 0xE61C2333.toInt()
    private val COL_BORDER    = 0x33FFFFFF
    private val COL_PRIMARY   = 0xFF3B82F6.toInt()
    private val COL_ACCENT    = 0xFF38BDF8.toInt()
    private val COL_TEXT      = 0xFFE6EDF3.toInt()
    private val COL_TEXT_DIM  = 0xFF8B949E.toInt()
    private val COL_DANGER    = 0xFFF85149.toInt()
    private val COL_ONLINE    = 0xFF3FB950.toInt()

    private fun id(path: String) = identifier("turtle-client", "textures/gui/menu/$path")
    private val LOGO = id("logo.png")
    private fun icon(name: String) = id("icons/icon_$name.png")

    private val PANO_SETS = listOf("cherry", "regular")
    private val panoSet = PANO_SETS.random()
    private val panoFaces = Array(6) { i -> id("panorama/$panoSet/panorama_$i.png") }
    private val openedAt = System.currentTimeMillis()

    // ── Widgets ───────────────────────────────────────────────────────
    private class NavButton(val x: Int, val y: Int, val w: Int, val h: Int, val label: String, val danger: Boolean = false, val action: () -> Unit)
    private class IconButton(val x: Int, val y: Int, val size: Int, val texture: Identifier, val label: String, val tint: Int, val action: () -> Unit)
    private class QuickToggle(val x: Int, val y: Int, val size: Int, val module: Module, val color: Int)

    private val navButtons = mutableListOf<NavButton>()
    private val iconButtons = mutableListOf<IconButton>()
    private val quickToggles = mutableListOf<QuickToggle>()

    private var logoX = 0; private var logoY = 0; private var logoSize = 0
    private var footerY = 0

    override fun init() {
        navButtons.clear(); iconButtons.clear(); quickToggles.clear()

        // ── Logo ──
        logoSize = (width * 0.11f).toInt().coerceIn(64, 128)
        logoX = width / 2 - logoSize / 2
        logoY = (height * 0.07f).toInt()

        // ── Center nav list ──
        val btnW = (width * 0.30f).toInt().coerceIn(220, 420)
        val btnH = 32
        val gap = 8
        val btnX = width / 2 - btnW / 2
        var y = logoY + logoSize + 34

        navButtons.add(NavButton(btnX, y, btnW, btnH, "Singleplayer") {
            client?.setScreen(SelectWorldScreen(this))
        }); y += btnH + gap

        navButtons.add(NavButton(btnX, y, btnW, btnH, "Multiplayer") {
            client?.setScreen(MultiplayerScreen(this))
        }); y += btnH + gap

        navButtons.add(NavButton(btnX, y, btnW, btnH, "Cosmetics") {
            // No standalone cosmetics browser screen wired up yet -- CosmeticManager
            // only tracks registry/equip state right now, see COSMETICS_README.txt.
        }); y += btnH + gap

        navButtons.add(NavButton(btnX, y, btnW, btnH, "Screenshots") {
            openScreenshotsFolder()
        }); y += btnH + gap + 4

        navButtons.add(NavButton(btnX, y, btnW, btnH, "Store") {
            openLink("https://your-store-url-here.example")
        }); y += btnH + gap + 10

        navButtons.add(NavButton(btnX, y, btnW, btnH - 6, "Quit Game", danger = true) {
            client?.scheduleStop()
        })

        footerY = y + (btnH - 6) + 14

        // ── Top-right icon dock ──
        val iconSize = 22
        val iconGap = 8
        val topMargin = 14
        data class IconDef(val name: String, val label: String, val tint: Int, val action: () -> Unit)
        val icons = listOf(
            IconDef("mods", "Mod List", COL_TEXT) { client?.setScreen(ClickGui()) },
            IconDef("packs", "Resource Packs", COL_TEXT) { /* not wired up yet */ },
            IconDef("controls", "Controls", COL_TEXT) { client?.let { c -> c.setScreen(ControlsOptionsScreen(this, c.options)) } },
            IconDef("cosmetics", "Cosmetics", COL_ACCENT) { /* not wired up yet */ },
            IconDef("screenshots", "Open Screenshots Folder", COL_TEXT) { openScreenshotsFolder() },
            IconDef("servers", "Server List", COL_TEXT) { client?.setScreen(MultiplayerScreen(this)) },
        )
        var ix = width - 12 - (icons.size * iconSize + (icons.size - 1) * iconGap)
        val iy = topMargin + 22
        for (def in icons) {
            iconButtons.add(IconButton(ix, iy, iconSize, icon(def.name), def.label, def.tint, def.action))
            ix += iconSize + iconGap
        }

        // Settings gear floats above the dock, right-aligned.
        val gearSize = 18
        iconButtons.add(IconButton(width - 12 - gearSize, topMargin, gearSize, icon("settings"), "Options", COL_TEXT_DIM) {
            client?.let { c -> c.setScreen(optionsScreen(this)) }
        })

        // ── Bottom-left favorited-module quick toggles ──
        val favorited = ModuleManager.modules.filter { it.favorited }.take(5)
        val chipSize = 30
        val chipGap = 6
        var cy = height - 12 - chipSize
        for (m in favorited) {
            quickToggles.add(QuickToggle(12, cy, chipSize, m, categoryColor(m.category)))
            cy -= chipSize + chipGap
        }
    }

    private fun categoryColor(cat: Module.Category): Int = when (cat) {
        Module.Category.HUD -> 0xFFA855F7.toInt()
        Module.Category.PVP -> 0xFFEF4444.toInt()
        Module.Category.RENDER -> 0xFF22C55E.toInt()
        Module.Category.MOVEMENT -> 0xFFEAB308.toInt()
        Module.Category.UTILITY -> 0xFF14B8A6.toInt()
        Module.Category.HYPIXEL -> 0xFFF97316.toInt()
        Module.Category.PERFORMANCE -> 0xFF3B82F6.toInt()
        Module.Category.ALL -> 0xFF6B7280.toInt()
    }

    private fun openScreenshotsFolder() {
        try {
            val dir = MinecraftClient.getInstance().runDirectory.resolve("screenshots")
            dir.mkdirs()
            openPath(dir)
        } catch (ignored: Exception) {}
    }

    private fun openLink(url: String) {
        try {
            openUri(URI.create(url))
        } catch (ignored: Exception) {}
    }

    // ── Render ────────────────────────────────────────────────────────
    override fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        drawPanorama(ctx)

        try { ctx.drawTexture(LOGO, logoX, logoY, logoSize, logoSize) } catch (_: Exception) {}
        val title = "TURTLE CLIENT"
        ctx.drawTextWithShadow(textRenderer, title, width / 2 - textRenderer.getWidth(title) / 2, logoY + logoSize + 6, COL_TEXT)

        for (b in navButtons) drawNavButton(ctx, b, mx, my)
        for (b in iconButtons) drawIconButton(ctx, b, mx, my)
        for (q in quickToggles) drawQuickToggle(ctx, q, mx, my)

        drawAccountPill(ctx)

        val mcVersion = try { gameVersion() } catch (e: Exception) { "?" }
        val ver = "Turtle Client 1.0.0 (fabric/$mcVersion)"
        ctx.drawTextWithShadow(textRenderer, ver, width / 2 - textRenderer.getWidth(ver) / 2, footerY, COL_TEXT_DIM)

        if (quickToggles.isNotEmpty()) {
            ctx.drawTextWithShadow(textRenderer, "GUI", 12, quickToggles.last().y - 12, COL_TEXT_DIM)
        }

        // Hover tooltips drawn last, on top of everything.
        for (b in navButtons) if (hovered(mx, my, b.x, b.y, b.w, b.h)) drawTooltip(ctx, b.label, mx, my)
        for (b in iconButtons) if (hovered(mx, my, b.x, b.y, b.size, b.size)) drawTooltip(ctx, b.label, mx, my)
        for (q in quickToggles) if (hovered(mx, my, q.x, q.y, q.size, q.size)) drawTooltip(ctx, "${q.module.name} (${if (q.module.enabled) "on" else "off"})", mx, my)

        super.renderGui(ctx, mx, my, delta)
    }

    private fun drawAccountPill(ctx: GuiContext) {
        val name = playerName()
        val pillH = 34
        val avatarSize = 24
        val textW = textRenderer.getWidth(name)
        val pillW = 12 + avatarSize + 10 + textW + 14
        val px = 12; val py = 12

        fillRounded(ctx, px, py, pillW, pillH, 8, COL_PANEL)
        fillRounded(ctx, px + 6, py + 5, avatarSize, avatarSize, 5, COL_PRIMARY)
        val initial = name.take(1).uppercase()
        ctx.drawTextWithShadow(textRenderer, initial, px + 6 + avatarSize / 2 - textRenderer.getWidth(initial) / 2, py + 5 + avatarSize / 2 - 4, COL_TEXT)
        fillCircle(ctx, px + 6 + avatarSize - 2, py + 5 + avatarSize - 2, 4, COL_ONLINE)
        ctx.drawTextWithShadow(textRenderer, name, px + 12 + avatarSize, py + pillH / 2 - 4, COL_TEXT)
    }

    private fun drawNavButton(ctx: GuiContext, b: NavButton, mx: Int, my: Int) {
        val hov = hovered(mx, my, b.x, b.y, b.w, b.h)
        val bg = when {
            b.label == "Store" -> if (hov) COL_ACCENT else COL_PRIMARY
            b.danger -> if (hov) 0x40F85149 else 0x20F85149
            else -> if (hov) COL_PANEL_HI else COL_PANEL
        }
        fillRounded(ctx, b.x, b.y, b.w, b.h, 6, bg)
        val textCol = if (b.danger) COL_DANGER else COL_TEXT
        ctx.drawTextWithShadow(textRenderer, b.label, b.x + b.w / 2 - textRenderer.getWidth(b.label) / 2, b.y + b.h / 2 - 4, textCol)
    }

    private fun drawIconButton(ctx: GuiContext, b: IconButton, mx: Int, my: Int) {
        val hov = hovered(mx, my, b.x, b.y, b.size, b.size)
        val color = if (hov) COL_ACCENT else b.tint
        try { ctx.drawTexture(b.texture, b.x, b.y, b.size, b.size, color) } catch (_: Exception) {}
    }

    private fun drawQuickToggle(ctx: GuiContext, q: QuickToggle, mx: Int, my: Int) {
        val hov = hovered(mx, my, q.x, q.y, q.size, q.size)
        val bg = if (hov) COL_PANEL_HI else COL_PANEL
        fillRounded(ctx, q.x, q.y, q.size, q.size, 6, bg)
        fillRounded(ctx, q.x + 3, q.y + 3, q.size - 6, q.size - 6, 4, q.color)
        val letter = q.module.name.take(1).uppercase()
        ctx.drawTextWithShadow(textRenderer, letter, q.x + q.size / 2 - textRenderer.getWidth(letter) / 2, q.y + q.size / 2 - 4, COL_TEXT)
        if (q.module.enabled) fillCircle(ctx, q.x + q.size - 4, q.y + 4, 3, COL_ONLINE)
    }

    private fun drawTooltip(ctx: GuiContext, text: String, mx: Int, my: Int) {
        val w = textRenderer.getWidth(text) + 8
        val tx = (mx + 10).coerceAtMost(width - w - 4)
        val ty = my - 14
        fillRounded(ctx, tx, ty, w, 14, 3, COL_PANEL_HI)
        ctx.drawTextWithShadow(textRenderer, text, tx + 4, ty + 3, COL_TEXT)
    }

    private fun drawPanorama(ctx: GuiContext) {
        ctx.fill(0, 0, width, height, COL_BG)

        val elapsed = (System.currentTimeMillis() - openedAt) / 1000.0
        val perImage = 8.0
        val totalT = elapsed / perImage
        val idx = totalT.toInt().mod(panoFaces.size)
        val nextIdx = (idx + 1) % panoFaces.size
        val frac = (totalT - Math.floor(totalT)).toFloat()

        try { ctx.drawTexture(panoFaces[idx], 0, 0, width, height) } catch (_: Exception) {}

        if (frac > 0.02f) {
            try {
                val alpha = (frac.coerceIn(0f, 1f) * 255f).toInt()
                ctx.drawTexture(panoFaces[nextIdx], 0, 0, width, height, (alpha shl 24) or 0x00FFFFFF)
            } catch (_: Exception) {}
        }

        // Darken for readability, slightly heavier than before to match the reference's near-black look.
        ctx.fill(0, 0, width, height, 0x8A000000.toInt())
    }

    // ── Small drawing primitives (no texture dependency) ────────────
    private fun fillRounded(ctx: GuiContext, x: Int, y: Int, w: Int, h: Int, r: Int, color: Int) {
        val rad = r.coerceAtMost(minOf(w, h) / 2).coerceAtLeast(0)
        if (rad == 0) { ctx.fill(x, y, x + w, y + h, color); return }
        ctx.fill(x, y + rad, x + w, y + h - rad, color)
        ctx.fill(x + rad, y, x + w - rad, y + rad, color)
        ctx.fill(x + rad, y + h - rad, x + w - rad, y + h, color)
        for (dy in 0 until rad) {
            val dx = sqrt((rad * rad - dy * dy).toDouble()).toInt()
            ctx.fill(x + rad - dx, y + rad - dy, x + rad, y + rad - dy + 1, color)
            ctx.fill(x + w - rad, y + rad - dy, x + w - rad + dx, y + rad - dy + 1, color)
            ctx.fill(x + rad - dx, y + h - rad + dy, x + rad, y + h - rad + dy + 1, color)
            ctx.fill(x + w - rad, y + h - rad + dy, x + w - rad + dx, y + h - rad + dy + 1, color)
        }
    }

    private fun fillCircle(ctx: GuiContext, cx: Int, cy: Int, r: Int, color: Int) {
        for (dy in -r..r) {
            val dx = sqrt((r * r - dy * dy).toDouble()).toInt()
            ctx.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color)
        }
    }

    private fun hovered(mx: Int, my: Int, x: Int, y: Int, w: Int, h: Int) =
        mx in x..(x + w) && my in y..(y + h)

    // ── Input ─────────────────────────────────────────────────────────
    override fun onMouseClicked(mx: Double, my: Double, btn: Int): Boolean {
        val imx = mx.toInt(); val imy = my.toInt()
        for (b in navButtons) if (hovered(imx, imy, b.x, b.y, b.w, b.h)) { b.action(); return true }
        for (b in iconButtons) if (hovered(imx, imy, b.x, b.y, b.size, b.size)) { b.action(); return true }
        for (q in quickToggles) if (hovered(imx, imy, q.x, q.y, q.size, q.size)) { q.module.toggle(); return true }
        return super.onMouseClicked(mx, my, btn)
    }

}
