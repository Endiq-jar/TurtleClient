package com.endiq.client.gui

import com.endiq.client.gui.components.*
import com.endiq.client.cosmetics.CosmeticMeshes
import com.endiq.client.cosmetics.CosmeticManager
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
    @Test fun allSixCosmeticTypesHaveBoundedNonemptyTexturedGeometry() {
        for (type in CosmeticManager.CosmeticType.values()) {
            val parts=CosmeticMeshes.models[type]?:fail("Missing mesh: $type")
            assertTrue(parts.isNotEmpty())
            for(p in parts) {
                assertTrue(p.vertices.isNotEmpty() && p.vertices.size%4==0)
                for(v in p.vertices) {
                    assertTrue(v.u in 0f..1f && v.v in 0f..1f)
                    assertTrue(v.x.isFinite() && v.y.isFinite() && v.z.isFinite())
                }
            }
            assertTrue(parts.sumOf { it.vertices.size }<=512,"Mesh budget exceeded for $type")
        }
    }
}
