package com.endiq.client.gui

import com.endiq.client.config.ModulePreferenceCodec
import com.endiq.client.cosmetics.CosmeticManager
import com.endiq.client.modules.Module
import com.endiq.client.modules.impl.utility.TimerClock
import com.endiq.client.modules.impl.hud.CpsModule
import com.google.gson.JsonParser
import java.nio.ByteBuffer
import kotlin.test.*

class WorkingControlsTest {
    private class Example:Module("Example","Test",Category.UTILITY) {
        val slider=slider("Amount",default=2f,min=1f,max=10f)
        val flag=bool("Flag",default=false)
        val tint=color("Tint",r=1,g=2,b=3,a=4)
        val mode=dropdown("Mode",options=arrayOf("One","Two"))
        val message=text("Text",default="hello",limit=8)
    }
    @Test fun preferencesRestoreTogglesFavoritesBindingsAndAllSettingTypes() {
        val source=Example();source.enable();source.favorited=true;source.key=67
        source.slider.value=8f;source.flag.value=true;source.tint.a=128;source.mode.selected=1;source.message.value="Turtle"
        val restored=Example();ModulePreferenceCodec.restore(listOf(restored),ModulePreferenceCodec.encode(listOf(source)))
        assertTrue(restored.enabled);assertTrue(restored.favorited);assertEquals(67,restored.key)
        assertEquals(8f,restored.slider.value);assertTrue(restored.flag.value);assertEquals(128,restored.tint.a)
        assertEquals(1,restored.mode.selected);assertEquals("Turtle",restored.message.value)
    }
    @Test fun invalidPersistedValuesCannotEscapeControlBounds() {
        val mod=Example()
        ModulePreferenceCodec.restore(listOf(mod),JsonParser.parseString("""{"modules":{"Example":{"key":9999,"settings":{"Amount":999,"Mode":999,"Tint":[-4,400,50,0],"Text":"123456789abc"}}}}""").asJsonObject)
        assertEquals(-1,mod.key);assertEquals(10f,mod.slider.value);assertEquals(1,mod.mode.selected)
        assertEquals(0,mod.tint.r);assertEquals(255,mod.tint.g);assertEquals("12345678",mod.message.value)
    }
    @Test fun timerCanStartPauseResumeResetAndFinishWithoutClockDrift() {
        var now=0L;val clock=TimerClock { now }
        clock.start(2);now=750;assertEquals(1250L,clock.remaining());clock.pause();now=4000;assertEquals(1250L,clock.remaining())
        clock.start(2);now=4500;assertEquals(750L,clock.remaining());now=10000;assertEquals(0L,clock.remaining())
        clock.reset();assertFalse(clock.running);assertFalse(clock.started);assertEquals(0L,clock.elapsed())
    }
    @Test fun cpsActuallyCountsAndDecaysWhenClicksStop() {
        var now=0L;val cps=CpsModule { now }
        cps.registerClick();cps.registerClick(true);assertEquals(1,cps.getLCps());assertEquals(1,cps.getRCps())
        now=1001;assertEquals(0,cps.getLCps());assertEquals(0,cps.getRCps())
        cps.trackLeft.value=false;cps.registerClick();assertEquals(0,cps.getLCps())
    }
    @Test fun customPngHeadersRejectDecompressionBombDimensionsAndWrongCapeLayouts() {
        fun png(w:Int,h:Int)=ByteBuffer.allocate(33).apply {
            put(byteArrayOf(-119,80,78,71,13,10,26,10));putInt(13);put("IHDR".toByteArray());putInt(w);putInt(h)
        }.array()
        CosmeticManager.validatePng(png(64,32),true)
        assertFailsWith<IllegalArgumentException> { CosmeticManager.validatePng(png(512,512),true) }
        assertFailsWith<IllegalArgumentException> { CosmeticManager.validatePng(png(Int.MAX_VALUE,16),false) }
        assertFailsWith<IllegalArgumentException> { CosmeticManager.validatePng(ByteArray(33),false) }
    }
}
