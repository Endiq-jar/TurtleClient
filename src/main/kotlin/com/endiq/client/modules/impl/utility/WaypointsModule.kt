package com.endiq.client.modules.impl.utility
import com.endiq.client.modules.Module
import net.minecraft.util.math.BlockPos
class WaypointsModule : Module("Waypoints", "Set waypoints with labels", Category.UTILITY) {
    val showLabels  = bool("Show Labels", default=true)
    val showDist    = bool("Show Distance", default=true)
    val showBeacon  = bool("Show Beacon", default=true)
    val showOnMap   = bool("Show On Map", default=true)
    val maxDist     = slider("Max Distance", default=500f, min=50f, max=5000f, suffix="m")
    val minDist     = slider("Min Distance", default=1f, min=0f, max=20f, suffix="m")
    val scale       = slider("Label Scale", default=1f, min=0.3f, max=3f, suffix="x")
    val bgAlpha     = slider("Label BG Alpha", default=140f, min=0f, max=255f)
    val beaconHeight= slider("Beacon Height", default=3f, min=0f, max=20f)
    val fadeWithDist= bool("Fade With Distance", default=true)
    val tracerLine  = bool("Tracer Line", default=false)
    val tracerAlpha = slider("Tracer Alpha", default=100f, min=0f, max=255f)
    val shadow      = bool("Text Shadow", default=true)
    data class Waypoint(val name: String, val pos: BlockPos, val color: Int = 0xFF00FF00.toInt(), val enabled: Boolean = true)
    val waypoints = mutableListOf<Waypoint>()
    init { enable() }
}
