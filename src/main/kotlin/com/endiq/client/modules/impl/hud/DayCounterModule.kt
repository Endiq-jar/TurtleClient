package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
/** Clock/day HUD: the in-game day count, not a cheat. */
class DayCounterModule : Module("Day Counter", "Shows the current Minecraft day", Category.HUD) {
    val textColor = color("Text Color", r=255, g=255, b=255)
    val bgColor   = color("Background", r=0, g=0, b=0, a=120)
    val showBg    = bool("Show Background", default=true)
    val showLabel = bool("Show Label", default=true)
    val scale     = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX      = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY      = slider("Position Y", default=92f, min=0f, max=100f, suffix="%")
    val shadow    = bool("Text Shadow", default=true)
    /** Pure so tests can pin it: day 1 starts at world time 0. */
    fun getText(worldTime: Long): String {
        val day = worldTime.coerceAtLeast(0) / 24000 + 1
        return if (showLabel.value) "Day: $day" else day.toString()
    }
}
