package com.endiq.client.modules.impl.render

import com.endiq.client.compat.*
import com.endiq.client.modules.Module

// NOTE: GameOptions.gamma is a SimpleOption<Double> as of 1.21.x mappings.
// If your local Yarn mappings type it differently, adjust the literals below.
class FullBrightModule : Module("Full Bright", "Maxes out brightness while enabled", Category.RENDER) {
    private var original = 1.0

    override fun onEnable() {
        val o = ClientOptions
        original = o.gamma
        o.gamma = 100.0
    }

    override fun onDisable() {
        ClientOptions.gamma = original
    }
}
