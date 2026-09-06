package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class MemoryHudModule : Module("Memory Usage", "Shows RAM usage", Category.HUD) {
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val bgColor     = color("Background", r=0, g=0, b=0, a=120)
    val showBg      = bool("Show Background", default=true)
    val showBar     = bool("Show Memory Bar", default=true)
    val barColor    = color("Bar Color", r=91, g=155, b=213)
    val warnColor   = color("Warning Color", r=255, g=85, b=85)
    val warnAt      = slider("Warn At", default=80f, min=50f, max=100f, suffix="%")
    val unit        = dropdown("Unit", options=arrayOf("MB", "GB", "%"), default=0)
    val scale       = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX        = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=72f, min=0f, max=100f, suffix="%")
    val shadow      = bool("Text Shadow", default=true)
    val showMax     = bool("Show Max", default=true)
    val barWidth    = slider("Bar Width", default=60f, min=20f, max=200f)
    fun usedBytes()=Runtime.getRuntime().let { it.totalMemory()-it.freeMemory() }
    fun maxBytes()=Runtime.getRuntime().maxMemory().coerceAtLeast(1)
    fun fraction()=(usedBytes().toDouble()/maxBytes()).coerceIn(0.0,1.0)
    fun getText(used:Long=usedBytes(),max:Long=maxBytes()):String {
        val safeMax=max.coerceAtLeast(1);val safeUsed=used.coerceIn(0,safeMax)
        fun amount(value:Long)=when(unit.selected) {
            1->java.lang.String.format(java.util.Locale.ROOT,"%.2f",value/1073741824.0)
            2->java.lang.String.format(java.util.Locale.ROOT,"%.1f",value*100.0/safeMax)
            else->(value/1048576).toString()
        }
        return "RAM: ${amount(safeUsed)}${if(showMax.value)"/${amount(safeMax)}" else ""} ${unit.value}"
    }
}
