package com.endiq.client.modules.impl.hypixel
import com.endiq.client.modules.Module
class TabStatModule : Module("Tab Stat", "Stats in tab list", Category.HYPIXEL) {
    val showLevel   = bool("Show Level", default=true)
    val showKd      = bool("Show K/D", default=true)
    val showWins    = bool("Show Wins", default=true)
    val showFkdr    = bool("Show FKDR", default=true)
    val showWlr     = bool("Show WLR", default=false)
    val showBbls    = bool("Show Beds Broken", default=false)
    val showRank    = bool("Show Rank", default=true)
    val colorByRank = bool("Color By Rank", default=true)
    val hideNicked  = bool("Mark Nicked Players", default=true)
    val nickedColor = color("Nicked Color", r=128, g=128, b=128)
}
