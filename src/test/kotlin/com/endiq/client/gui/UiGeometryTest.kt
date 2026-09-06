package com.endiq.client.gui

import com.endiq.client.gui.components.*
import kotlin.test.*

class UiGeometryTest {
    @Test fun menusFitAtCommonGuiScales() {
        for ((width, height) in listOf(320 to 240, 426 to 240, 640 to 360, 960 to 540, 1920 to 1080)) {
            for ((maxWidth, maxHeight) in listOf(680 to 430, 380 to 386)) {
                val panel = centeredPanel(width, height, maxWidth, maxHeight)
                assertTrue(panel.x >= 0 && panel.y >= 0)
                assertTrue(panel.right <= width && panel.bottom <= height)
            }
        }
    }

    @Test fun cardsReflowAndLastRowIsReachable() {
        for (width in listOf(280, 380, 660)) {
            val viewport = UiRect(20, 60, width, 160)
            val grid = UiGrid(viewport)
            val count = 57
            val scroll = ScrollState()
            scroll.update(grid.contentHeight(count), viewport.height)
            scroll.moveTo(scroll.maximum)
            val last = grid.card(count - 1, scroll.pixels)
            assertTrue(last.bottom <= viewport.bottom)
            assertTrue(last.bottom > viewport.y)
            for (index in 0 until count) assertTrue(grid.card(index, 0).right <= viewport.right)
            assertEquals(count - 1, grid.hit(last.x + 3.0, last.bottom - 3.0, count, scroll.pixels))
        }
    }

    @Test fun hitTestingUsesTheSameScrollOffsetAndClipAsDrawing() {
        val grid = UiGrid(UiRect(20, 60, 400, 160))
        val offset = 42
        val card = grid.card(0, offset)
        assertTrue(card.y < grid.viewport.y)
        assertNull(grid.hit(card.x + 3.0, card.y + 3.0, 20, offset))
        assertEquals(0, grid.hit(card.x + 3.0, grid.viewport.y + 3.0, 20, offset))
        assertNull(grid.hit(card.right + 2.0, grid.viewport.y + 3.0, 20, offset))
        assertNull(grid.hit(22.0, grid.viewport.bottom + 1.0, 20, offset))
    }

    @Test fun aCompleteCardFitsWhenSmallWindowsNeedTwoRowsOfTabs() {
        val grid = UiGrid(UiRect(12, 128, 280, 76))
        val first = grid.card(0, 0)
        assertTrue(first.y >= grid.viewport.y)
        assertTrue(first.bottom <= grid.viewport.bottom)
        assertEquals(0, grid.hit(first.x + 4.0, first.bottom - 4.0, 1, 0))
    }

    @Test fun backgroundCoversWithoutStretching() {
        for ((width, height) in listOf(320 to 240, 1920 to 1080, 3440 to 1440, 480 to 800)) {
            val rect = coverBounds(width, height, 1024, 576)
            assertTrue(rect.x <= 0 && rect.y <= 0 && rect.right >= width && rect.bottom >= height)
            assertEquals(1024.0 / 576, rect.width.toDouble() / rect.height, 0.01)
        }
    }
}
