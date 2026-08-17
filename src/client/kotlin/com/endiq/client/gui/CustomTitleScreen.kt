package com.endiq.client.gui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
import net.minecraft.client.gui.screen.option.OptionsScreen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import java.net.URI

class CustomTitleScreen : Screen(Text.literal("Turtle Client")) {

    // ── Assets ────────────────────────────────────────────────────────
    private fun id(path: String) = Identifier.of("turtle-client", "textures/gui/menu/$path")

    private val LOGO   = id("logo.png")
    private val FOOTER = id("footer_bar.png")

    private val PANO_SETS = listOf("cherry", "regular")
    private val panoSet = PANO_SETS.random()
    private val panoFaces = Array(6) { i -> id("panorama/$panoSet/panorama_$i.png") }
    private val openedAt = System.currentTimeMillis()

    // ── Layout / interaction ─────────────────────────────────────────
    private class MenuButton(
        val x: Int, val y: Int, val w: Int, val h: Int,
        val normal: Identifier, val hover: Identifier,
        val label: String,
        val action: () -> Unit
    )

    private val buttons = mutableListOf<MenuButton>()
    private val icons = mutableListOf<MenuButton>()

    private var footerX = 0; private var footerY = 0; private var footerW = 0; private var footerH = 0
    private var logoX = 0; private var logoY = 0; private var logoSize = 0

    override fun init() {
        buttons.clear()
        icons.clear()

        // Logo
        logoSize = (width * 0.16f).toInt().coerceIn(80, 180)
        logoX = width / 2 - logoSize / 2
        logoY = (height * 0.08f).toInt()

        // Big banner buttons
        val btnW = (width * 0.42f).toInt().coerceIn(200, 360)
        val btnH = (btnW * (108f / 1280f)).toInt().coerceAtLeast(16)
        val btnX = width / 2 - btnW / 2
        var y = (height * 0.42f).toInt()
        val gap = 8

        buttons.add(MenuButton(btnX, y, btnW, btnH, id("button_singleplayer.png"), id("button_singleplayer_hover.png"), "Singleplayer") {
            MinecraftClient.getInstance().setScreen(SelectWorldScreen(this))
        })
        y += btnH + gap

        buttons.add(MenuButton(btnX, y, btnW, btnH, id("button_multiplayer.png"), id("button_multiplayer_hover.png"), "Multiplayer") {
            MinecraftClient.getInstance().setScreen(MultiplayerScreen(this))
        })
        y += btnH + gap

        buttons.add(MenuButton(btnX, y, btnW, btnH, id("button_store.png"), id("button_store_hover.png"), "Store") {
            openLink("https://your-store-url-here.example")
        })

        // Footer bar (version tag)
        footerW = btnW
        footerH = (footerW * (53f / 1280f)).toInt().coerceAtLeast(8)
        footerX = width / 2 - footerW / 2
        footerY = height - footerH - 34

        // Bottom-left utility icon row
        val iconSize = 26
        val iconGap = 6
        var ix = 12
        val iy = height - iconSize - 12

        icons.add(MenuButton(ix, iy, iconSize, iconSize, id("icon_account.png"), id("icon_account_hover.png"), "Account") {
            // No standalone account screen wired up yet -- launcher handles auth.
        })
        ix += iconSize + iconGap

        icons.add(MenuButton(ix, iy, iconSize, iconSize, id("icon_mods.png"), id("icon_mods_hover.png"), "Client Mods") {
            MinecraftClient.getInstance().setScreen(ClickGui())
        })
        ix += iconSize + iconGap

        icons.add(MenuButton(ix, iy, iconSize, iconSize, id("icon_settings.png"), id("icon_settings_hover.png"), "Options") {
            MinecraftClient.getInstance().setScreen(OptionsScreen(this, MinecraftClient.getInstance().options))
        })
        ix += iconSize + iconGap

        icons.add(MenuButton(ix, iy, iconSize, iconSize, id("icon_link.png"), id("icon_link_hover.png"), "Community") {
            openLink("https://discord.gg/your-invite-here")
        })
        ix += iconSize + iconGap

        icons.add(MenuButton(ix, iy, iconSize, iconSize, id("icon_exit.png"), id("icon_exit_hover.png"), "Quit") {
            MinecraftClient.getInstance().scheduleStop()
        })
    }

