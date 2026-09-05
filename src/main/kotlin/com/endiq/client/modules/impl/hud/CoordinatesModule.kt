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
        val x = floor(p.x).toInt(); val y = floor(p.y).toInt(); val z = floor(p.z).toInt()
        return "XYZ: $x / $y / $z"
    }
    init { enable() }
}
