package com.endiq.client.modules.impl.hypixel
import com.endiq.client.modules.Module
class UhcOverlayModule : Module("UHC Overlay", "HP and saturation for UHC", Category.HYPIXEL) {
    val hpColor     = color("HP Color", r=255, g=85, b=85)
    val satColor    = color("Saturation Color", r=85, g=255, b=85)
    val bgColor     = color("Background", r=0, g=0, b=0, a=120)
    val showBg      = bool("Show Background", default=true)
    val showHp      = bool("Show HP", default=true)
    val showSat     = bool("Show Saturation", default=true)
    val showFood    = bool("Show Food Level", default=false)
    val showAbsorp  = bool("Show Absorption", default=true)
    val scale       = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX        = slider("Position X", default=50f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=85f, min=0f, max=100f, suffix="%")
    val precision   = slider("Decimal Places", default=1f, min=0f, max=3f)
    val showBar     = bool("Show Bar", default=false)
    val shadow      = bool("Text Shadow", default=true)
    val style       = dropdown("Style", options=arrayOf("Compact", "Full", "Bars Only"), default=0)
}
