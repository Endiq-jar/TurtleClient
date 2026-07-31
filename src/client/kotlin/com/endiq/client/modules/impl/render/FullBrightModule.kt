package com.endiq.client.modules.impl.render

import com.endiq.client.modules.Module
import net.minecraft.client.MinecraftClient

// NOTE: GameOptions.gamma is a SimpleOption<Double> as of 1.21.x mappings.
// If your local Yarn mappings type it differently, adjust the literals below.
class FullBrightModule : Module("Full Bright", "Maxes out brightness while enabled", Category.RENDER) {
    private var original = 1.0

    override fun onEnable() {
        val o = MinecraftClient.getInstance().options
        original = o.gamma.value
        o.gamma.value = 100.0
    }

    override fun onDisable() {
        MinecraftClient.getInstance().options.gamma.value = original
    }
}
