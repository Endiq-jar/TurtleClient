package com.endiq.client.modules.impl.pvp

import com.endiq.client.modules.Module
import java.util.UUID

class ComboCounterModule : Module("Combo Counter", "Tracks consecutive hits on a target", Category.PVP) {
    val textColor  = color("Text Color", r = 255, g = 255, b = 255)
    val comboColor = color("Combo Color", r = 224, g = 82, b = 82)
    val scale      = slider("Scale", default = 1f, min = 0.5f, max = 3f, suffix = "x")
    val posX       = slider("Position X", default = 50f, min = 0f, max = 100f, suffix = "%")
    val posY       = slider("Position Y", default = 70f, min = 0f, max = 100f, suffix = "%")
    val resetAfter = slider("Reset After", default = 2f, min = 0.5f, max = 5f, suffix = "s")
    val shadow     = bool("Text Shadow", default = true)
    val shakeOnHit = bool("Pop On Hit", default = true)

    var combo = 0
        private set
    var lastPopTicks = 0
    private var targetId: UUID? = null
    private var lastHitAt = 0L

    fun registerHit(target: UUID) {
        val now = System.currentTimeMillis()
        combo = if (target == targetId && now - lastHitAt <= (resetAfter.value * 1000).toLong()) combo + 1 else 1
        targetId = target
        lastHitAt = now
        if (shakeOnHit.value) lastPopTicks = 6
    }

    fun isExpired(): Boolean =
        System.currentTimeMillis() - lastHitAt > (resetAfter.value * 1000).toLong()

    init { enable() }
}
