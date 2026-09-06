package com.endiq.client.modules

/** Only advertise controls consumed by the implementation, not legacy placeholder fields. */
object ModulePresentation {
    private val prototypes=setOf("AnimationsModule","FreecamModule","NoWeatherModule","TimeChangerModule",
        "ChatModule","NickHiderModule","BossBarModule","ScoreboardModule","TeamCirclesModule",
        "SkyblockAddonsModule","TabStatModule","MotionBlurModule")
    fun unavailable(module:Module):String? = if(module.javaClass.simpleName in prototypes)
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
        "AutoHideHudModule" to setOf("Hide Delay"),
        "HypixelAddonsModule" to setOf("Auto /gg"),
        "SprintModule" to setOf("Cancel On Sneak","Min Speed"),
        "HitColorModule" to setOf("Hit Color","Duration","Intensity"),
        "AttackIndicatorModule" to setOf("Ready Color","Charging Color","Background","Bar Width","Bar Height",
            "Position X","Position Y","Show Percentage","Text Shadow","Show Outline","Only While Charging","Scale","BG Alpha")
    )
    private val fixed=setOf("ArmorStatusModule","ArmorBarModule","KeystrokesModule","PotionStatusModule","UhcOverlayModule")
    fun settings(module:Module):List<Setting> {
        val name=module.javaClass.simpleName
        if(unavailable(module)!=null || name in fixed)return emptyList()
        exclusive[name]?.let { allowed->return module.settings.filter { it.name in allowed } }
        val allowed=extras[name] ?: return module.settings
        return module.settings.filter { it.name in common || it.name in allowed }
    }
    fun note(module:Module):String=unavailable(module) ?: when(module.javaClass.simpleName) {
        in fixed->"Basic overlay with a fixed layout."
        "SprintModule"->"Forward auto-sprint; alternate movement modes are not implemented."
        "ToggleSprintModule"->"Displays sprint state; use Minecraft's sprint key or the Sprint module to change it."
        "AttackIndicatorModule"->"Configurable cooldown bar; circle/icon modes are not implemented."
        "HypixelAddonsModule"->"Auto /gg on Hypixel only; other old addon prototypes are not implemented."
        else->module.description
    }
}
