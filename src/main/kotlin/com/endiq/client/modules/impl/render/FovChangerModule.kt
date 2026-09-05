package com.endiq.client.modules.impl.render

import com.endiq.client.compat.*
import com.endiq.client.modules.Module

class FovChangerModule : Module("FOV Changer", "Overrides FOV beyond the vanilla slider", Category.RENDER) {
    val fov = slider("FOV", default = 90f, min = 30f, max = 130f)

    private var original = 70

    override fun onEnable() {
        val o = ClientOptions
        original = o.fov
        o.fov = fov.value.toInt()
    }

    override fun onDisable() {
        ClientOptions.fov = original
    }

    fun apply() {
        if (enabled) ClientOptions.fov = fov.value.toInt()
    }
}
