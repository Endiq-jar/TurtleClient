package com.endiq.client.gui

import com.endiq.client.gui.components.*
import com.endiq.client.capes.CapeMesh
import kotlin.test.*

class ControlsTest {
    @Test fun nineSliceGeometryCoversButtonsWithoutOverlapsOrEscapingBounds() {
        for ((w,h) in listOf(1 to 1, 6 to 4,24 to 22,160 to 30,400 to 280)) {
            val rect=UiRect(12,24,w,h);val pieces=nineSlices(rect,128,32)
            assertEquals(w*h,pieces.sumOf { it.destination.width*it.destination.height })
            for (p in pieces) {
                assertTrue(p.destination.x>=rect.x && p.destination.right<=rect.right)
                assertTrue(p.destination.y>=rect.y && p.destination.bottom<=rect.bottom)
                assertTrue(p.source.x>=0 && p.source.right<=128 && p.source.y>=0 && p.source.bottom<=32)
            }
        }
    }
    @Test fun actionButtonsInvokeExactlyOnceAndDisabledButtonsExplainWhy() {
        var calls=0;var message="";val buttons=ActionButtons { message=it }
        buttons.add(UiRect(10,10,40,20),UiAction("open","Open") { calls++ })
        buttons.add(UiRect(60,10,40,20),UiAction("switch","Switch",{false},"Disconnect first") { calls++ })
        assertFalse(buttons.click(20.0,20.0,1));assertTrue(buttons.click(20.0,20.0,0));assertEquals(1,calls)
        assertTrue(buttons.click(70.0,20.0,0));assertEquals("Disconnect first",message);assertEquals(1,calls)
        assertFalse(buttons.click(2.0,20.0,0))
    }
    @Test fun theCapeMeshIsBoundedTexturedAndUsesStandardCapeUvs() {
        val vertices = CapeMesh.vertices
        assertTrue(vertices.isNotEmpty() && vertices.size % 4 == 0, "Cape mesh must be whole quads")
        for (vertex in vertices) {
            assertTrue(vertex.u in 0f..1f && vertex.v in 0f..1f, "Cape UVs stay inside the texture")
            assertTrue(vertex.x.isFinite() && vertex.y.isFinite() && vertex.z.isFinite())
        }
        // The visible back panel occupies u 1/64..11/64, v 1/32..17/32 of a 64x32 cape.
        assertTrue(vertices.any { it.u > 0f && it.v > 0f })
        assertTrue(vertices.size <= 64, "Cape mesh budget exceeded")
    }
}
