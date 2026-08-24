package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
import java.time.LocalTime
import java.time.format.DateTimeFormatter
class ClockHudModule : Module("Clock", "Shows real-time clock", Category.HUD) {
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val bgColor     = color("Background", r=0, g=0, b=0, a=120)
    val showBg      = bool("Show Background", default=true)
    val format24    = bool("24-Hour Format", default=false)
    val showSeconds = bool("Show Seconds", default=false)
    val showDate    = bool("Show Date", default=false)
    val dateFormat  = dropdown("Date Format", options=arrayOf("MM/DD", "DD/MM", "YYYY/MM/DD"), default=0)
    val scale       = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX        = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=82f, min=0f, max=100f, suffix="%")
    val shadow      = bool("Text Shadow", default=true)
    val showLabel   = bool("Show Label", default=false)
    val colorHours  = bool("Color Hours", default=false)
    val hoursColor  = color("Hours Color", r=62, g=153, b=112)
    fun getText(): String {
        val pat = if (format24.value) {
            if (showSeconds.value) "HH:mm:ss" else "HH:mm"
        } else {
            if (showSeconds.value) "hh:mm:ss a" else "hh:mm a"
        }
        return LocalTime.now().format(DateTimeFormatter.ofPattern(pat))
    }
}
