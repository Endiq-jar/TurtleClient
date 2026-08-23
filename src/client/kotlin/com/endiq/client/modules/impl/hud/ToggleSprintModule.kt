package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class ToggleSprintModule : Module("Toggle Sprint", "Shows sprint toggle state", Category.HUD) {
    val onColor    = color("On Color", r=62, g=153, b=112)
    val offColor   = color("Off Color", r=224, g=82, b=82)
    val textColor  = color("Text Color", r=255, g=255, b=255)
    val bgColor    = color("Background", r=0, g=0, b=0, a=120)
    val showBg     = bool("Show Background", default=true)
    val style      = dropdown("Style", options=arrayOf("Sprint: ON/OFF", "Sprinting/Walking", "Icon"), default=0)
    val scale      = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX       = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY       = slider("Position Y", default=112f, min=0f, max=100f, suffix="%")
    val shadow     = bool("Text Shadow", default=true)
    val showSneakToggle = bool("Show Sneak Toggle", default=false)
    var sprintOn = false
    fun getText() = when(style.selected) {
        1 -> if (sprintOn) "Sprinting" else "Walking"
        else -> "Sprint: ${if (sprintOn) "ON" else "OFF"}"
    }
    init { enable() }
}
