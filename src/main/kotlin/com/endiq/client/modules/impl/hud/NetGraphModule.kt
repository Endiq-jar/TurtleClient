package com.endiq.client.modules.impl.hud

import com.endiq.client.compat.*
import com.endiq.client.modules.Module

class NetGraphModule : Module("Net Graph", "Ping history graph", Category.HUD) {
    val barColor    = color("Bar Color", r = 61, g = 153, b = 112)
    val badColor    = color("Bad Color", r = 224, g = 82, b = 82)
    val bgColor     = color("Background", r = 0, g = 0, b = 0, a = 120)
    val posX        = slider("Position X", default = 2f, min = 0f, max = 100f, suffix = "%")
    val posY        = slider("Position Y", default = 44f, min = 0f, max = 100f, suffix = "%")
    val width       = slider("Width", default = 60f, min = 30f, max = 200f)
    val height      = slider("Height", default = 20f, min = 10f, max = 60f)
    val sampleTicks = slider("Sample Every", default = 10f, min = 2f, max = 40f, suffix = "t")
    val badAbove    = slider("Bad Above", default = 150f, min = 50f, max = 500f, suffix = "ms")
    val historyLen  = slider("History Length", default = 40f, min = 10f, max = 100f)

    val samples = ArrayDeque<Int>()
    private var tickCounter = 0

    fun onTick() {
        tickCounter++
        if (tickCounter < sampleTicks.value.toInt().coerceAtLeast(1)) return
        tickCounter = 0
        val ping = clientPing() ?: 0
        samples.addLast(ping)
        while (samples.size > historyLen.value.toInt().coerceAtLeast(1)) samples.removeFirst()
    }

    init { enable() }
}
