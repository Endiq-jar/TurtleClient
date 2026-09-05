package com.endiq.client.modules.impl.performance

import com.endiq.client.compat.*
import com.endiq.client.modules.Module
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

// Scales view (render) distance -- and optionally simulation distance -- down
// when FPS falls under a target, back up when there's headroom. Lets low-end
// hardware trade draw distance for framerate automatically instead of the
// player having to dig into video settings mid-fight.
//
// Simulation Distance didn't exist as a concept separate from Render Distance
// before the 1.18 options split, so this module is meaningless below 1.18.2 --
// nothing in stonecutter.properties.toml goes that low anyway (1.17.1 was
// removed), so no version gating needed here.
//
// NOTE: GameOptions.viewDistance / simulationDistance are SimpleOption<Integer>
// as of 1.21.x mappings, same family as gamma/fov in FullBrightModule /
// FovChangerModule. Adjust the `.value` accesses below if your local Yarn
// mappings type these differently.
class DynamicRenderDistanceModule : Module(
    "Dynamic Render Distance",
    "Automatically lowers/raises render (and simulation) distance to hit a target FPS",
    Category.PERFORMANCE
) {
    private val targetFps = slider("Target FPS", default = 60f, min = 20f, max = 240f, suffix = " fps")
    private val tolerance = slider("Tolerance", "How far below/above target before adjusting", default = 5f, min = 0f, max = 30f, suffix = " fps")
    private val minView = slider("Min View Distance", default = 4f, min = 2f, max = 32f, suffix = "c")
    private val maxView = slider("Max View Distance", default = 12f, min = 2f, max = 32f, suffix = "c")
    private val stepSize = slider("Step Size", default = 1f, min = 1f, max = 4f, suffix = "c")
    private val stepInterval = slider("Adjust Interval", "How often to re-check FPS and step", default = 3f, min = 1f, max = 10f, suffix = "s")
    private val scaleSimulation = bool("Scale Simulation Distance", "Also shrink/grow simulation distance alongside view distance", default = true)

    private var originalView = -1
    private var originalSim = -1
    private var ticksSinceAdjust = 0

    init {
        // Always registered, gated internally by `enabled` -- same pattern as
        // CullingModule, so toggling doesn't need to add/remove listeners.
        ClientTickEvents.END_CLIENT_TICK.register { onTick() }
    }

    override fun onEnable() {
        val o = ClientOptions
        originalView = o.viewDistance
        originalSim = o.simulationDistance
        ticksSinceAdjust = 0
    }

    override fun onDisable() {
        val o = ClientOptions
        if (originalView >= 0) o.viewDistance = originalView
        if (originalSim >= 0) o.simulationDistance = originalSim
        originalView = -1
        originalSim = -1
    }

    private fun onTick() {
        if (!enabled) return
        ticksSinceAdjust++
        // 20 ticks/sec baseline.
        val intervalTicks = (stepInterval.value * 20f).toInt().coerceAtLeast(20)
        if (ticksSinceAdjust < intervalTicks) return
        ticksSinceAdjust = 0

        val client = MinecraftClient.getInstance()
        if (client.world == null) return // no session active (e.g. main menu)
        val fps = clientFps()
        val o = ClientOptions

        val lo = minView.value.toInt().coerceAtLeast(2)
        val hi = maxView.value.toInt().coerceAtLeast(lo)
        val delta = stepSize.value.toInt().coerceAtLeast(1)
        val current = o.viewDistance.coerceIn(lo, hi)

        val newView = when {
            fps < targetFps.value - tolerance.value -> (current - delta).coerceIn(lo, hi)
            fps > targetFps.value + tolerance.value -> (current + delta).coerceIn(lo, hi)
            else -> current
        }

        if (newView != o.viewDistance) {
            o.viewDistance = newView
            if (scaleSimulation.value) o.simulationDistance = newView
        }
    }
}
