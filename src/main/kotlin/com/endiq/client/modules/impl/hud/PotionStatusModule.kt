package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class PotionStatusModule : Module("Potion Effects", "Shows active potion effects", Category.HUD) {
    val textColor    = color("Text Color", r=255, g=255, b=255)
    val bgColor      = color("Background", r=0, g=0, b=0, a=120)
    val showBg       = bool("Show Background", default=true)
    val showIcons    = bool("Show Icons", default=true)
    val showTimer    = bool("Show Timer", default=true)
    val showAmp      = bool("Show Amplifier", default=true)
    val showName     = bool("Show Effect Name", default=true)
    val scale        = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX         = slider("Position X", default=98f, min=0f, max=100f, suffix="%")
    val posY         = slider("Position Y", default=2f, min=0f, max=100f, suffix="%")
    val iconSize     = slider("Icon Size", default=16f, min=8f, max=32f)
    val layout       = dropdown("Layout", options=arrayOf("Vertical", "Horizontal", "Grid"), default=0)
    val shadow       = bool("Text Shadow", default=true)
    val hideNeg      = bool("Hide Negative Effects", default=false)
    val hidePos      = bool("Hide Positive Effects", default=false)
    val warnExpire   = bool("Warn On Expire", default=true)
    val warnTime     = slider("Warn Time", default=5f, min=1f, max=30f, suffix="s")
    val warnColor    = color("Warn Color", r=255, g=85, b=85)
    val maxEffects   = slider("Max Effects", default=10f, min=1f, max=20f)
    init { enable() }
}
