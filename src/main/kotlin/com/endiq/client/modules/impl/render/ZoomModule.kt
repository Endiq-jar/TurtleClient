package com.endiq.client.modules.impl.render

import com.endiq.client.modules.Module
import com.endiq.client.compat.*

class ZoomModule : Module("Zoom","Hold C to zoom; scroll adjusts the zoom level",Category.RENDER) {
    val zoomFov=slider("Zoom FOV",default=10f,min=1f,max=45f)
    val smoothZoom=bool("Smooth Zoom",default=true)
    val smoothSpeed=slider("Smooth Speed",default=.3f,min=.05f,max=1f)
    val zoomScroll=bool("Scroll to Adjust",default=true)
    val minFov=slider("Min FOV (Scroll)",default=5f,min=1f,max=30f)
    val maxFov=slider("Max FOV (Scroll)",default=45f,min=30f,max=90f)
    val scrollStep=slider("Scroll Step",default=5f,min=1f,max=20f)
    val reduceSens=bool("Reduce Sensitivity",default=true)
    val sensFactor=slider("Sensitivity Factor",default=.3f,min=.1f,max=1f)
    val zoomCinematic=bool("Cinematic Camera",default=false)
    val showFovNum=bool("Show Current FOV",default=false)
    var isZooming=false
        private set
    var progress=0f
        private set
    var currentFov=10f
        private set
    private var sensitivity:Double?=null
    private var cinematic=false
    fun tick(pressed:Boolean) {
        val held=enabled && pressed
        if(held && !isZooming) { currentFov=zoomFov.value;sensitivity=ClientOptions.sensitivity;cinematic=ClientOptions.cinematic }
        isZooming=held
        val target=if(held)1f else 0f
        progress=if(smoothZoom.value)progress+(target-progress)*smoothSpeed.value else target
        if(kotlin.math.abs(progress-target)<.005f)progress=target
        sensitivity?.let { original ->
            if(held) {
                ClientOptions.sensitivity=if(reduceSens.value)original*(1-progress*(1-sensFactor.value)) else original
                ClientOptions.cinematic=cinematic || zoomCinematic.value
            } else { ClientOptions.sensitivity=original;ClientOptions.cinematic=cinematic;sensitivity=null }
        }
    }
    fun scroll(amount:Double):Boolean {
        if(!enabled || !isZooming || !zoomScroll.value || !amount.isFinite() || amount==0.0)return false
        currentFov=(currentFov-amount.toFloat()*scrollStep.value).coerceIn(minFov.value,maxFov.value)
        return true
    }
    fun stopZoom() { tick(false);progress=0f }
    override fun onDisable()=stopZoom()
    init { enable() }
}
