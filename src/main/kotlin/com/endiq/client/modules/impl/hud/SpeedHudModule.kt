package com.endiq.client.modules.impl.hud
import com.endiq.client.compat.*
import com.endiq.client.modules.Module
import kotlin.math.sqrt
class SpeedHudModule : Module("Speedometer", "Shows movement speed", Category.HUD) {
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val bgColor     = color("Background", r=0, g=0, b=0, a=120)
    val showBg      = bool("Show Background", default=true)
    val unit        = dropdown("Unit", options=arrayOf("m/s", "km/h", "mph", "BPS"), default=0)
    val precision   = slider("Decimal Places", default=2f, min=0f, max=4f)
    val scale       = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX        = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=62f, min=0f, max=100f, suffix="%")
    val showMax     = bool("Show Max Speed", default=false)
    val showBar     = bool("Show Speed Bar", default=false)
    val maxSpeed    = slider("Max Speed", default=20f, min=5f, max=100f, suffix="m/s")
    val barColor    = color("Bar Color", r=62, g=153, b=112)
    val colorCode   = bool("Color Code Speed", default=false)
    val shadow      = bool("Text Shadow", default=true)
    val includeVert = bool("Include Vertical Speed", default=false)
    fun getText(): String {
        val p = MinecraftClient.getInstance().player ?: return "Speed: 0.00"
        val h = sqrt(p.velocity.x * p.velocity.x + p.velocity.z * p.velocity.z)
        val v = if (includeVert.value) sqrt(h * h + p.velocity.y * p.velocity.y) else h
        val fmt = "%.${precision.value.toInt()}f"
        val converted = when(unit.selected) { 1 -> v * 3.6; 2 -> v * 2.237; else -> v }
        return "Speed: ${fmt.format(converted)} ${unit.value}"
    }
}
