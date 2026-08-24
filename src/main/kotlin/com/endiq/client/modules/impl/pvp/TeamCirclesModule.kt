package com.endiq.client.modules.impl.pvp
import com.endiq.client.modules.Module
class TeamCirclesModule : Module("Team Circles", "Circles under teammates", Category.PVP) {
    val circleColor = color("Circle Color", r=62, g=153, b=112, a=180)
    val enemyColor  = color("Enemy Color", r=224, g=82, b=82, a=180)
    val radius      = slider("Radius", default=0.6f, min=0.2f, max=2f)
    val lineWidth   = slider("Line Width", default=2f, min=1f, max=5f)
    val showEnemy   = bool("Show Enemies", default=false)
    val showTeam    = bool("Show Teammates", default=true)
    val fill        = bool("Fill Circle", default=false)
    val fillAlpha   = slider("Fill Alpha", default=40f, min=0f, max=200f)
    val style       = dropdown("Style", options=arrayOf("Circle", "Square", "Diamond"), default=0)
    val maxDist     = slider("Max Distance", default=50f, min=10f, max=200f, suffix="m")
    val pulse       = bool("Pulse Effect", default=false)
    val pulseSpeed  = slider("Pulse Speed", default=1f, min=0.1f, max=5f)
}