    private fun openLink(url: String) {
        try {
            Util.getOperatingSystem().open(URI.create(url))
        } catch (ignored: Exception) {}
    }

    // ── Render ────────────────────────────────────────────────────────
    override fun render(ctx: DrawContext, mx: Int, my: Int, delta: Float) {
        drawPanorama(ctx)

        try {
            ctx.drawTexture(RenderLayer::getGuiTextured, LOGO, logoX, logoY, 0f, 0f, logoSize, logoSize, logoSize, logoSize, -1)
        } catch (ignored: Exception) {}

        for (b in buttons) drawMenuButton(ctx, b, mx, my)
        for (b in icons) drawMenuButton(ctx, b, mx, my)

        try {
            ctx.drawTexture(RenderLayer::getGuiTextured, FOOTER, footerX, footerY, 0f, 0f, footerW, footerH, footerW, footerH, -1)
        } catch (ignored: Exception) {}
        val ver = "Turtle Client v1.0"
        ctx.drawTextWithShadow(textRenderer, ver, width / 2 - textRenderer.getWidth(ver) / 2, footerY + footerH + 4, 0xFFFFFFFF.toInt())

        super.render(ctx, mx, my, delta)
    }

    private fun drawMenuButton(ctx: DrawContext, b: MenuButton, mx: Int, my: Int) {
        val hovered = mx in b.x..(b.x + b.w) && my in b.y..(b.y + b.h)
        val tex = if (hovered) b.hover else b.normal
        try {
            ctx.drawTexture(RenderLayer::getGuiTextured, tex, b.x, b.y, 0f, 0f, b.w, b.h, b.w, b.h, -1)
        } catch (ignored: Exception) {}
        if (hovered) {
            ctx.drawTextWithShadow(textRenderer, b.label, mx + 12, my - 10, 0xFFFFFFFF.toInt())
        }
    }

    private fun drawPanorama(ctx: DrawContext) {
        ctx.fill(0, 0, width, height, 0xFF0A0A0F.toInt())

        val elapsed = (System.currentTimeMillis() - openedAt) / 1000.0
        val perImage = 8.0
        val totalT = elapsed / perImage
        val idx = (totalT.toInt().mod(panoFaces.size))
        val nextIdx = (idx + 1) % panoFaces.size
        val frac = (totalT - Math.floor(totalT)).toFloat()

        try {
            ctx.drawTexture(RenderLayer::getGuiTextured, panoFaces[idx], 0, 0, 0f, 0f, width, height, width, height, -1)
        } catch (ignored: Exception) {}

        if (frac > 0.02f) {
            try {
                val alpha = (frac.coerceIn(0f, 1f) * 255f).toInt()
                val color = (alpha shl 24) or 0x00FFFFFF
                ctx.drawTexture(RenderLayer::getGuiTextured, panoFaces[nextIdx], 0, 0, 0f, 0f, width, height, width, height, color)
            } catch (ignored: Exception) {}
        }

        // Darken for button/text readability
        ctx.fill(0, 0, width, height, 0x66000000)
    }

    // ── Input ─────────────────────────────────────────────────────────
    override fun mouseClicked(mx: Double, my: Double, btn: Int): Boolean {
        val imx = mx.toInt(); val imy = my.toInt()
        for (b in buttons) {
            if (imx in b.x..(b.x + b.w) && imy in b.y..(b.y + b.h)) { b.action(); return true }
        }
        for (b in icons) {
            if (imx in b.x..(b.x + b.w) && imy in b.y..(b.y + b.h)) { b.action(); return true }
        }
        return super.mouseClicked(mx, my, btn)
    }

    override fun shouldPause() = false
}
