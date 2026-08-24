package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class BlockOverlayModule : Module("Block Overlay", "Overlay on targeted block", Category.HUD) {
    val bgColor     = color("Background", r=0, g=0, b=0, a=140)
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val showBg      = bool("Show Background", default=true)
    val posOffsetX  = slider("X Offset", default=0f, min=-50f, max=50f)
    val posOffsetY  = slider("Y Offset", default=24f, min=0f, max=80f)
    val scale       = slider("Scale", default=1f, min=0.5f, max=2f)
    val shadow      = bool("Text Shadow", default=true)
    val bgPadding   = slider("BG Padding", default=4f, min=0f, max=12f)
    val style       = dropdown("Style", options=arrayOf("Name", "Name + Coords", "Full"), default=0)
}
