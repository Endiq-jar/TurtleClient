package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class ArmorStatusModule : Module("Armor Status", "Shows armor items and durability", Category.HUD) {
    val scale        = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX         = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY         = slider("Position Y", default=85f, min=0f, max=100f, suffix="%")
    val showDurNum   = bool("Show Durability Number", default=true)
    val showDurBar   = bool("Show Durability Bar", default=false)
    val showPercent  = bool("Show Percentage", default=false)
    val colorGood    = color("Good Color", r=85, g=255, b=85)
    val colorMid     = color("Mid Color", r=255, g=170, b=0)
    val colorBad     = color("Bad Color", r=255, g=85, b=85)
    val goodThresh   = slider("Good Threshold", default=60f, min=0f, max=100f, suffix="%")
    val badThresh    = slider("Bad Threshold", default=30f, min=0f, max=100f, suffix="%")
    val layout       = dropdown("Layout", options=arrayOf("Horizontal", "Vertical"), default=0)
    val showOffhand  = bool("Show Offhand", default=true)
    val showEmpty    = bool("Show Empty Slots", default=false)
    val iconSize     = slider("Icon Size", default=16f, min=8f, max=32f)
    val shadow       = bool("Text Shadow", default=true)
    val warnLow      = bool("Flash When Low", default=true)
    val warnThresh   = slider("Warn Threshold", default=20f, min=5f, max=50f, suffix="%")
    val bgColor      = color("Background", r=0, g=0, b=0, a=100)
    val showBg       = bool("Show Background", default=false)
    init { enable() }
}
