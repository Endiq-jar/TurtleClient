package com.endiq.client.modules.impl.render
import com.endiq.client.modules.Module
class TimeChangerModule : Module("Time Changer", "Locks world time", Category.RENDER) {
    val preset      = dropdown("Preset", options=arrayOf("Custom", "Day (6000)", "Noon (6000)", "Sunset (12000)", "Night (18000)", "Midnight (18000)"), default=1)
    val customTime  = slider("Custom Time", default=6000f, min=0f, max=24000f)
    val cycleSpeed  = slider("Cycle Speed", default=1f, min=0f, max=10f)
    val enableCycle = bool("Enable Time Cycle", default=false)
    var lockedTime = 6000L
    fun getTime() = if (preset.selected == 0) customTime.value.toLong()
        else listOf(6000L, 6000L, 12000L, 18000L, 18000L).getOrElse(preset.selected - 1) { 6000L }
}
