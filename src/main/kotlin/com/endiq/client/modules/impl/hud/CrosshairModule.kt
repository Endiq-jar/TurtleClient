package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class CrosshairModule : Module("Crosshair", "Custom crosshair", Category.HUD) {
    val color       = color("Color", r=255, g=255, b=255)
    val size        = slider("Size", default=5f, min=1f, max=20f)
    val thickness   = slider("Thickness", default=1f, min=1f, max=5f)
    val gap         = slider("Gap", default=0f, min=0f, max=10f)
    val style       = dropdown("Style", options=arrayOf("Cross", "Dot", "Circle", "T-Shape", "X-Shape", "None"), default=0)
    val showDot     = bool("Show Center Dot", default=false)
    val dotSize     = slider("Dot Size", default=2f, min=1f, max=8f)
    val dotColor    = color("Dot Color", r=255, g=255, b=255)
    val alpha       = slider("Alpha", default=255f, min=50f, max=255f)
    val outline     = bool("Outline", default=false)
    val outlineColor= color("Outline Color", r=0, g=0, b=0)
    val colorMode   = dropdown("Color Mode", options=arrayOf("Static", "Rainbow", "Enemy Color"), default=0)
    val attackScale = bool("Scale on Attack", default=false)
    val hideVanilla = bool("Hide Vanilla Crosshair", default=true)
    val offsetX     = slider("X Offset", default=0f, min=-20f, max=20f)
    val offsetY     = slider("Y Offset", default=0f, min=-20f, max=20f)
    val rainbowSpeed= slider("Rainbow Speed", default=1f, min=0.1f, max=5f)
    val opacity     = slider("Opacity", default=1f, min=0.1f, max=1f)
    init { enable() }
}
