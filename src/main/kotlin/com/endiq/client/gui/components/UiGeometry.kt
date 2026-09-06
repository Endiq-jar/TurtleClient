package com.endiq.client.gui.components

/** GUI-scaled pixels, shared by drawing, clipping and hit testing. */
data class UiRect(val x: Int, val y: Int, val width: Int, val height: Int) {
    val right get() = x + width
    val bottom get() = y + height
    fun contains(mx: Double, my: Double) = mx >= x && mx < right && my >= y && my < bottom
    fun inset(amount: Int) = UiRect(x + amount, y + amount, (width - amount * 2).coerceAtLeast(0), (height - amount * 2).coerceAtLeast(0))
}

fun centeredPanel(screenWidth: Int, screenHeight: Int, maxWidth: Int, maxHeight: Int): UiRect {
    val margin = minOf(12, screenWidth / 20, screenHeight / 20).coerceAtLeast(0)
    val w = minOf(maxWidth, (screenWidth - margin * 2).coerceAtLeast(1))
    val h = minOf(maxHeight, (screenHeight - margin * 2).coerceAtLeast(1))
    return UiRect((screenWidth - w) / 2, (screenHeight - h) / 2, w, h)
}

/** Reflow rather than scaling text or leaving a fixed-width menu off-screen. */
data class UiGrid(val viewport: UiRect, val minimumCardWidth: Int = 132, val cardHeight: Int = 78, val gap: Int = 8, val maxColumns: Int = 4) {
    val columns = ((viewport.width - gap) / (minimumCardWidth + gap)).coerceIn(1, maxColumns)
    val cardWidth = ((viewport.width - gap * (columns + 1)) / columns).coerceAtLeast(1)
    fun contentHeight(count: Int) = if (count == 0) 0 else gap + ((count + columns - 1) / columns) * (cardHeight + gap)
    fun hit(mx: Double, my: Double, count: Int, offset: Int): Int? {
        if (!viewport.contains(mx, my)) return null
        val x = mx - viewport.x - gap
        val y = my - viewport.y - gap + offset
        if (x < 0 || y < 0) return null
        val column = (x / (cardWidth + gap)).toInt()
        val row = (y / (cardHeight + gap)).toInt()
        val index = row * columns + column
        return if (column < columns && index in 0 until count && card(index, offset).contains(mx, my)) index else null
    }
    fun card(index: Int, offset: Int) = UiRect(
        viewport.x + gap + index % columns * (cardWidth + gap),
        viewport.y + gap + index / columns * (cardHeight + gap) - offset,
        cardWidth, cardHeight
    )
}
