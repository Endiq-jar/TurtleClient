package com.endiq.client.compat

/** Adapts GUI rendering and the pre/post-1.21.9 input event signatures once. */
abstract class ClientScreen(title: String) : Screen(literalText(title)) {
//? if >=26.1 {
/*    protected val textRenderer: TextRenderer get() = font
    protected val client: MinecraftClient get() = minecraft
*///?}

//? if >=26.1 {
/*    final override fun extractRenderState(ctx: NativeGuiContext, mx: Int, my: Int, delta: Float) {
        renderGui(GuiContext(ctx), mx, my, delta)
    }

    open fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        super.extractRenderState(ctx.native, mx, my, delta)
    }

    final override fun isPauseScreen() = false
*///?} else {
    final override fun render(ctx: NativeGuiContext, mx: Int, my: Int, delta: Float) {
        renderGui(GuiContext(ctx), mx, my, delta)
    }

    open fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        super.render(ctx.native, mx, my, delta)
    }

    final override fun shouldPause() = false
//?}

    open fun onMouseClicked(mx: Double, my: Double, button: Int) = false
    open fun onMouseReleased(mx: Double, my: Double, button: Int) = false
    open fun onMouseDragged(mx: Double, my: Double, button: Int, dx: Double, dy: Double) = false
    open fun onMouseScrolled(mx: Double, my: Double, horizontal: Double, vertical: Double) = false
    open fun onKeyPressed(key: Int, scancode: Int, modifiers: Int) = false
    open fun onCharTyped(text: String, modifiers: Int) = false

//? if >=26.1 {
/*    final override fun mouseClicked(event: net.minecraft.client.input.MouseButtonEvent, doubled: Boolean): Boolean =
        onMouseClicked(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubled)
    final override fun mouseReleased(event: net.minecraft.client.input.MouseButtonEvent): Boolean =
        onMouseReleased(event.x(), event.y(), event.button()) || super.mouseReleased(event)
    final override fun mouseDragged(event: net.minecraft.client.input.MouseButtonEvent, dx: Double, dy: Double): Boolean =
        onMouseDragged(event.x(), event.y(), event.button(), dx, dy) || super.mouseDragged(event, dx, dy)
    final override fun keyPressed(event: net.minecraft.client.input.KeyEvent): Boolean =
        onKeyPressed(event.key(), event.scancode(), event.modifiers()) || super.keyPressed(event)
    final override fun charTyped(event: net.minecraft.client.input.CharacterEvent): Boolean =
        onCharTyped(String(Character.toChars(event.codepoint())), event.modifiers()) || super.charTyped(event)
*///?} else if >=1.21.9 {
/*    final override fun mouseClicked(event: net.minecraft.client.gui.Click, doubled: Boolean): Boolean =
        onMouseClicked(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubled)
    final override fun mouseReleased(event: net.minecraft.client.gui.Click): Boolean =
        onMouseReleased(event.x(), event.y(), event.button()) || super.mouseReleased(event)
    final override fun mouseDragged(event: net.minecraft.client.gui.Click, dx: Double, dy: Double): Boolean =
        onMouseDragged(event.x(), event.y(), event.button(), dx, dy) || super.mouseDragged(event, dx, dy)
    final override fun keyPressed(event: net.minecraft.client.input.KeyInput): Boolean =
        onKeyPressed(event.key(), event.scancode(), event.modifiers()) || super.keyPressed(event)
    final override fun charTyped(event: net.minecraft.client.input.CharInput): Boolean =
        onCharTyped(event.asString(), event.modifiers()) || super.charTyped(event)
*///?} else {
    final override fun mouseClicked(mx: Double, my: Double, button: Int): Boolean =
        onMouseClicked(mx, my, button) || super.mouseClicked(mx, my, button)
    final override fun mouseReleased(mx: Double, my: Double, button: Int): Boolean =
        onMouseReleased(mx, my, button) || super.mouseReleased(mx, my, button)
    final override fun mouseDragged(mx: Double, my: Double, button: Int, dx: Double, dy: Double): Boolean =
        onMouseDragged(mx, my, button, dx, dy) || super.mouseDragged(mx, my, button, dx, dy)
    final override fun keyPressed(key: Int, scancode: Int, modifiers: Int): Boolean =
        onKeyPressed(key, scancode, modifiers) || super.keyPressed(key, scancode, modifiers)
    final override fun charTyped(character: Char, modifiers: Int): Boolean =
        onCharTyped(character.toString(), modifiers) || super.charTyped(character, modifiers)
//?}

//? if >=1.20.2 {
    final override fun mouseScrolled(mx: Double, my: Double, horizontal: Double, vertical: Double): Boolean =
        onMouseScrolled(mx, my, horizontal, vertical) || super.mouseScrolled(mx, my, horizontal, vertical)
//?} else {
/*    final override fun mouseScrolled(mx: Double, my: Double, vertical: Double): Boolean =
        onMouseScrolled(mx, my, 0.0, vertical) || super.mouseScrolled(mx, my, vertical)
*///?}
}
