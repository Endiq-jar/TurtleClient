package com.endiq.client.gui

import com.endiq.client.modules.impl.hud.DayCounterModule
import com.endiq.client.modules.impl.hud.HealthDisplayModule
import com.endiq.client.modules.impl.render.*
import com.endiq.client.modules.impl.utility.BestPingJoinerModule
import com.endiq.client.modules.impl.utility.ToolWarningModule
import com.endiq.client.modules.impl.utility.UiSwitchModule
import com.endiq.client.modules.impl.performance.HeatSaverModule
import kotlin.test.*

class FhasixFeaturesTest {
    @Test fun dayCounterStartsAtDayOne() {
        val day = DayCounterModule()
        assertEquals("Day: 1", day.getText(0))
        assertEquals("Day: 1", day.getText(23_999))
        assertEquals("Day: 2", day.getText(24_000))
        assertEquals("Day: 1", day.getText(-5))
        day.showLabel.value = false
        assertEquals("3", day.getText(48_000))
    }
    @Test fun healthDisplayShowsPreciseHpAndAbsorption() {
        val hp = HealthDisplayModule()
        assertEquals("HP: 17.5/20", hp.getText(17.5f, 20f))
        assertEquals("HP: 20.0/20 +4.0", hp.getText(20f, 20f, 4f))
        hp.showAbsorption.value = false
        assertEquals("HP: 20.0/20", hp.getText(20f, 20f, 4f))
    }
    @Test fun toolWarningFiresOncePerItemPerCooldown() {
        var now = 0L
        val warn = ToolWarningModule { now }
        assertTrue(warn.isLow(95, 100))
        assertFalse(warn.isLow(50, 100))
        assertFalse(warn.isLow(0, 0))
        assertTrue(warn.shouldWarn("pickaxe"))
        assertFalse(warn.shouldWarn("pickaxe"))
        assertTrue(warn.shouldWarn("shovel"))
        now += 61_000
        assertTrue(warn.shouldWarn("pickaxe"))
    }
    @Test fun fhasixMenuOnlyFeaturesAreListedButCanNeverEnable() {
        val menuOnly = listOf(WingsModule(), BannersModule(), EmotesModule(), HatsModule(),
            GlassesModule(), MasksModule(), ShoesModule(), ParticlesModule(), CharactersModule(),
            BestPingJoinerModule(), UiSwitchModule(), HeatSaverModule())
        assertEquals(12, menuOnly.size)
        for (module in menuOnly) {
            assertEquals("Not implemented: this FHASIX feature only shows in the mod menu.",
                module.unavailableReason, module.name)
            module.enable()
            module.updateKeyState(true, true)
            assertFalse(module.enabled, "${module.name} must stay off")
            assertTrue(module.visibleSettings.isEmpty(), "${module.name} must show no fake controls")
        }
    }
}
