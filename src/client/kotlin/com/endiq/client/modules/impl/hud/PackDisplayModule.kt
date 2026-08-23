package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class PackDisplayModule : Module("Pack Display", "Shows resource pack name", Category.HUD) {
    val textColor  = color("Text Color", r=255, g=255, b=255)
    val bgColor    = color("Background", r=0, g=0, b=0, a=120)
    val showBg     = bool("Show Background", default=true)
    val showLabel  = bool("Show Label", default=true)
    val showIcon   = bool("Show Pack Icon", default=false)
    val truncate   = slider("Max Length", default=20f, min=5f, max=50f)
    val scale      = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX       = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY       = slider("Position Y", default=102f, min=0f, max=100f, suffix="%")
    val shadow     = bool("Text Shadow", default=true)
    val showAll    = bool("Show All Packs", default=false)
    var packName = "Default"
    init { enable() }
}
