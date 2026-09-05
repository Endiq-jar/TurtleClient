package com.endiq.client.modules.impl.hud
import com.endiq.client.compat.*
import com.endiq.client.modules.Module
class FpsModule : Module("FPS Counter", "Shows frames per second", Category.HUD) {
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val bgColor     = color("Background Color", r=0, g=0, b=0, a=120)
    val showBg      = bool("Show Background", default=true)
    val showLabel   = bool("Show Label", default=true)
    val label       = dropdown("Label Style", options=arrayOf("FPS: %d", "%d fps", "%d FPS", "Frames: %d"), default=0)
    val scale       = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX        = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=2f, min=0f, max=100f, suffix="%")
    val shadow      = bool("Text Shadow", default=true)
    val bold        = bool("Bold Text", default=false)
    val bgPadding   = slider("BG Padding", default=2f, min=0f, max=10f)
    val bgRadius    = slider("BG Radius", default=2f, min=0f, max=8f)
    val colorMode   = dropdown("Color Mode", options=arrayOf("Static", "Rainbow", "Pulse"), default=0)
    val rainbowSpeed= slider("Rainbow Speed", default=1f, min=0.1f, max=5f)
    val showGraph   = bool("Show FPS Graph", default=false)
    val graphHeight = slider("Graph Height", default=20f, min=10f, max=60f)
    val graphColor  = color("Graph Color", r=62, g=153, b=112)
    val warnBelow   = slider("Warn Below FPS", default=30f, min=0f, max=120f)
    val warnColor   = color("Warn Color", r=224, g=82, b=82)
    val alignment   = dropdown("Alignment", options=arrayOf("Left", "Center", "Right"), default=0)
    fun getText(): String {
        val fps = clientFps()
        return when(label.selected) {
            1 -> "$fps fps"; 2 -> "$fps FPS"; 3 -> "Frames: $fps"
            else -> "FPS: $fps"
        }
    }
    init { enable() }
}
