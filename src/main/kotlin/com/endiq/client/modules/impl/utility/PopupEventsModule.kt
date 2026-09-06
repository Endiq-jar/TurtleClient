package com.endiq.client.modules.impl.utility

import com.endiq.client.compat.*
import com.endiq.client.modules.Module

class PopupEventsModule(private val now:()->Long={System.nanoTime()/1_000_000}) : Module("Popup Events", "World-join and optional game-message notifications", Category.UTILITY) {
    val bgColor=color("Background",r=20,g=20,b=20,a=200)
    val textColor=color("Text Color",r=255,g=255,b=255)
    val accentColor=color("Accent Color",r=62,g=153,b=112)
    val duration=slider("Duration",default=4f,min=1f,max=10f,suffix="s")
    val fadeTime=slider("Fade Time",default=.5f,min=.1f,max=2f,suffix="s")
    val posX=slider("Position X",default=50f,min=0f,max=100f,suffix="%")
    val posY=slider("Position Y",default=15f,min=0f,max=100f,suffix="%")
    val maxPopups=slider("Max Popups",default=5f,min=1f,max=10f)
    val showIcon=bool("Show Icon",default=true)
    val sound=bool("Play Sound",default=false)
    val style=dropdown("Style",options=arrayOf("Toast","Banner","Minimal"),default=0)
    val onJoin=bool("On World Join",default=true)
    val onMessage=bool("On Game Message",default=false)
    data class Popup(val msg:String,val createdAt:Long,val expiresAt:Long)
    private val popups=ArrayDeque<Popup>()
    fun add(message:String) {
        if(!enabled)return
        val clean=message.replace(Regex("§."),"").filter { it>=' ' && it!='\u007f' }.take(240)
        if(clean.isBlank())return
        val time=now();popups.addLast(Popup(clean,time,time+(duration.value*1000).toLong()))
        prune()
        if(sound.value)playUiClick()
    }
    private fun prune() {
        popups.removeAll { now()>=it.expiresAt }
        while(popups.size>maxPopups.value.toInt().coerceIn(1,10))popups.removeFirst()
    }
    fun visible():List<Popup> { prune();return popups.toList() }
    fun opacity(popup:Popup):Float {
        val fade=(fadeTime.value*1000).coerceAtMost((popup.expiresAt-popup.createdAt)/2f).coerceAtLeast(1f)
        return minOf((now()-popup.createdAt)/fade,(popup.expiresAt-now())/fade).coerceIn(0f,1f)
    }
    override fun onDisable() { popups.clear() }
    init {
        action("Show test popup","Join a world to preview notifications.",{MinecraftClient.getInstance().world!=null}) {
            enable();add("Turtle notifications are working.");MinecraftClient.getInstance().setScreen(null);"Previewing notification."
        }
        enable()
    }
}
