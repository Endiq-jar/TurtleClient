package com.endiq.client.modules.impl.utility
import com.endiq.client.modules.Module
class ChatModule : Module("Chat", "Custom chat appearance", Category.UTILITY) {
    val bgColor     = color("Background", r=0, g=0, b=0, a=140)
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val chatWidth   = slider("Chat Width", default=320f, min=100f, max=600f)
    val chatHeight  = slider("Chat Height", default=100f, min=40f, max=300f)
    val opacity     = slider("Opacity", default=0.9f, min=0.1f, max=1f)
    val fontSize    = slider("Font Size", default=1f, min=0.5f, max=2f, suffix="x")
    val fadeTime    = slider("Fade Time", default=100f, min=0f, max=500f)
    val maxLines    = slider("Max Lines", default=100f, min=10f, max=500f)
    val showTimestamps = bool("Show Timestamps", default=false)
    val timestampColor = color("Timestamp Color", r=128, g=128, b=128)
    val shadow      = bool("Text Shadow", default=true)
    val compactMode = bool("Compact Mode", default=false)
    val filterSpam  = bool("Filter Spam", default=false)
    val mentionAlert= bool("Mention Alert", default=true)
    val mentionColor= color("Mention Color", r=255, g=255, b=0)
    init { enable() }
}
