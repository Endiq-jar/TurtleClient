package com.endiq.client.modules.impl.utility
import com.endiq.client.modules.Module
/**
 * Tool warning: warns YOU about YOUR gear wearing out.
 * State only decides when to warn; the popup itself is Popup Events.
 */
class ToolWarningModule(
    private val now: () -> Long = { System.currentTimeMillis() }
) : Module("Tool Warning", "Warns when your held tool or armor is about to break", Category.UTILITY) {
    val threshold = slider("Warn Below", default=10f, min=1f, max=50f, suffix="%")
    val checkArmor = bool("Check Armor", default=true)
    val cooldownSeconds = slider("Cooldown", default=60f, min=10f, max=300f, suffix="s")
    private var lastWarnAt = Long.MIN_VALUE / 2
    private var lastSignature = ""
    fun durabilityLeft(damage: Int, maxDamage: Int): Float =
        if (maxDamage <= 0) 1f else ((maxDamage - damage).coerceAtLeast(0)).toFloat() / maxDamage
    fun isLow(damage: Int, maxDamage: Int): Boolean =
        maxDamage > 0 && durabilityLeft(damage, maxDamage) * 100f <= threshold.value
    /** One warning per item per cooldown window; swapping items re-arms it. */
    fun shouldWarn(signature: String): Boolean {
        val time = now()
        if (signature == lastSignature && time - lastWarnAt < cooldownSeconds.value * 1000) return false
        lastSignature = signature; lastWarnAt = time; return true
    }
    override fun onDisable() { lastSignature = ""; lastWarnAt = Long.MIN_VALUE / 2 }
}
