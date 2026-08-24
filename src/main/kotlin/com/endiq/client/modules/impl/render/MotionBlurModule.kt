package com.endiq.client.modules.impl.render
import com.endiq.client.modules.Module
class MotionBlurModule : Module("Motion Blur", "Camera motion blur", Category.RENDER) {
    val strength    = slider("Strength", default=0.5f, min=0.1f, max=1f)
    val minAngle    = slider("Min Angle", default=0.5f, min=0f, max=5f)
    val maxBlur     = slider("Max Blur Alpha", default=180f, min=20f, max=255f)
    val yawOnly     = bool("Yaw Only", default=false)
    val pitchOnly   = bool("Pitch Only", default=false)
    val color       = color("Blur Color", r=0, g=0, b=0)
    var lastYaw = 0f; var lastPitch = 0f
}
