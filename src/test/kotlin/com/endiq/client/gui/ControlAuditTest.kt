package com.endiq.client.gui

import com.endiq.client.accounts.AccountProblem
import com.endiq.client.accounts.AccountProfile
import com.endiq.client.compat.SessionBridge
import com.endiq.client.config.ModulePreferenceCodec
import com.endiq.client.modules.impl.hud.*
import com.endiq.client.modules.impl.hypixel.HypixelAddonsModule
import com.endiq.client.modules.impl.movement.SprintModule
import com.endiq.client.modules.impl.utility.*
import com.google.gson.JsonParser
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.*

class ControlAuditTest {
    @Test fun actualSettingNamesStayReachableAndPlaceholdersAreNotControls() {
        val clock=ClockHudModule();assertTrue(clock.format24 in clock.visibleSettings)
        val speed=SpeedHudModule();assertTrue(speed.unit in speed.visibleSettings);assertTrue(speed.includeVert in speed.visibleSettings)
        val memory=MemoryHudModule();assertTrue(memory.unit in memory.visibleSettings);assertTrue(memory.showMax in memory.visibleSettings)
        val server=ServerAddressModule();assertTrue(server.customText in server.visibleSettings)
        val hypixel=HypixelAddonsModule();assertEquals(listOf(hypixel.autoGg),hypixel.visibleSettings)
        val attack=AttackIndicatorModule();assertTrue(attack.readyColor in attack.visibleSettings);assertFalse(attack.style in attack.visibleSettings)
        val sprint=SprintModule();assertTrue(sprint.cancelSneak in sprint.visibleSettings);assertFalse(sprint.mode in sprint.visibleSettings)
    }
    @Test fun unavailablePrivacyPrototypeCannotEnableThroughClickKeyOrConfiguration() {
        val nick=NickHiderModule();assertNotNull(nick.unavailableReason)
        nick.enable();nick.updateKeyState(true,true);assertFalse(nick.enabled)
        ModulePreferenceCodec.restore(listOf(nick),JsonParser.parseString("""{"modules":{"Nick Hider":{"enabled":true}}}""").asJsonObject)
        assertFalse(nick.enabled);assertTrue(nick.visibleSettings.isEmpty())
    }
    @Test fun speedUsesBlocksPerSecondAndUnitAndVerticalControls() {
        val speed=SpeedHudModule()
        assertEquals("Speed: 2.00 m/s",speed.formatVelocity(.1,1.0,0.0))
        speed.unit.selected=1;assertEquals("Speed: 7.20 km/h",speed.formatVelocity(.1,0.0,0.0))
        speed.unit.selected=0;speed.includeVert.value=true
        assertEquals("Speed: 2.00 m/s",speed.formatVelocity(0.0,.1,0.0))
    }
    @Test fun memoryUnitsAndMaximumControlsAffectOutput() {
        val memory=MemoryHudModule();val used=1073741824L;val max=used*2
        assertEquals("RAM: 1024/2048 MB",memory.getText(used,max))
        memory.showMax.value=false;memory.unit.selected=1
        assertEquals("RAM: 1.00 GB",memory.getText(used,max))
        memory.unit.selected=2;assertEquals("RAM: 50.0 %",memory.getText(used,max))
    }
    @Test fun clockAndDirectionOptionsHaveObservableEffects() {
        val clock=ClockHudModule();val now=LocalDateTime.of(2026,9,6,21,7,3)
        clock.format24.value=true;clock.showSeconds.value=true;clock.showDate.value=true;clock.dateFormat.selected=2;clock.showLabel.value=true
        assertEquals("Time: 21:07:03 2026/09/06",clock.getText(now))
        val direction=DirectionHudModule();direction.showArrow.value=false;direction.showDegrees.value=true
        assertEquals("N 180°",direction.getText(-540f))
        direction.style.selected=2;assertEquals("180°",direction.getText(180f))
    }
    @Test fun addressControlsPreserveIpv6AndSupportCustomText() {
        val server=ServerAddressModule()
        assertEquals("Server: [::1]",server.getText("[::1]:25565"))
        server.showPort.value=true;assertEquals("Server: [::1]:25565",server.getText("[::1]:25565"))
        server.showOnSP.value=false;assertEquals("",server.getText(null))
        server.style.selected=2;server.customText.value="Turtle";server.showLabel.value=false
        assertEquals("Turtle",server.getText("example.net"))
    }
    @Test fun repeatStartsAndStopsLiveAndDoesNotBurstAfterADelay() {
        var time=0L;val schedule=AutoTextSchedule { time }
        schedule.joined(true,1000)
        assertFalse(schedule.due(true,false,5000));time=1000;assertTrue(schedule.due(true,false,5000))
        assertFalse(schedule.due(true,false,5000))
        assertFalse(schedule.due(true,true,5000));time=6000;assertTrue(schedule.due(true,true,5000))
        assertFalse(schedule.due(true,false,5000));time=12000;assertFalse(schedule.due(true,false,5000))
        assertFalse(schedule.due(true,true,5000));time=100000;assertTrue(schedule.due(true,true,5000))
        assertFalse(schedule.due(true,true,5000))
        schedule.joined(true,1000);time+=1000;assertFalse(schedule.due(false,false,5000))
    }
    @Test fun notificationsAreBoundedAndFadeByTimeNotFrameCount() {
        var time=0L;val module=PopupEventsModule { time };module.maxPopups.value=2f
        module.add("first");module.add("second");module.add("§ared")
        val visible=module.visible();assertEquals(listOf("second","red"),visible.map { it.msg })
        assertEquals(0f,module.opacity(visible[0]))
        time=250;assertEquals(.5f,module.opacity(visible[0]))
        time=1000;repeat(100) { assertEquals(1f,module.opacity(visible[0])) }
        time=4000;assertTrue(module.visible().isEmpty())
        module.add("x".repeat(1000));assertEquals(240,module.visible().single().msg.length)
        module.disable();assertTrue(module.visible().isEmpty())
    }
    @Test fun signInExpiryIsRecheckedBeforeAnySessionMutation() {
        val profile=AccountProfile(UUID.randomUUID(),"Turtle",AccountProfile.Kind.MICROSOFT)
        val prepared=SessionBridge.Prepared(emptyList(),profile,61_000)
        assertTrue(prepared.usable(30_000));assertFalse(prepared.usable(31_000))
        // Expires before touching Minecraft, so no game instance is needed for this guard.
        assertFailsWith<AccountProblem> { SessionBridge.commit(prepared) }
        prepared.close()
    }
}
