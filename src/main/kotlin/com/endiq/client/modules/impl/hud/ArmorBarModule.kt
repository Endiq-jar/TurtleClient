package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class ArmorBarModule : Module("Armor Bar", "Shows armor points as a bar", Category.HUD) {
    val barColor    = color("Bar Color", r=91, g=155, b=213)
    val bgColor     = color("Background Color", r=26, g=26, b=26)
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val barWidth    = slider("Bar Width", default=80f, min=20f, max=200f)
    val barHeight   = slider("Bar Height", default=4f, min=2f, max=12f)
    val posX        = slider("Position X", default=50f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=90f, min=0f, max=100f, suffix="%")
    val showText    = bool("Show Text", default=true)
    val showIcons   = bool("Show Icons", default=false)
    val style       = dropdown("Bar Style", options=arrayOf("Flat", "Rounded", "Segmented"), default=0)
    val segments    = slider("Segments", default=20f, min=4f, max=20f)
    val colorMode   = dropdown("Color Mode", options=arrayOf("Static", "Gradient", "Dynamic"), default=0)
    val colorFull   = color("Full Color", r=91, g=155, b=213)
    val colorLow    = color("Low Color", r=224, g=82, b=82)
    val shadow      = bool("Text Shadow", default=true)
    val animation   = bool("Smooth Animation", default=true)
    val animSpeed   = slider("Anim Speed", default=0.1f, min=0.01f, max=1f)
    val orientation = dropdown("Orientation", options=arrayOf("Horizontal", "Vertical"), default=0)
    val outline     = bool("Show Outline", default=false)
    val outlineColor= color("Outline Color", r=0, g=0, b=0)
    init { enable() }
}
