package com.endiq.client.modules.impl.performance

import com.endiq.client.modules.Module
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient

// Flips vanilla's VSync option on once FPS comfortably clears a threshold
// (caps GPU/battery usage once frames are cheap to spare) and back off below
// it (so a heavy scene never gets vsync-capped below its uncapped framerate).
// TurtleLauncher already does adaptive vsync for the whole game window at the
// renderer level; this is the client-mod-side counterpart for people running
// TurtleClient without TurtleLauncher.
//
// NOTE: GameOptions.enableVsync is SimpleOption<Boolean> as of 1.21.x
// mappings, same family as gamma/fov in FullBrightModule / FovChangerModule.
class AdaptiveVsyncModule : Module(
    "Adaptive VSync",
    "Enables VSync once FPS clears a threshold, disables it below -- caps GPU usage without capping headroom when you need it",
    Category.PERFORMANCE
) {
    private val threshold = slider("FPS Threshold", default = 90f, min = 30f, max = 240f, suffix = " fps")
    private val checkInterval = slider("Check Interval", default = 1f, min = 0.5f, max = 5f, suffix = "s")

    private var original = false
    private var ticksSinceCheck = 0

    init {
        ClientTickEvents.END_CLIENT_TICK.register { onTick() }
    }

    override fun onEnable() {
        original = MinecraftClient.getInstance().options.enableVsync.value
        ticksSinceCheck = 0
    }

    override fun onDisable() {
        MinecraftClient.getInstance().options.enableVsync.value = original
    }

    private fun onTick() {
        if (!enabled) return
        ticksSinceCheck++
        val intervalTicks = (checkInterval.value * 20f).toInt().coerceAtLeast(10)
        if (ticksSinceCheck < intervalTicks) return
        ticksSinceCheck = 0

        val client = MinecraftClient.getInstance()
        val shouldVsync = client.currentFps >= threshold.value
        val o = client.options
        if (o.enableVsync.value != shouldVsync) o.enableVsync.value = shouldVsync
    }
}
