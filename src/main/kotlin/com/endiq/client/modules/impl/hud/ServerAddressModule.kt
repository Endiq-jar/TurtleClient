package com.endiq.client.modules.impl.hud
import com.endiq.client.compat.*
import com.endiq.client.modules.Module
class ServerAddressModule : Module("Server Address", "Shows server address on HUD", Category.HUD) {
    val textColor  = color("Text Color", r=255, g=255, b=255)
    val bgColor    = color("Background", r=0, g=0, b=0, a=120)
    val showBg     = bool("Show Background", default=true)
    val showLabel  = bool("Show Label", default=true)
    val showPort   = bool("Show Port", default=false)
    val showTps    = bool("Show TPS", default=false)
    val showOnSP   = bool("Show In Singleplayer", default=true)
    val scale      = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX       = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY       = slider("Position Y", default=42f, min=0f, max=100f, suffix="%")
    val shadow     = bool("Text Shadow", default=true)
    val style      = dropdown("Style", options=arrayOf("Full Address", "IP Only", "Custom"), default=0)
    val customText = bool("Custom Text", default=false)
    fun getText() = "Server: ${serverAddress() ?: "Singleplayer"}"
    init { enable() }
}
