package com.endiq.client.modules.impl.utility
import com.endiq.client.modules.Module
class NickHiderModule : Module("Nick Hider", "Hides your username", Category.UTILITY) {
    val hideInChat    = bool("Hide In Chat", default=true)
    val hideInTab     = bool("Hide In Tablist", default=true)
    val hideInName    = bool("Hide Nametag", default=true)
    val hideInScoreboard = bool("Hide In Scoreboard", default=false)
    val replaceWith   = dropdown("Replace With", options=arrayOf("Custom Nick", "Asterisks", "Empty"), default=0)
    val style         = dropdown("Style", options=arrayOf("Static", "Rainbow", "Color Code"), default=0)
    val colorCode     = bool("Custom Color", default=false)
    val nickColor     = color("Nick Color", r=255, g=255, b=255)
    var nickname = "Steve"
}
