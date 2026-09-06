package com.endiq.client.modules.impl.utility

import com.endiq.client.compat.*
import com.endiq.client.modules.Module

class AutoTextModule : Module("Auto Text","Configurable, opt-in join messages and reminders",Category.UTILITY) {
    val message=text("Message",default="Hello!",limit=256)
    val onJoinMsg=bool("Message On Join",default=true)
    val delay=slider("Send Delay",default=1f,min=0f,max=10f,suffix="s")
    val repeatMsg=bool("Repeat Message",default=false)
    val repeatDelay=slider("Repeat Delay",default=60f,min=5f,max=300f,suffix="s")
    val whisper=bool("Send As Whisper",default=false)
    val whisperTarget=text("Whisper Target",default="",limit=16)
    private val schedule=AutoTextSchedule()
    fun joined() { schedule.joined(enabled && onJoinMsg.value,(delay.value*1000).toLong()) }
    fun send():String {
        if(MinecraftClient.getInstance().player==null)return "Join a world before sending a message."
        val content=message.value.trim();if(content.isEmpty())return "Enter a message first."
        if(whisper.value && !whisperTarget.value.matches(Regex("[A-Za-z0-9_]{3,16}")))return "Enter a valid whisper target."
        sendChatMessage(if(whisper.value)"/msg ${whisperTarget.value} $content" else content)
        return "Message sent."
    }
    fun tick() {
        if(!enabled || MinecraftClient.getInstance().player==null) { schedule.reset();return }
        if(schedule.due(onJoinMsg.value,repeatMsg.value,(repeatDelay.value*1000).toLong()))send()
    }
    override fun onDisable() { schedule.reset() }
    init { action("Send now","Join a world before sending.",{MinecraftClient.getInstance().player!=null}) { send() } }
}
