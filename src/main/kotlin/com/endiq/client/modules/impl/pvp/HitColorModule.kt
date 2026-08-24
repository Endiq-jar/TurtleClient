package com.endiq.client.modules.impl.pvp
import com.endiq.client.modules.Module
class HitColorModule : Module("Hit Color", "Custom hurt flash color", Category.PVP) {
    val hitColor    = color("Hit Color", r=255, g=0, b=0, a=100)
    val ownHitColor = color("Own Hit Color", r=255, g=255, b=0, a=100)
    val duration    = slider("Duration", default=10f, min=1f, max=30f)
    val intensity   = slider("Intensity", default=1f, min=0.1f, max=2f)
    val colorMode   = dropdown("Color Mode", options=arrayOf("Static", "Rainbow", "Random"), default=0)
    val affectSelf  = bool("Affect Self", default=true)
    val affectMobs  = bool("Affect Mobs", default=true)
    val affectPlayers=bool("Affect Players", default=true)
    val flash       = bool("Flash Effect", default=false)
    val flashSpeed  = slider("Flash Speed", default=5f, min=1f, max=20f)

    // Live flash state, driven by the attack-entity hook in TurtleClientClient
    var flashTicks = 0
        private set

    fun trigger() { flashTicks = duration.value.toInt().coerceAtLeast(1) }
    fun tick() { if (flashTicks > 0) flashTicks-- }

    init { enable() }
}
