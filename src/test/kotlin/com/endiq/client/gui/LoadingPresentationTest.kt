package com.endiq.client.gui

import com.endiq.client.gui.components.LoadingLayout
import com.endiq.client.gui.components.LoadingProgress
import kotlin.test.*

class LoadingPresentationTest {
    @Test fun progressIsBoundedMonotonicAndRejectsNonfiniteInputs() {
        val progress=LoadingProgress()
        assertEquals(0,progress.update(-1f,0).percent)
        val halfway=progress.update(.5f,100_000_000)
        assertEquals(50,halfway.percent);assertTrue(halfway.displayed in 0f..halfway.reported)
        val back=progress.update(.2f,200_000_000)
        assertEquals(.5f,back.reported);assertTrue(back.displayed>=halfway.displayed)
        assertEquals(.5f,progress.update(Float.NaN,300_000_000).reported)
        assertEquals(.5f,progress.update(Float.POSITIVE_INFINITY,400_000_000).reported)
        val done=progress.update(2f,500_000_000)
        assertEquals(100,done.percent);assertTrue(done.finalizing);assertTrue(done.displayed<=1f)
    }
    @Test fun noFakeProgressWhileMinecraftReportsNoNewWork() {
        val progress=LoadingProgress()
        progress.update(.4f,0)
        for(i in 1..100) {
            val frame=progress.update(.4f,i*100_000_000L)
            assertEquals(.4f,frame.reported);assertEquals(.4f,frame.displayed);assertFalse(frame.finalizing)
        }
    }
    @Test fun smoothingUsesElapsedTimeRatherThanFrameCount() {
        fun sample(frames:Int):Float {
            val state=LoadingProgress();state.update(0f,0)
            var frame=state.update(0f,0)
            for(i in 1..frames)frame=state.update(.8f,i*250_000_000L/frames)
            return frame.displayed
        }
        assertEquals(sample(5),sample(25),.00001f)
        assertTrue(sample(25)<.8f)
    }
    @Test fun eachOverlayStartsFreshIncludingRepeatedResourceReloads() {
        val first=LoadingProgress();assertTrue(first.update(1f,1).finalizing)
        val next=LoadingProgress().update(0f,100_000_000)
        assertEquals(0,next.percent);assertEquals(0f,next.displayed);assertFalse(next.finalizing)
    }
    @Test fun layoutStaysInsideSmallTallAndWideGuiScaledWindows() {
        for((w,h) in listOf(1 to 1,80 to 60,160 to 90,320 to 240,640 to 360,1920 to 1080,480 to 960)) {
            val layout=LoadingLayout.fit(w,h)
            for(rect in listOf(layout.logo,layout.wordmark,layout.bar,layout.status,layout.percentage)) {
                assertTrue(rect.width>0 && rect.height>0)
                assertTrue(rect.x>=0 && rect.y>=0 && rect.right<=w && rect.bottom<=h,"$w x $h: $rect")
            }
        }
    }
    @Test fun readableLayoutsDoNotOverlapAndChromeIsHiddenOnSmallWindows() {
        for((w,h) in listOf(160 to 90,320 to 240,640 to 360,1920 to 1080)) {
            val layout=LoadingLayout.fit(w,h)
            assertTrue(layout.logo.bottom<=layout.wordmark.y)
            assertTrue(layout.wordmark.bottom<layout.bar.y)
            assertTrue(layout.bar.bottom<layout.status.y)
            assertTrue(layout.status.right<layout.percentage.x)
        }
        assertFalse(LoadingLayout.fit(160,90).showChrome)
        assertTrue(LoadingLayout.fit(640,360).showChrome)
    }
}
