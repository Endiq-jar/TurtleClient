package com.endiq.client.gui.components

import kotlin.math.roundToInt

/** Retains sub-pixel wheel input; never truncate each individual wheel event. */
class ScrollState(private val pixelsPerStep: Double = 28.0) {
    var offset: Double = 0.0
        private set
    var maximum: Double = 0.0
        private set
    var dragging: Boolean = false
        private set
    private var grabOffset = 0.0
    val pixels get() = offset.roundToInt()

    fun update(contentHeight: Int, viewportHeight: Int) {
        maximum = (contentHeight - viewportHeight).coerceAtLeast(0).toDouble()
        offset = offset.coerceIn(0.0, maximum)
        if (maximum == 0.0) dragging = false
    }

    fun reset() { offset = 0.0; dragging = false }
    fun moveTo(value: Double) {
        if (value.isFinite()) offset = value.coerceIn(0.0, maximum)
    }
    fun moveBy(pixels: Double) = moveTo(offset + pixels)
    fun wheel(vertical: Double, horizontal: Double = 0.0): Boolean {
        if (maximum == 0.0) return false
        val amount = if (vertical != 0.0) vertical else horizontal
        if (!amount.isFinite() || amount == 0.0) return false
        moveBy(-amount * pixelsPerStep)
        return true // Consume at boundaries, too: don't leak to the game beneath the menu.
    }

    fun thumb(track: UiRect): UiRect {
        if (maximum == 0.0) return track
        val height = (track.height.toDouble() * track.height / (track.height + maximum)).roundToInt()
            .coerceIn(minOf(18, track.height), track.height)
        val y = track.y + ((track.height - height) * offset / maximum).roundToInt()
        return UiRect(track.x, y, track.width, height)
    }

    fun beginDrag(mx: Double, my: Double, track: UiRect): Boolean {
        if (maximum == 0.0 || !track.contains(mx, my)) return false
        val thumb = thumb(track)
        grabOffset = if (thumb.contains(mx, my)) my - thumb.y else thumb.height / 2.0
        dragging = true
        drag(my, track)
        return true
    }

    fun drag(my: Double, track: UiRect): Boolean {
        if (!dragging) return false
        val travel = track.height - thumb(track).height
        if (travel > 0) moveTo((my - track.y - grabOffset) / travel * maximum)
        return true
    }

    fun endDrag(): Boolean = dragging.also { dragging = false }
}
