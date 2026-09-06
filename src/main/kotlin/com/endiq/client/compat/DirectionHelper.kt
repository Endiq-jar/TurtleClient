package com.endiq.client.compat

import kotlin.math.floor

// Minecraft yaw is 0=south, 90=west, 180=north, 270=east. Keep the
// presentation independent of Direction's renamed factories/accessors.
fun horizontalDirection(yaw: Float): String =
    when (Math.floorMod(floor(yaw / 90.0 + 0.5).toInt(), 4)) {
        0 -> "SOUTH"
        1 -> "WEST"
        2 -> "NORTH"
        else -> "EAST"
    }
