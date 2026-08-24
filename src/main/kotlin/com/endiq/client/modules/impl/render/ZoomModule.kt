package com.endiq.client.modules.impl.render
import com.endiq.client.modules.Module
import net.minecraft.client.MinecraftClient
class ZoomModule : Module("Zoom", "Zooms camera (hold C)", Category.RENDER) {
    val zoomFov    = slider("Zoom FOV", default=10f, min=1f, max=45f)
    val smoothZoom = bool("Smooth Zoom", default=true)
    val smoothSpeed= slider("Smooth Speed", default=0.3f, min=0.05f, max=1f)
    val zoomScroll = bool("Scroll to Adjust", default=true)
    val minFov     = slider("Min FOV (Scroll)", default=5f, min=1f, max=30f)
    val maxFov     = slider("Max FOV (Scroll)", default=45f, min=30f, max=90f)
    val scrollStep = slider("Scroll Step", default=5f, min=1f, max=20f)
    val reduceSens = bool("Reduce Sensitivity", default=true)
    val sensFactor = slider("Sensitivity Factor", default=0.3f, min=0.1f, max=1f)
    val zoomCinematic = bool("Cinematic Camera", default=false)
    val showFovNum = bool("Show Current FOV", default=false)
    private var orig = 70
    var isZooming = false
    var currentFov = 70f
    fun startZoom() {
        if (isZooming) return
        val o = MinecraftClient.getInstance().options
        orig = o.fov.value; o.fov.value = zoomFov.value.toInt(); isZooming = true; currentFov = zoomFov.value
    }
    fun stopZoom() {
        if (!isZooming) return
        MinecraftClient.getInstance().options.fov.value = orig; isZooming = false
    }
    override fun onDisable() { stopZoom() }
    init { enable() }
}
