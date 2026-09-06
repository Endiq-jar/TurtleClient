package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class CpsModule(private val now:()->Long={System.nanoTime()/1_000_000}) : Module("CPS Counter", "Shows clicks per second", Category.HUD) {
    val textColor    = color("Text Color", r=255, g=255, b=255)
    val bgColor      = color("Background Color", r=0, g=0, b=0, a=120)
    val showBg       = bool("Show Background", default=true)
    val showLabel    = bool("Show Label", default=true)
    val trackLeft    = bool("Track Left Click", default=true)
    val trackRight   = bool("Track Right Click", default=true)
    val showBoth     = bool("Show LMB & RMB Separately", default=false)
    val scale        = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX         = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY         = slider("Position Y", default=12f, min=0f, max=100f, suffix="%")
    val shadow       = bool("Text Shadow", default=true)
    val colorMode    = dropdown("Color Mode", options=arrayOf("Static", "Rainbow"), default=0)
    val maxCps       = slider("Max CPS Display", default=20f, min=5f, max=50f)
    val showBar      = bool("Show CPS Bar", default=false)
    val barColor     = color("Bar Color", r=62, g=153, b=112)
    val barWidth     = slider("Bar Width", default=60f, min=20f, max=200f)
    val alignment    = dropdown("Alignment", options=arrayOf("Left", "Center", "Right"), default=0)
    private val lClicks = ArrayDeque<Long>()
    private val rClicks = ArrayDeque<Long>()
    private fun prune() {
        val time=now()
        for(q in listOf(lClicks,rClicks))while(q.isNotEmpty() && time-q.first()>=1000)q.removeFirst()
    }
    fun registerClick(right:Boolean=false) {
        prune()
        if((right && trackRight.value) || (!right && trackLeft.value)) {
            val q=if(right)rClicks else lClicks;q.addLast(now());while(q.size>500)q.removeFirst()
        }
    }
    fun getLCps():Int { prune();return if(trackLeft.value)lClicks.size else 0 }
    fun getRCps():Int { prune();return if(trackRight.value)rClicks.size else 0 }
    fun getText():String {
        val left=getLCps().coerceAtMost(maxCps.value.toInt());val right=getRCps().coerceAtMost(maxCps.value.toInt())
        return if(showBoth.value)"L: $left R: $right" else (if(showLabel.value)"CPS: " else "")+(left+right).coerceAtMost(maxCps.value.toInt())
    }
    init { enable() }
}
