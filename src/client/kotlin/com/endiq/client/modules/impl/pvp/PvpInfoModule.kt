package com.endiq.client.modules.impl.pvp
import com.endiq.client.modules.Module
class PvpInfoModule : Module("PvP Info", "Shows nearby enemy info", Category.PVP) {
    val textColor    = color("Text Color", r=255, g=255, b=255)
    val headerColor  = color("Header Color", r=224, g=82, b=82)
    val bgColor      = color("Background", r=0, g=0, b=0, a=140)
    val showBg       = bool("Show Background", default=true)
    val showHealth   = bool("Show Health", default=true)
    val showDist     = bool("Show Distance", default=true)
    val showArmor    = bool("Show Armor", default=false)
    val showName     = bool("Show Name", default=true)
    val maxDist      = slider("Max Distance", default=20f, min=5f, max=64f, suffix="m")
    val maxPlayers   = slider("Max Players", default=5f, min=1f, max=15f)
    val scale        = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX         = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY         = slider("Position Y", default=25f, min=0f, max=100f, suffix="%")
    val sortBy       = dropdown("Sort By", options=arrayOf("Distance", "Health", "Name"), default=0)
    val colorHealth  = bool("Color By Health", default=true)
    val shadow       = bool("Text Shadow", default=true)
    val showOnlyEnemy= bool("Only Enemies", default=false)
}
