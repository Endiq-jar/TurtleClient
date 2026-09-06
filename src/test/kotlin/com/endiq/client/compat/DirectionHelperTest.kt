package com.endiq.client.compat

import kotlin.test.Test
import kotlin.test.assertEquals

class DirectionHelperTest {
    @Test
    fun cardinalDirectionsUseMinecraftYaw() {
        assertEquals("SOUTH", horizontalDirection(0f))
        assertEquals("WEST", horizontalDirection(90f))
        assertEquals("NORTH", horizontalDirection(180f))
        assertEquals("EAST", horizontalDirection(270f))
    }

    @Test
    fun negativeAndRepeatedRotationsWrap() {
        assertEquals("EAST", horizontalDirection(-90f))
        assertEquals("NORTH", horizontalDirection(-180f))
        assertEquals("WEST", horizontalDirection(-270f))
        assertEquals("SOUTH", horizontalDirection(-360f))
        assertEquals("SOUTH", horizontalDirection(720f))
        assertEquals("WEST", horizontalDirection(810f))
    }

    @Test
    fun diagonalBoundariesMatchVanillaRounding() {
        assertEquals("SOUTH", horizontalDirection(44.99f))
        assertEquals("WEST", horizontalDirection(45f))
        assertEquals("WEST", horizontalDirection(134.99f))
        assertEquals("NORTH", horizontalDirection(135f))
        assertEquals("EAST", horizontalDirection(225f))
        assertEquals("SOUTH", horizontalDirection(315f))
        assertEquals("SOUTH", horizontalDirection(-45f))
        assertEquals("EAST", horizontalDirection(-45.01f))
    }
}
