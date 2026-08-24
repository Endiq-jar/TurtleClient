package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class ReachDisplayModule : Module("Reach Display", "Shows last attack distance", Category.HUD) {
    val textColor  = color("Text Color", r=255, g=255, b=255)
    val bgColor    = color("Background", r=0, g=0, b=0, a=120)
    val showBg     = bool("Show Background", default=true)
    val precision  = slider("Decimal Places", default=2f, min=0f, max=4f)
    val scale      = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX       = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY       = slider("Position Y", default=52f, min=0f, max=100f, suffix="%")
    val shadow     = bool("Text Shadow", default=true)
    val fadeTime   = slider("Fade Time", default=3f, min=0f, max=10f, suffix="s")
    val showMax    = bool("Show Max Reach", default=false)
    val colorCode  = bool("Color Code Distance", default=true)
    val closeColor = color("Close Color", r=255, g=85, b=85)
    val farColor   = color("Far Color", r=85, g=255, b=85)
    var lastReach = 0.0
    fun getText() = if (lastReach > 0) "Reach: ${"%.${precision.value.toInt()}f".format(lastReach)}m" else ""
    init { enable() }
}
