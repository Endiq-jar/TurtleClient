package com.endiq.client.modules.impl.movement
import com.endiq.client.modules.Module
class SprintModule : Module("Sprint", "Auto-sprints when moving", Category.MOVEMENT) {
    val mode       = dropdown("Mode", options=arrayOf("Forward Only", "Omni-Sprint", "Toggle"), default=0)
    val cancelSneak= bool("Cancel On Sneak", default=true)
    val cancelPunch= bool("Sprint On Punch", default=false)
    val inWater    = bool("Sprint In Water", default=false)
    val minSpeed   = slider("Min Speed", default=0.1f, min=0f, max=1f)
}
