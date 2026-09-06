package com.endiq.client.modules

/** Do not advertise unimplemented legacy prototypes as working/privacy-protecting features. */
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
        "ServerAddressModule" to setOf("Hide Port"),
        "SpeedHudModule" to setOf("Units","Decimal Places"),
        "MemoryHudModule" to setOf("Show Max","Show Percentage","Units"),
        "ClockHudModule" to setOf("24 Hour","Show Seconds"),
        "DirectionHudModule" to setOf("Show Degrees"),
        "ToggleSprintModule" to emptySet()
    )
    private val fixed=setOf("ArmorStatusModule","ArmorBarModule","KeystrokesModule","PotionStatusModule","UhcOverlayModule")
    fun settings(module:Module):List<Setting> {
        if(unavailable(module)!=null)return emptyList()
        if(module.javaClass.simpleName in fixed)return emptyList()
        val allowed=extras[module.javaClass.simpleName]?:return when(module.javaClass.simpleName) {
            "AutoHideHudModule"->module.settings.filter { it.name=="Hide Delay" }
            "HypixelAddonsModule"->module.settings.filter { it.name=="Auto GG" }
            else->module.settings
        }
        return module.settings.filter { it.name in common || it.name in allowed }
    }
    fun note(module:Module)=unavailable(module) ?: if(module.javaClass.simpleName in fixed)"Basic overlay with a fixed layout." else module.description
}
