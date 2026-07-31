package com.endiq.client.modules.impl.utility
import com.endiq.client.modules.Module
class TimersModule : Module("Stopwatch / Timer", "Countdown timer on HUD", Category.UTILITY) {
    val textColor   = color("Text Color", r=255, g=255, b=0)
    val bgColor     = color("Background", r=0, g=0, b=0, a=140)
    val showBg      = bool("Show Background", default=true)
    val scale       = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX        = slider("Position X", default=50f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=8f, min=0f, max=100f, suffix="%")
    val showMs      = bool("Show Milliseconds", default=false)
    val soundAlert  = bool("Sound On Complete", default=true)
    val alertColor  = color("Alert Color", r=255, g=85, b=85)
    val countUp     = bool("Count Up Mode", default=false)
    val showLabel   = bool("Show Label", default=true)
    val defaultTime = slider("Default Time", default=60f, min=1f, max=3600f, suffix="s")
    val shadow      = bool("Text Shadow", default=true)
    var timerMs = 0L; var startMs = 0L; var running = false
    fun start(s: Int = defaultTime.value.toInt()) { timerMs = s * 1000L; startMs = System.currentTimeMillis(); running = true }
    fun stop() { running = false }
    fun remaining() = if (!running) 0L else (timerMs - (System.currentTimeMillis() - startMs)).coerceAtLeast(0)
}
