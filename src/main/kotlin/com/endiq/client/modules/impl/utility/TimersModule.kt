package com.endiq.client.modules.impl.utility

import com.endiq.client.modules.Module
import com.endiq.client.compat.playUiClick

class TimersModule : Module("Stopwatch / Timer", "Start, pause and reset a real stopwatch or countdown", Category.UTILITY) {
    val textColor=color("Text Color",r=255,g=255,b=140)
    val bgColor=color("Background",r=0,g=0,b=0,a=140)
    val showBg=bool("Show Background",default=true)
    val scale=slider("Scale",default=1f,min=.5f,max=3f,suffix="x")
    val posX=slider("Position X",default=50f,min=0f,max=100f,suffix="%")
    val posY=slider("Position Y",default=8f,min=0f,max=100f,suffix="%")
    val showMs=bool("Show Milliseconds",default=false)
    val soundAlert=bool("Sound On Complete",default=true)
    val alertColor=color("Alert Color",r=255,g=120,b=110)
    val countUp=bool("Count Up Mode",default=false)
    val showLabel=bool("Show Label",default=true)
    val defaultTime=slider("Default Time",default=60f,min=1f,max=3600f,suffix="s")
    val shadow=bool("Text Shadow",default=true)
    val clock=TimerClock()
    var finished=false
        private set
    val running get()=clock.running
    fun start(s:Int=defaultTime.value.toInt()) { if(finished)clock.reset();finished=false;clock.start(s);enable() }
    fun stop()=clock.pause()
    fun remaining()=clock.remaining()
    fun tick() {
        if(running && !countUp.value && remaining()==0L) { clock.pause();finished=true;if(soundAlert.value)playUiClick() }
    }
    fun text():String {
        val millis=if(countUp.value)clock.elapsed() else clock.remaining()
        val duration="${millis/60000}:${"%02d".format(millis/1000%60)}"+if(showMs.value)".${"%03d".format(millis%1000)}" else ""
        return (if(showLabel.value)if(countUp.value)"Stopwatch " else "Timer " else "")+duration+(if(finished)" / done" else if(!running)" / paused" else "")
    }
    init {
        action("Start / Resume") { start();"Timer started. Press Escape to return to the game." }
        action("Pause") { stop();"Timer paused." }
        action("Reset") { clock.reset();finished=false;"Timer reset." }
    }
    override fun onDisable()=clock.pause()
}
