package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class AttackIndicatorModule : Module("Attack Indicator", "Shows attack cooldown", Category.HUD) {
    val readyColor   = color("Ready Color", r=62, g=153, b=112)
    val chargeColor  = color("Charging Color", r=224, g=82, b=82)
    val bgColor      = color("Background", r=26, g=26, b=26)
    val barWidth     = slider("Bar Width", default=40f, min=20f, max=120f)
    val barHeight    = slider("Bar Height", default=3f, min=2f, max=10f)
    val posX         = slider("Position X", default=50f, min=0f, max=100f, suffix="%")
    val posY         = slider("Position Y", default=55f, min=0f, max=100f, suffix="%")
    val style        = dropdown("Style", options=arrayOf("Bar", "Circle", "Text"), default=0)
    val showPercent  = bool("Show Percentage", default=false)
    val showIcon     = bool("Show Sword Icon", default=false)
    val pulseReady   = bool("Pulse When Ready", default=true)
    val animation    = bool("Smooth Fill", default=true)
    val shadow       = bool("Text Shadow", default=true)
    val outline      = bool("Show Outline", default=false)
    val onlyInCombat = bool("Only In Combat", default=false)
    val scale        = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val colorMode    = dropdown("Color Mode", options=arrayOf("Static", "Dynamic"), default=1)
    val bgAlpha      = slider("BG Alpha", default=180f, min=0f, max=255f)
    init { enable() }
}
