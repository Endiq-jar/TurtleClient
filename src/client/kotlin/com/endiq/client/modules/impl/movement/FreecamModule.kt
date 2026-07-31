package com.endiq.client.modules.impl.movement
import com.endiq.client.modules.Module
class FreecamModule : Module("Freelook", "Detaches camera / freelook", Category.MOVEMENT) {
    val speed      = slider("Speed", default=1f, min=0.1f, max=5f, suffix="x")
    val smoothing  = slider("Smoothing", default=0.5f, min=0f, max=1f)
    val fov        = slider("FOV", default=70f, min=30f, max=120f)
    val resetOnDisable = bool("Reset On Disable", default=true)
    val showPosition   = bool("Show Camera Position", default=false)
    val noclip         = bool("No Clip", default=false)
}
