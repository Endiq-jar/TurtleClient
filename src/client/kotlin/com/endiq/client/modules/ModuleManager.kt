package com.endiq.client.modules

import com.endiq.client.modules.impl.hud.*
import com.endiq.client.modules.impl.pvp.*
import com.endiq.client.modules.impl.movement.*
import com.endiq.client.modules.impl.render.*
import com.endiq.client.modules.impl.utility.*
import com.endiq.client.modules.impl.hypixel.*
import com.endiq.client.modules.impl.performance.*

object ModuleManager {
    val modules = mutableListOf<Module>()

    fun init() {
        // HUD
        register(FpsModule()); register(CpsModule()); register(CoordinatesModule())
        register(KeystrokesModule()); register(ArmorStatusModule()); register(ArmorBarModule())
        register(AttackIndicatorModule()); register(AutoHideHudModule())
        register(BlockIndicatorModule()); register(BlockOverlayModule()); register(BossBarModule())
        register(CrosshairModule()); register(ScoreboardModule()); register(PackDisplayModule())
        register(PingModule()); register(PotionStatusModule()); register(ServerAddressModule())
        register(ReachDisplayModule()); register(SpeedHudModule()); register(MemoryHudModule())
        register(ClockHudModule()); register(DirectionHudModule()); register(ToggleSprintModule())
        register(NetGraphModule())
        // PvP
        register(HitColorModule()); register(PvpInfoModule()); register(TeamCirclesModule())
        register(ComboCounterModule())
        // Movement
        register(SprintModule()); register(FreecamModule())
        // Render
        register(AnimationsModule()); register(MotionBlurModule())
        register(NoWeatherModule()); register(TimeChangerModule()); register(ZoomModule())
        register(FovChangerModule()); register(FullBrightModule()); register(CullingModule())
        // Utility
        register(AutoTextModule()); register(CameraModule()); register(ChatModule())
        register(NickHiderModule()); register(PopupEventsModule())
        register(TimersModule()); register(WaypointsModule()); register(TeamViewModule())
        // Hypixel
        register(HypixelAddonsModule()); register(SkyblockAddonsModule())
        register(TabStatModule()); register(UhcOverlayModule())
        // Performance
        register(DynamicRenderDistanceModule()); register(AdaptiveVsyncModule())

        modules.filter { it.name in listOf("Auto Hide HUD", "Camera", "UHC Overlay",
            "Net Graph", "Combo Counter", "FOV Changer", "Full Bright", "Team View",
            "Dynamic Render Distance", "Adaptive VSync") }
            .forEach { it.isNew = true }
    }

    private fun register(m: Module) = modules.add(m)
    fun getByName(name: String) = modules.firstOrNull { it.name.equals(name, true) }
    fun getEnabled() = modules.filter { it.enabled }
    fun getByCategory(cat: Module.Category) =
        if (cat == Module.Category.ALL) modules else modules.filter { it.category == cat }
}
