package com.endiq.client.modules.impl.utility

import com.endiq.client.modules.Module

class TeamViewModule : Module("Team View", "Lists nearby teammates by scoreboard team", Category.UTILITY) {
    val textColor  = color("Text Color", r = 255, g = 255, b = 255)
    val headerColor= color("Header Color", r = 61, g = 153, b = 112)
    val bgColor    = color("Background", r = 0, g = 0, b = 0, a = 140)
    val showBg     = bool("Show Background", default = true)
    val maxDist    = slider("Max Distance", default = 40f, min = 5f, max = 128f, suffix = "m")
    val maxPlayers = slider("Max Players", default = 8f, min = 1f, max = 20f)
    val posX       = slider("Position X", default = 2f, min = 0f, max = 100f, suffix = "%")
    val posY       = slider("Position Y", default = 90f, min = 0f, max = 100f, suffix = "%")
    val shadow     = bool("Text Shadow", default = true)
}
