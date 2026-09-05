package com.endiq.client.compat

/** Shared loading-screen and watermark drawing, independent of GUI API versions. */
object BrandingRenderer {
    private val loading = identifier("turtle-client", "textures/loading_icon.png")
    private val logo = identifier("turtle-client", "turtle_logo.png")

    @JvmStatic
    fun renderSplash(ctx: GuiContext, progress: Float) {
        val sw = ctx.scaledWindowWidth
        val sh = ctx.scaledWindowHeight
        val cx = sw / 2
        val cy = sh / 2
        ctx.fill(0, 0, sw, sh, 0xFF0A0A0F.toInt())

        val t = System.currentTimeMillis() / 1000.0
        val ring = (72 + kotlin.math.sin(t * 2.0) * 4).toInt()
        val alpha = ((kotlin.math.sin(t * 2.0) * 0.4 + 0.5) * 140).toInt().coerceIn(0, 180)
        val teal = (alpha shl 24) or 0x002EBFA5
        ctx.fill(cx - ring, cy - ring, cx + ring, cy - ring + 2, teal)
        ctx.fill(cx - ring, cy + ring - 2, cx + ring, cy + ring, teal)
        ctx.fill(cx - ring, cy - ring, cx - ring + 2, cy + ring, teal)
        ctx.fill(cx + ring - 2, cy - ring, cx + ring, cy + ring, teal)
        ctx.fill(cx - 44, cy - 39, cx + 44, cy + 49, 0x44000000)
        ctx.drawTexture(loading, cx - 44, cy - 44, 88, 88)

        val barX = cx - 80
        val barY = cy + 58
        ctx.fill(barX, barY, barX + 160, barY + 4, 0xFF1A1A1A.toInt())
        ctx.fill(barX, barY, barX + (160 * progress.coerceIn(0f, 1f)).toInt(), barY + 4, 0xFF2EBFA5.toInt())
        val font = MinecraftClient.getInstance().textRenderer
        val text = "TurtleClient v1.0"
        ctx.drawTextWithShadow(font, text, cx - font.getWidth(text) / 2, cy + 68, -1)
    }

    @JvmStatic
    fun renderWatermark(ctx: GuiContext) {
        ctx.drawTexture(logo, ctx.scaledWindowWidth - 52, ctx.scaledWindowHeight - 18, 48, 16, 0xB2FFFFFF.toInt())
    }

    @JvmStatic
    fun showTitleScreen() {
        MinecraftClient.getInstance().setScreen(com.endiq.client.gui.CustomTitleScreen())
    }
}
