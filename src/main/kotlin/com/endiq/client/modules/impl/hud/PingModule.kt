package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
import net.minecraft.client.MinecraftClient
class PingModule : Module("Ping Display", "Shows server ping", Category.HUD) {
    val textColor   = color("Text Color", r=255, g=255, b=255)
    val bgColor     = color("Background", r=0, g=0, b=0, a=120)
    val showBg      = bool("Show Background", default=true)
    val colorGood   = color("Good Color", r=85, g=255, b=85)
    val colorMid    = color("Mid Color", r=255, g=170, b=0)
    val colorBad    = color("Bad Color", r=255, g=85, b=85)
    val goodBelow   = slider("Good Below", default=80f, min=0f, max=200f, suffix="ms")
    val badAbove    = slider("Bad Above", default=150f, min=50f, max=500f, suffix="ms")
    val scale       = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX        = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY        = slider("Position Y", default=32f, min=0f, max=100f, suffix="%")
    val shadow      = bool("Text Shadow", default=true)
    val colorCode   = bool("Color By Ping", default=true)
    val style       = dropdown("Style", options=arrayOf("Ping: Xms", "X ms", "X ping"), default=0)
    val showBar     = bool("Show Signal Bar", default=false)
    fun getText(): String {
        val mc = MinecraftClient.getInstance()
        val uuid = mc.session.uuidOrNull ?: return "Ping: N/A"
        val ping = mc.networkHandler?.getPlayerListEntry(uuid)?.latency ?: return "Ping: N/A"
        return "Ping: ${ping}ms"
    }
    init { enable() }
}
