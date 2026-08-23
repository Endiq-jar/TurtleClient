package com.endiq.client.modules.impl.utility
import com.endiq.client.modules.Module
class AutoTextModule : Module("Auto Text", "Sends messages on events", Category.UTILITY) {
    val onJoinMsg   = bool("Message On Join", default=true)
    val joinMessage = bool("Custom Join Msg", default=true)
    val onLeaveMsg  = bool("Message On Leave", default=false)
    val leaveMessage= bool("Custom Leave Msg", default=false)
    val delay       = slider("Send Delay", default=1f, min=0f, max=10f, suffix="s")
    val repeatMsg   = bool("Repeat Message", default=false)
    val repeatDelay = slider("Repeat Delay", default=60f, min=5f, max=300f, suffix="s")
    val whisper     = bool("Send As Whisper", default=false)
    val whisperTarget = bool("Whisper Target", default=false)
    var message = "Hello!"
    var sent = false
    var pendingSend = false
}
