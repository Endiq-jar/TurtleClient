package com.endiq.client.modules.impl.utility

import com.endiq.client.compat.*
import com.endiq.client.modules.Module

class WaypointsModule : Module("Waypoints","Temporary HUD waypoints for this world session",Category.UTILITY) {
    val label=text("New waypoint name",default="Waypoint",limit=32)
    val textColor=color("Text Color",r=134,g=232,b=188)
    val bgColor=color("Background",r=12,g=22,b=25,a=160)
    val showBg=bool("Show Background",default=true)
    val showLabels=bool("Show Labels",default=true)
    val showDist=bool("Show Distance",default=true)
    val maxDist=slider("Max Distance",default=500f,min=50f,max=5000f,suffix="m")
    val minDist=slider("Min Distance",default=1f,min=0f,max=20f,suffix="m")
    val scale=slider("Scale",default=1f,min=.5f,max=3f,suffix="x")
    val posX=slider("Position X",default=70f,min=0f,max=100f,suffix="%")
    val posY=slider("Position Y",default=12f,min=0f,max=100f,suffix="%")
    val shadow=bool("Text Shadow",default=true)
    data class Waypoint(val name:String,val pos:BlockPos)
    val waypoints=mutableListOf<Waypoint>()
    private var owner:ClientWorld?=null
    fun tick() {
        val world=MinecraftClient.getInstance().world
        if(world!==owner) { waypoints.clear();owner=world }
    }
    fun visibleText():String {
        val player=MinecraftClient.getInstance().player?:return ""
        return waypoints.mapNotNull { p ->
            val dx=p.pos.x+.5-player.x;val dy=p.pos.y.toDouble()-player.y;val dz=p.pos.z+.5-player.z
            val distance=kotlin.math.sqrt(dx*dx+dy*dy+dz*dz)
            if(distance<minDist.value || distance>maxDist.value)null
            else (if(showLabels.value)p.name else "")+(if(showDist.value)" ${"%.0f".format(distance)}m" else "")+"  [${p.pos.x}, ${p.pos.y}, ${p.pos.z}]"
        }.take(8).joinToString("\n")
    }
    init {
        action("Add current position","Join a world to create a waypoint.",{MinecraftClient.getInstance().player!=null}) {
            tick()
            val player=MinecraftClient.getInstance().player!!
            if(waypoints.size>=32)"32 waypoints reached. Remove one first."
            else { waypoints+=Waypoint(label.value.ifBlank { "Waypoint ${waypoints.size+1}" },BlockPos(kotlin.math.floor(player.x).toInt(),kotlin.math.floor(player.y).toInt(),kotlin.math.floor(player.z).toInt()));enable();"Waypoint added for this world session." }
        }
        action("Remove last waypoint") { if(waypoints.isNotEmpty())waypoints.removeAt(waypoints.lastIndex);"Last waypoint removed." }
        action("Clear waypoints") { waypoints.clear();"Waypoints cleared." }
        enable()
    }
}
