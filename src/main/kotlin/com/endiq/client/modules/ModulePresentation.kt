package com.endiq.client.modules

/** Only advertise controls consumed by the implementation, not legacy placeholder fields. */
object ModulePresentation {
    private val prototypes=setOf("AnimationsModule","FreecamModule","NoWeatherModule","TimeChangerModule",
        "ChatModule","NickHiderModule","BossBarModule","ScoreboardModule","TeamCirclesModule",
        "SkyblockAddonsModule","TabStatModule","MotionBlurModule")
    /** FHASIX shows these in its menu only ("are not available"); mirrored honestly. */
    private val fhasixMenuOnly=setOf("WingsModule","BannersModule","EmotesModule","HatsModule","GlassesModule","MasksModule","ShoesModule","ParticlesModule","CharactersModule","BestPingJoinerModule","UiSwitchModule","HeatSaverModule")
    fun unavailable(module:Module):String? = if(module.javaClass.simpleName in fhasixMenuOnly)
        "Not implemented: this FHASIX feature only shows in the mod menu."
        else if(module.javaClass.simpleName in prototypes)
        if(module.javaClass.simpleName=="NickHiderModule")"Unavailable: this prototype does NOT hide your name."
        else "Unavailable: the legacy ${module.name} implementation is incomplete." else null

    private val common=setOf("Text Color","Color","Background Color","Background","Show Background","Scale",
        "Position X","Position Y","Text Shadow","Bold Text","BG Padding","BG Radius","Color Mode","Rainbow Speed","Alignment")
    private val extras=mapOf(
        "CoordinatesModule" to setOf("Show X","Show Y","Show Z","Show Facing","Decimal Precision","Layout","Label Style"),
        "ReachDisplayModule" to setOf("Decimal Places"),
        "BlockIndicatorModule" to emptySet(),
        "PackDisplayModule" to emptySet(),
        "ServerAddressModule" to setOf("Show Label","Show Port","Show In Singleplayer","Style","Custom Text"),
        "SpeedHudModule" to setOf("Unit","Decimal Places","Include Vertical Speed"),
        "MemoryHudModule" to setOf("Unit","Show Max","Show Memory Bar","Bar Width","Bar Color","Warning Color","Warn At"),
        "ClockHudModule" to setOf("24-Hour Format","Show Seconds","Show Date","Date Format","Show Label"),
        "DirectionHudModule" to setOf("Show Degrees","Show Arrow","Style"),
        "ToggleSprintModule" to setOf("Style"),
        "BlockOverlayModule" to setOf("X Offset","Y Offset","Style")
    )
    private val exclusive=mapOf(
        "ArmorStatusModule" to setOf("Scale","Position X","Position Y","Background","Show Background","Text Shadow","BG Padding","BG Radius",
            "Layout","Show Empty Slots","Show Durability Number","Show Durability Bar","Show Percentage","Good Threshold","Bad Threshold","Good Color","Mid Color","Bad Color","Icon Size"),
        "ArmorBarModule" to setOf("Scale","Position X","Position Y","Bar Width","Bar Height","Bar Color","Background Color"),
        "KeystrokesModule" to setOf("Scale","Position X","Position Y","Show WASD","Show Space","Key Size","Key Gap","Corner Radius","Text Shadow",
            "Key Color","Pressed Color","Text Color","Pressed Text Color"),
        "PotionStatusModule" to setOf("Scale","Position X","Position Y","Text Color","Background","Show Background","Show Effect Name","Show Timer","Show Amplifier","Max Effects","Text Shadow"),
        "UhcOverlayModule" to setOf("Scale","Position X","Position Y","Show Background","Background","Show HP","Show Saturation","Decimal Places","Text Shadow",
            "HP Color","Saturation Color"),
        "AutoHideHudModule" to setOf("Hide Delay"),
        "HypixelAddonsModule" to setOf("Auto /gg"),
        "SprintModule" to setOf("Cancel On Sneak","Min Speed"),
        "HitColorModule" to setOf("Hit Color","Duration","Intensity"),
        "AttackIndicatorModule" to setOf("Ready Color","Charging Color","Background","Bar Width","Bar Height",
            "Position X","Position Y","Show Percentage","Text Shadow","Show Outline","Only While Charging","Scale","BG Alpha")
    )
    fun settings(module:Module):List<Setting> {
        val name=module.javaClass.simpleName
        if(unavailable(module)!=null)return emptyList()
        exclusive[name]?.let { allowed->return module.settings.filter { it.name in allowed } }
        val allowed=extras[name] ?: return module.settings
        return module.settings.filter { it.name in common || it.name in allowed }
    }
    fun note(module:Module):String=unavailable(module) ?: when(module.javaClass.simpleName) {
        "ArmorStatusModule","ArmorBarModule","KeystrokesModule","PotionStatusModule","UhcOverlayModule" ->
            "Drag this overlay in the HUD editor or adjust its position and scale here."
        "SprintModule"->"Forward auto-sprint; alternate movement modes are not implemented."
        "ToggleSprintModule"->"Displays sprint state; use Minecraft's sprint key or the Sprint module to change it."
        "AttackIndicatorModule"->"Configurable cooldown bar; circle/icon modes are not implemented."
        "HypixelAddonsModule"->"Auto /gg on Hypixel only; other old addon prototypes are not implemented."
        else->module.description
    }
}
