package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class BlockIndicatorModule : Module("Block Indicator", "Shows block you look at", Category.HUD) {
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val bgColor     = color("Background", r=0, g=0, b=0, a=140)
    val showBg      = bool("Show Background", default=true)
    val showId      = bool("Show Block ID", default=false)
    val showMeta    = bool("Show Metadata", default=false)
    val showHardness= bool("Show Hardness", default=false)
    val maxDist     = slider("Max Distance", default=5f, min=1f, max=10f, suffix="m")
    val scale       = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX        = slider("Position X", default=50f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=60f, min=0f, max=100f, suffix="%")
    val shadow      = bool("Text Shadow", default=true)
    val style       = dropdown("Style", options=arrayOf("Name Only", "With Icon", "Full Info"), default=0)
    val showCoords  = bool("Show Block Coords", default=false)
    val fadeOut     = bool("Fade When No Block", default=true)
    val prefix      = bool("Show Prefix", default=true)
    var blockName = ""
}
