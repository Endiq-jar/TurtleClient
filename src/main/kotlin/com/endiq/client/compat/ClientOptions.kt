package com.endiq.client.compat

// SimpleOption replaced primitive GameOptions fields in 1.19; Mojang names use
// OptionInstance. Keep those representations out of individual modules.
object ClientOptions {
    var fov: Int
//? if >=26.1 {
/*        get() = MinecraftClient.getInstance().options.fov().get()
        set(value) { MinecraftClient.getInstance().options.fov().set(value) }
*///?} else if >=1.19 {
        get() = MinecraftClient.getInstance().options.fov.value
        set(value) { MinecraftClient.getInstance().options.fov.value = value }
//?} else {
/*        get() = MinecraftClient.getInstance().options.fov.toInt()
        set(value) {
            MinecraftClient.getInstance().options.fov = value.toDouble()
        }
*///?}

    var gamma: Double
//? if >=26.1 {
/*        get() = MinecraftClient.getInstance().options.gamma().get()
        set(value) { MinecraftClient.getInstance().options.gamma().set(value) }
*///?} else if >=1.19 {
        get() = MinecraftClient.getInstance().options.gamma.value
        set(value) { MinecraftClient.getInstance().options.gamma.value = value }
//?} else {
/*        get() = MinecraftClient.getInstance().options.gamma
        set(value) {
            MinecraftClient.getInstance().options.gamma = value
        }
*///?}

    var vsync: Boolean
//? if >=26.1 {
/*        get() = MinecraftClient.getInstance().options.enableVsync().get()
        set(value) { MinecraftClient.getInstance().options.enableVsync().set(value) }
*///?} else if >=1.19 {
        get() = MinecraftClient.getInstance().options.enableVsync.value
        set(value) { MinecraftClient.getInstance().options.enableVsync.value = value }
//?} else {
/*        get() = MinecraftClient.getInstance().options.enableVsync
        set(value) {
            MinecraftClient.getInstance().options.enableVsync = value
            MinecraftClient.getInstance().window.setVsync(value)
        }
*///?}

    var viewDistance: Int
//? if >=26.1 {
/*        get() = MinecraftClient.getInstance().options.renderDistance().get()
        set(value) { MinecraftClient.getInstance().options.renderDistance().set(value) }
*///?} else if >=1.19 {
        get() = MinecraftClient.getInstance().options.viewDistance.value
        set(value) { MinecraftClient.getInstance().options.viewDistance.value = value }
//?} else {
/*        get() = MinecraftClient.getInstance().options.viewDistance
        set(value) {
            MinecraftClient.getInstance().options.viewDistance = value
        }
*///?}

    var simulationDistance: Int
//? if >=26.1 {
/*        get() = MinecraftClient.getInstance().options.simulationDistance().get()
        set(value) { MinecraftClient.getInstance().options.simulationDistance().set(value) }
*///?} else if >=1.19 {
        get() = MinecraftClient.getInstance().options.simulationDistance.value
        set(value) { MinecraftClient.getInstance().options.simulationDistance.value = value }
//?} else {
/*        get() = MinecraftClient.getInstance().options.simulationDistance
        set(value) {
            MinecraftClient.getInstance().options.simulationDistance = value
        }
*///?}

}
