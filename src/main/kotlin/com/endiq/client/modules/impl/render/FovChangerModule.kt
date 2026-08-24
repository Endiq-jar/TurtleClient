package com.endiq.client.modules.impl.render

import com.endiq.client.modules.Module
import net.minecraft.client.MinecraftClient

class FovChangerModule : Module("FOV Changer", "Overrides FOV beyond the vanilla slider", Category.RENDER) {
    val fov = slider("FOV", default = 90f, min = 30f, max = 130f)

    private var original = 70

    override fun onEnable() {
        val o = MinecraftClient.getInstance().options
        original = o.fov.value
        o.fov.value = fov.value.toInt()
    }

    override fun onDisable() {
        MinecraftClient.getInstance().options.fov.value = original
    }

    fun apply() {
        if (enabled) MinecraftClient.getInstance().options.fov.value = fov.value.toInt()
    }
}
