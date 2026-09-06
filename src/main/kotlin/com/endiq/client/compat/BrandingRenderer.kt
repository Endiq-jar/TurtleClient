package com.endiq.client.compat

import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme

/** Original TurtleClient branding, shared by all supported render APIs. */
object BrandingRenderer {
    @JvmStatic
    fun renderSplash(ctx: GuiContext, progress: Float) {
        val sw = ctx.scaledWindowWidth
        val sh = ctx.scaledWindowHeight
        val cx = sw / 2
        val cy = sh / 2
        ctx.fill(0, 0, sw, sh, Theme.BACKGROUND)
        val size = minOf(72, (sh / 3).coerceAtLeast(24))
        ctx.drawTexture(Theme.LOGO, cx - size / 2, cy - size - 8, size, size)
        ctx.drawTexture(Theme.WORDMARK, cx - 72, cy + 2, 144, 27)
        val width = minOf(180, sw - 40).coerceAtLeast(1)
        val bar = UiRect(cx - width / 2, cy + 43, width, 4)
        Theme.rounded(ctx, bar, Theme.CARD, 2)
        Theme.rounded(ctx, UiRect(bar.x, bar.y, (width * progress.coerceIn(0f, 1f)).toInt(), bar.height), Theme.ACCENT, 2)
        val font = MinecraftClient.getInstance().textRenderer
        val text = if (progress >= 1f) "Ready to explore" else "Preparing your world"
        Theme.label(ctx, font, text, cx - font.getWidth(text) / 2, bar.bottom + 12, Theme.MUTED)
    }

    @JvmStatic
    fun renderWatermark(ctx: GuiContext) {
        // Small square mark, not a distorted screenshot or a squashed wordmark.
        ctx.drawTexture(Theme.LOGO, ctx.scaledWindowWidth - 20, ctx.scaledWindowHeight - 20, 14, 14, 0x88FFFFFF.toInt())
    }

    @JvmStatic
    fun showTitleScreen() {
        MinecraftClient.getInstance().setScreen(com.endiq.client.gui.CustomTitleScreen())
    }
}
