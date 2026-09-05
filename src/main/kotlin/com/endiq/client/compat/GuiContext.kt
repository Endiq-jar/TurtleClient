package com.endiq.client.compat

/** The drawing operations TurtleClient uses, backed by each version's GUI API. */
class GuiContext(val native: NativeGuiContext) {
    val scaledWindowWidth: Int get() = MinecraftClient.getInstance().windowWidth()
    val scaledWindowHeight: Int get() = MinecraftClient.getInstance().windowHeight()

    fun fill(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
//? if >=1.20 {
        native.fill(x1, y1, x2, y2, color)
//?} else {
/*        net.minecraft.client.gui.DrawableHelper.fill(native, x1, y1, x2, y2, color)
*///?}
    }

    fun drawTextWithShadow(font: TextRenderer, text: String, x: Int, y: Int, color: Int) {
//? if >=26.1 {
/*        native.text(font, text, x, y, color, true)
*///?} else if >=1.20 {
        native.drawTextWithShadow(font, text, x, y, color)
//?} else {
/*        font.drawWithShadow(native, text, x.toFloat(), y.toFloat(), color)
*///?}
    }

    fun drawItem(stack: ItemStack, x: Int, y: Int) {
//? if >=26.1 {
/*        native.item(stack, x, y)
*///?} else if >=1.20 {
        native.drawItem(stack, x, y)
//?} else {
/*        MinecraftClient.getInstance().itemRenderer.renderInGui(stack, x, y)
*///?}
    }

    // All current callers draw a whole texture, scaled to the supplied rectangle.
    fun drawTexture(texture: Identifier, x: Int, y: Int, width: Int, height: Int, color: Int = -1) {
//? if >=26.1 {
/*        native.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
            texture, x, y, 0f, 0f, width, height, width, height, color)
*///?} else if >=1.21.6 {
/*        native.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
            texture, x, y, 0f, 0f, width, height, width, height, color)
*///?} else if >=1.21.2 {
        native.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured,
            texture, x, y, 0f, 0f, width, height, width, height, color)
//?} else if >=1.20 {
/*        val a = (color ushr 24 and 255) / 255f
        val r = (color ushr 16 and 255) / 255f
        val g = (color ushr 8 and 255) / 255f
        val b = (color and 255) / 255f
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(r, g, b, a)
        try {
            native.drawTexture(texture, x, y, 0f, 0f, width, height, width, height)
        } finally {
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        }
*///?} else {
/*        val a = (color ushr 24 and 255) / 255f
        val r = (color ushr 16 and 255) / 255f
        val g = (color ushr 8 and 255) / 255f
        val b = (color and 255) / 255f
        com.mojang.blaze3d.systems.RenderSystem.setShader { net.minecraft.client.render.GameRenderer.getPositionTexProgram() }
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, texture)
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(r, g, b, a)
        try {
            net.minecraft.client.gui.DrawableHelper.drawTexture(native, x, y, 0f, 0f, width, height, width, height)
        } finally {
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        }
*///?}
    }

    fun enableScissor(x1: Int, y1: Int, x2: Int, y2: Int) {
//? if >=1.20 {
        native.enableScissor(x1, y1, x2, y2)
//?} else {
/*        val window = MinecraftClient.getInstance().window
        val scale = window.scaleFactor
        com.mojang.blaze3d.systems.RenderSystem.enableScissor(
            (x1 * scale).toInt(), (window.framebufferHeight - y2 * scale).toInt(),
            ((x2 - x1) * scale).toInt().coerceAtLeast(0), ((y2 - y1) * scale).toInt().coerceAtLeast(0))
*///?}
    }

    fun disableScissor() {
//? if >=1.20 {
        native.disableScissor()
//?} else {
/*        com.mojang.blaze3d.systems.RenderSystem.disableScissor()
*///?}
    }
}
