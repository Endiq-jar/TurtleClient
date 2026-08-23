package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class BossBarModule : Module("Boss Bar", "Custom boss bar", Category.HUD) {
    val barColor    = color("Bar Color", r=224, g=82, b=82)
    val bgColor     = color("Background", r=30, g=30, b=30)
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val barWidth    = slider("Bar Width", default=182f, min=60f, max=400f)
    val barHeight   = slider("Bar Height", default=5f, min=2f, max=16f)
    val posX        = slider("Position X", default=50f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=12f, min=0f, max=50f, suffix="%")
    val showText    = bool("Show Text", default=true)
    val showPercent = bool("Show Percentage", default=true)
    val style       = dropdown("Style", options=arrayOf("Flat", "Notched", "Rounded"), default=0)
    val shadow      = bool("Text Shadow", default=true)
    val outline     = bool("Show Outline", default=false)
    val hideDefault = bool("Hide Default Vanilla Bar", default=true)
    val compact     = bool("Compact Mode", default=false)
    val maxBars     = slider("Max Bars Shown", default=3f, min=1f, max=10f)
    val animation   = bool("Animate Bar", default=true)
}
