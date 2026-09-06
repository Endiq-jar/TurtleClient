package com.endiq.client.compat

import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import kotlin.math.roundToInt

/** Flat, source-editable branding. The splash never depends on fonts being reloaded. */
object BrandingRenderer {
    private fun asset(name: String) = identifier("turtle-client", "textures/gui/loading/$name.png")
    private val contours = asset("contours")
    private val resources = asset("resources")
    private val finishing = asset("finishing")
    private val numbers = asset("numbers")
    private val edition = asset("edition")
    private val startup = asset("startup")

    @JvmStatic
    fun renderSplash(ctx: GuiContext, progress: LoadingProgress.Frame) {
        val sw = ctx.scaledWindowWidth
        val sh = ctx.scaledWindowHeight
        val layout = LoadingLayout.fit(sw, sh)
        ctx.fill(0, 0, sw, sh, Theme.BACKGROUND)
        if (sw >= 480 && sh >= 260) {
            val bounds = coverBounds(sw, sh, 512, 288)
            ctx.drawTexture(contours, bounds.x, bounds.y, bounds.width, bounds.height, 0x80FFFFFF.toInt())
        }
        if (layout.showChrome) {
            ctx.drawTexture(startup, layout.margin, layout.margin, 100, 10, 0xA0FFFFFF.toInt())
            ctx.fill(sw - layout.margin - 14, layout.margin + 4, sw - layout.margin, layout.margin + 6, Theme.ACTIVE)
            ctx.drawTexture(edition, (sw - 96) / 2, sh - layout.margin - 10, 96, 10, 0x90FFFFFF.toInt())
        }
        fun picture(texture: Identifier, rect: UiRect) = ctx.drawTexture(texture, rect.x, rect.y, rect.width, rect.height)
        picture(Theme.LOGO, layout.logo)
        picture(Theme.WORDMARK, layout.wordmark)

        val bar = layout.bar
        Theme.rounded(ctx, bar, Theme.BORDER, 1)
        val filled = (bar.width * progress.displayed).roundToInt().coerceIn(0, bar.width)
        if (filled > 0) Theme.rounded(ctx, UiRect(bar.x, bar.y, filled, bar.height), Theme.ACCENT, 1)

        val statusWidth = ((if (progress.finalizing) 126 else 112) * layout.scale).roundToInt()
            .coerceIn(1, layout.status.width)
        ctx.drawTexture(if (progress.finalizing) finishing else resources, layout.status.x, layout.status.y,
            statusWidth, layout.status.height, 0xC0FFFFFF.toInt())
        val value = "${progress.percent}%"
        val glyphWidth = (8 * layout.scale).roundToInt().coerceAtLeast(1)
        val left = layout.percentage.right - value.length * glyphWidth
        if (left >= layout.bar.x) for ((index, character) in value.withIndex()) {
            val cell = if (character == '%') 10 else character - '0'
            ctx.drawTextureRegion(numbers, left + index * glyphWidth, layout.percentage.y, glyphWidth,
                layout.percentage.height, (cell * 16).toFloat(), 0f, 16, 24, 176, 24, 0xD8FFFFFF.toInt())
        }
    }

    @JvmStatic
    fun renderWatermark(ctx: GuiContext) {
        ctx.drawTexture(Theme.LOGO, ctx.scaledWindowWidth - 20, ctx.scaledWindowHeight - 20, 14, 14, 0x88FFFFFF.toInt())
    }

    @JvmStatic
    fun showTitleScreen() {
        MinecraftClient.getInstance().setScreen(com.endiq.client.gui.CustomTitleScreen())
    }
}
