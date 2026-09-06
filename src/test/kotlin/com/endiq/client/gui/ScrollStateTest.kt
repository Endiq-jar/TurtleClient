package com.endiq.client.gui

import com.endiq.client.gui.components.*
import kotlin.test.*

class ScrollStateTest {
    @Test fun fractionalWheelInputAccumulates() {
        val scroll = ScrollState()
        scroll.update(1000, 200)
        repeat(10) { assertTrue(scroll.wheel(-0.01)) }
        assertEquals(2.8, scroll.offset, 0.000001)
        assertEquals(3, scroll.pixels)
    }

    @Test fun normalWheelMovesInBothDirectionsAndClamps() {
        val scroll = ScrollState()
        scroll.update(300, 200)
        scroll.wheel(-1.0); assertEquals(28.0, scroll.offset)
        scroll.wheel(1.0); assertEquals(0.0, scroll.offset)
        scroll.wheel(-500.0); assertEquals(100.0, scroll.offset)
        assertTrue(scroll.wheel(-1.0))
        scroll.wheel(500.0); assertEquals(0.0, scroll.offset)
    }

    @Test fun shiftedHorizontalWheelAlsoScrolls() {
        val scroll = ScrollState()
        scroll.update(300, 100)
        assertTrue(scroll.wheel(0.0, -1.0))
        assertEquals(28.0, scroll.offset)
    }

    @Test fun invalidOrUnnecessaryScrollIsIgnored() {
        val scroll = ScrollState()
        scroll.update(20, 100)
        assertFalse(scroll.wheel(-1.0))
        scroll.update(500, 100)
        assertFalse(scroll.wheel(Double.NaN))
        assertFalse(scroll.wheel(Double.POSITIVE_INFINITY))
        assertFalse(scroll.wheel(0.0))
        assertEquals(0.0, scroll.offset)
    }

    @Test fun filteringAndResizingClampWithoutLosingValidPosition() {
        val scroll = ScrollState()
        scroll.update(1000, 200); scroll.moveTo(400.0)
        scroll.update(1000, 300); assertEquals(400.0, scroll.offset)
        scroll.update(350, 300); assertEquals(50.0, scroll.offset)
        scroll.update(0, 300); assertEquals(0.0, scroll.offset)
        scroll.update(1000, 200); scroll.wheel(-1.0); assertEquals(28.0, scroll.offset)
    }

    @Test fun thumbDraggingReachesBothEndsAndReleaseStopsIt() {
        val scroll = ScrollState()
        val track = UiRect(100, 10, 6, 200)
        scroll.update(1000, 200); scroll.moveTo(400.0)
        assertEquals(40, scroll.thumb(track).height)
        val start = scroll.thumb(track).y + 10.0
        assertTrue(scroll.beginDrag(103.0, start, track))
        assertEquals(400.0, scroll.offset)
        assertTrue(scroll.drag(500.0, track)); assertEquals(800.0, scroll.offset)
        scroll.drag(-100.0, track); assertEquals(0.0, scroll.offset)
        assertTrue(scroll.endDrag()); assertFalse(scroll.drag(100.0, track))
        assertFalse(scroll.beginDrag(50.0, 100.0, track))
    }

    @Test fun shrinkingListWhileDraggingResetsTheThumb() {
        val scroll = ScrollState()
        val track = UiRect(100, 10, 6, 200)
        scroll.update(1000, 200)
        scroll.beginDrag(103.0, 100.0, track)
        scroll.update(50, 200)
        assertFalse(scroll.dragging)
        assertEquals(0.0, scroll.offset)
    }

    @Test fun evenVeryShortViewportsHaveValidThumbs() {
        val scroll = ScrollState()
        scroll.update(500, 1)
        assertEquals(UiRect(0, 0, 6, 1), scroll.thumb(UiRect(0, 0, 6, 1)))
    }
}
