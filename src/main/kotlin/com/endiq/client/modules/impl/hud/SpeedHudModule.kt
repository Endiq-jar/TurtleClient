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
        val p = MinecraftClient.getInstance().player ?: return "Speed: N/A"
        return formatVelocity(p.velocity.x,p.velocity.y,p.velocity.z)
    }
    /** Minecraft velocity is blocks/tick, not blocks/second. */
    fun formatVelocity(x:Double,y:Double,z:Double):String {
        val bps=sqrt(x*x+z*z+if(includeVert.value)y*y else 0.0)*20.0
        val converted=when(unit.selected) { 1->bps*3.6;2->bps*2.2369362921;else->bps }
        return "Speed: ${java.lang.String.format(java.util.Locale.ROOT,"%.${precision.value.toInt().coerceIn(0,4)}f",converted)} ${unit.value}"
    }
}
