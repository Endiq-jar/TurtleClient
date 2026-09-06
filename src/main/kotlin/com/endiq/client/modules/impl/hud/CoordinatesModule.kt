package com.endiq.client.modules.impl.hud
import com.endiq.client.compat.*
import com.endiq.client.modules.Module
import kotlin.math.floor
class CoordinatesModule : Module("Coordinates", "Shows XYZ position", Category.HUD) {
    val textColor  = color("Text Color", r=255, g=255, b=255)
    val bgColor    = color("Background Color", r=0, g=0, b=0, a=120)
    val showBg     = bool("Show Background", default=true)
    val scale      = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX       = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY       = slider("Position Y", default=22f, min=0f, max=100f, suffix="%")
    val showX      = bool("Show X", default=true)
    val showY      = bool("Show Y", default=true)
    val showZ      = bool("Show Z", default=true)
    val showBiome  = bool("Show Biome", default=false)
    val showDim    = bool("Show Dimension", default=false)
    val showDir    = bool("Show Facing", default=true)
    val precision  = slider("Decimal Precision", default=0f, min=0f, max=3f)
    val colorX     = color("X Color", r=255, g=85, b=85)
    val colorY     = color("Y Color", r=85, g=255, b=85)
    val colorZ     = color("Z Color", r=85, g=85, b=255)
    val layout     = dropdown("Layout", options=arrayOf("XYZ Single Line", "XYZ Multi Line", "Compact"), default=0)
    val shadow     = bool("Text Shadow", default=true)
    val showNether = bool("Show Nether Coords", default=false)
    val labelStyle = dropdown("Label Style", options=arrayOf("XYZ:", "X Y Z", "Coords:"), default=0)
    fun getText(): String {
        val p = MinecraftClient.getInstance().player ?: return "XYZ: N/A"
        val digits=precision.value.toInt()
        fun value(v:Double)=if(digits==0)floor(v).toInt().toString() else java.lang.String.format(java.util.Locale.ROOT,"%.${digits}f",v)
        val parts=mutableListOf<String>()
        if(showX.value)parts+=(if(layout.selected==2)"" else "X: ")+value(p.x)
        if(showY.value)parts+=(if(layout.selected==2)"" else "Y: ")+value(p.y)
        if(showZ.value)parts+=(if(layout.selected==2)"" else "Z: ")+value(p.z)
        if(showDir.value) {
            val names=arrayOf("S","SW","W","NW","N","NE","E","SE")
            parts+=names[Math.floorMod(floor(p.yaw/45.0+.5).toInt(),8)]
        }
        val prefix=when(labelStyle.selected){2->"Coords: ";1->"";else->if(layout.selected==2)"XYZ: " else ""}
        return prefix+parts.joinToString(if(layout.selected==1)"\n" else if(layout.selected==2)", " else " / ")
    }
    init { enable() }
}
