package com.endiq.client.gui.components

import kotlin.math.exp
import kotlin.math.roundToInt

/** Per-overlay progress. Never invent milestones or advance beyond Minecraft's reported work. */
class LoadingProgress {
    data class Frame(val reported: Float, val displayed: Float) {
        val percent get() = (reported * 100).toInt().coerceIn(0, 100)
        val finalizing get() = reported >= 1f
    }
    private var reported = 0f
    private var displayed = 0f
    private var lastNanos: Long? = null

    fun update(actual: Float, nowNanos: Long): Frame {
        if (actual.isFinite()) reported = maxOf(reported, actual.coerceIn(0f, 1f))
        val previous = lastNanos
        if (previous == null) displayed = reported
        else {
            val elapsed = ((nowNanos - previous) / 1_000_000_000.0).coerceIn(0.0, .25)
            displayed += (reported - displayed) * (1.0 - exp(-elapsed / .12)).toFloat()
            if (reported - displayed < .0005f) displayed = reported
        }
        lastNanos = nowNanos
        return Frame(reported, displayed.coerceIn(0f, reported))
    }
}

/** Shared with layout tests; coordinates are GUI-scaled pixels, not framebuffer pixels. */
data class LoadingLayout(
    val logo: UiRect,
    val wordmark: UiRect,
    val bar: UiRect,
    val status: UiRect,
    val percentage: UiRect,
    val scale: Float,
    val margin: Int,
    val showChrome: Boolean
) {
    companion object {
        fun fit(width: Int, height: Int): LoadingLayout {
            val w = width.coerceAtLeast(1); val h = height.coerceAtLeast(1)
            val margin = minOf(24, w / 12, h / 12)
            val groupHeight = minOf(184, h - margin * 2).coerceAtLeast(1)
            val scale = minOf(1f, groupHeight / 184f, (w - margin * 2) / 224f).coerceAtLeast(.01f)
            val top = (h - (184 * scale).roundToInt()) / 2
            fun px(value: Int) = (value * scale).roundToInt().coerceAtLeast(1)
            fun box(x: Int, y: Int, rw: Int, rh: Int): UiRect {
                val bw = rw.coerceIn(1, w); val bh = rh.coerceIn(1, h)
                return UiRect(x.coerceIn(0, w - bw), y.coerceIn(0, h - bh), bw, bh)
            }
            fun centered(y: Int, rw: Int, rh: Int) = box((w - px(rw)) / 2, top + px(y), px(rw), px(rh))
            val bar = centered(136, 224, 3)
            val status = box(bar.x, bar.bottom + px(11), px(126), px(12))
            val percentage = box(bar.right - px(32), status.y, px(32), px(12))
            return LoadingLayout(centered(5, 52, 52), centered(73, 166, 31), bar, status, percentage,
                scale, margin, w >= 280 && h >= 220)
        }
    }
}
