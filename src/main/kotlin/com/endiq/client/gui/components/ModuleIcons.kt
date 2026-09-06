package com.endiq.client.gui.components

/** One stable pictogram per built-in module, independent of its translated/display name. */
object ModuleIcons {
    val keys: Map<String, String> = mapOf(
        "FpsModule" to "fps",
        "CpsModule" to "cps",
        "CoordinatesModule" to "coordinates",
        "KeystrokesModule" to "keystrokes",
        "ArmorStatusModule" to "armor-status",
        "ArmorBarModule" to "armor-bar",
        "AttackIndicatorModule" to "attack-indicator",
        "AutoHideHudModule" to "auto-hide-hud",
        "BlockIndicatorModule" to "block-indicator",
        "BlockOverlayModule" to "block-overlay",
        "BossBarModule" to "boss-bar",
        "CrosshairModule" to "crosshair",
        "ScoreboardModule" to "scoreboard",
        "PackDisplayModule" to "pack-display",
        "PingModule" to "ping",
        "PotionStatusModule" to "potion-status",
        "ServerAddressModule" to "server-address",
        "ReachDisplayModule" to "reach-display",
        "SpeedHudModule" to "speed-hud",
        "MemoryHudModule" to "memory-hud",
        "ClockHudModule" to "clock-hud",
        "DirectionHudModule" to "direction-hud",
        "ToggleSprintModule" to "toggle-sprint",
        "NetGraphModule" to "net-graph",
        "HitColorModule" to "hit-color",
        "PvpInfoModule" to "pvp-info",
        "TeamCirclesModule" to "team-circles",
        "ComboCounterModule" to "combo-counter",
        "SprintModule" to "sprint",
        "FreecamModule" to "freecam",
        "AnimationsModule" to "animations",
        "MotionBlurModule" to "motion-blur",
        "NoWeatherModule" to "no-weather",
        "TimeChangerModule" to "time-changer",
        "ZoomModule" to "zoom",
        "FovChangerModule" to "fov-changer",
        "FullBrightModule" to "full-bright",
        "CullingModule" to "culling",
        "AutoTextModule" to "auto-text",
        "CameraModule" to "camera",
        "ChatModule" to "chat",
        "NickHiderModule" to "nick-hider",
        "PopupEventsModule" to "popup-events",
        "TimersModule" to "timers",
        "WaypointsModule" to "waypoints",
        "TeamViewModule" to "team-view",
        "HypixelAddonsModule" to "hypixel-addons",
        "SkyblockAddonsModule" to "skyblock-addons",
        "TabStatModule" to "tab-stat",
        "UhcOverlayModule" to "uhc-overlay",
        "DynamicRenderDistanceModule" to "dynamic-render-distance",
        "AdaptiveVsyncModule" to "adaptive-vsync"
    )
    private val categories = setOf("hud", "pvp", "render", "movement", "utility", "hypixel", "performance")
    fun path(className: String, category: String = "hud"): String {
        val key = keys[className]
        val fallback = if (category in categories) category else "grid"
        return if (key == null) "textures/gui/icons/$fallback.png" else "textures/gui/modules/$key.png"
    }
}
