package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.Direction
class DirectionHudModule : Module("Direction HUD", "Shows facing direction", Category.HUD) {
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val bgColor     = color("Background", r=0, g=0, b=0, a=120)
    val showBg      = bool("Show Background", default=true)
    val showDegrees = bool("Show Degrees", default=false)
    val showArrow   = bool("Show Arrow", default=true)
    val style       = dropdown("Style", options=arrayOf("Cardinal", "Compass", "Degrees"), default=0)
    val scale       = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX        = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=92f, min=0f, max=100f, suffix="%")
    val shadow      = bool("Text Shadow", default=true)
    val northColor  = color("North Color", r=85, g=255, b=85)
    val southColor  = color("South Color", r=255, g=85, b=85)
    val eastColor   = color("East Color", r=85, g=85, b=255)
    val westColor   = color("West Color", r=255, g=170, b=0)
    fun getText(): String {
        val p = MinecraftClient.getInstance().player ?: return "Dir: N/A"
        val dir = Direction.fromRotation(p.yaw.toDouble()).getName().uppercase()
        return if (showArrow.value) "$dir ${getArrow(p.yaw)}" else dir
    }
    private fun getArrow(yaw: Float) = when {
        yaw >= -45 && yaw < 45 -> "↑"; yaw >= 45 && yaw < 135 -> "→"
        yaw >= -135 && yaw < -45 -> "←"; else -> "↓"
    }
}
