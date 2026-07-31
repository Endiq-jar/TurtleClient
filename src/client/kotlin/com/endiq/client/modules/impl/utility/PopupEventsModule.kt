package com.endiq.client.modules.impl.utility
import com.endiq.client.modules.Module
class PopupEventsModule : Module("Popup Events", "Shows popup notifications", Category.UTILITY) {
    val bgColor       = color("Background", r=20, g=20, b=20, a=200)
    val textColor     = color("Text Color", r=255, g=255, b=255)
    val accentColor   = color("Accent Color", r=62, g=153, b=112)
    val duration      = slider("Duration", default=4f, min=1f, max=10f, suffix="s")
    val fadeTime      = slider("Fade Time", default=0.5f, min=0.1f, max=2f, suffix="s")
    val posX          = slider("Position X", default=50f, min=0f, max=100f, suffix="%")
    val posY          = slider("Position Y", default=15f, min=0f, max=100f, suffix="%")
    val maxPopups     = slider("Max Popups", default=5f, min=1f, max=10f)
    val showIcon      = bool("Show Icon", default=true)
    val sound         = bool("Play Sound", default=false)
    val style         = dropdown("Style", options=arrayOf("Toast", "Banner", "Minimal"), default=0)
    val onJoin        = bool("On Player Join", default=true)
    val onLeave       = bool("On Player Leave", default=true)
    val onMessage     = bool("On Message", default=false)
    data class Popup(val msg: String, var ticks: Int = 80)
    val popups = ArrayDeque<Popup>()
    fun add(msg: String) { popups.addLast(Popup(msg, (duration.value * 20).toInt())) }
    init { enable() }
}
