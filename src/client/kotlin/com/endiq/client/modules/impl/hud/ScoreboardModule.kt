package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class ScoreboardModule : Module("Scoreboard", "Custom scoreboard styling", Category.HUD) {
    val bgColor      = color("Background", r=0, g=0, b=0, a=140)
    val titleColor   = color("Title Color", r=255, g=255, b=85)
    val textColor    = color("Text Color", r=255, g=255, b=255)
    val scoreColor   = color("Score Color", r=255, g=85, b=85)
    val titleBg      = color("Title Background", r=0, g=0, b=0, a=180)
    val showBg       = bool("Show Background", default=true)
    val showTitle    = bool("Show Title", default=true)
    val showScores   = bool("Show Scores", default=true)
    val posX         = slider("Position X", default=98f, min=0f, max=100f, suffix="%")
    val posY         = slider("Position Y", default=10f, min=0f, max=100f, suffix="%")
    val scale        = slider("Scale", default=1f, min=0.5f, max=2f, suffix="x")
    val maxEntries   = slider("Max Entries", default=15f, min=1f, max=20f)
    val shadow       = bool("Text Shadow", default=true)
    val outline      = bool("Show Outline", default=false)
    val outlineColor = color("Outline Color", r=62, g=153, b=112)
    val style        = dropdown("Style", options=arrayOf("Default", "Compact", "Modern"), default=0)
    val padding      = slider("Padding", default=4f, min=0f, max=12f)
    val hideNumbers  = bool("Hide Numbers", default=false)
    init { enable() }
}
