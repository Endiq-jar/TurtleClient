package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
import java.util.Locale
/** Precise health HUD: your own precise HP, nothing about other players. */
class HealthDisplayModule : Module("Health Display", "Shows your precise health and absorption", Category.HUD) {
    val textColor = color("Text Color", r=255, g=255, b=255)
    val bgColor   = color("Background", r=0, g=0, b=0, a=120)
    val showBg    = bool("Show Background", default=true)
    val showLabel = bool("Show Label", default=true)
    val showAbsorption = bool("Show Absorption", default=true)
    val scale     = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX      = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY      = slider("Position Y", default=87f, min=0f, max=100f, suffix="%")
    val shadow    = bool("Text Shadow", default=true)
    fun getText(health: Float, maxHealth: Float, absorption: Float = 0f): String {
        val hp = String.format(Locale.ROOT, "%.1f", health.coerceAtLeast(0f))
        val max = String.format(Locale.ROOT, "%.0f", maxHealth.coerceAtLeast(1f))
        val abs = if (showAbsorption.value && absorption > 0f)
            " +${String.format(Locale.ROOT, "%.1f", absorption)}" else ""
        return if (showLabel.value) "HP: $hp/$max$abs" else "$hp$abs"
    }
}
