package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class KeystrokesModule : Module("Keystrokes", "Shows WASD keys on HUD", Category.HUD) {
    val keyColor      = color("Key Color", r=0, g=0, b=0, a=150)
    val pressedColor  = color("Pressed Color", r=62, g=153, b=112)
    val textColor     = color("Text Color", r=255, g=255, b=255)
    val pressedText   = color("Pressed Text Color", r=0, g=0, b=0)
    val scale         = slider("Scale", default=1f, min=0.5f, max=3f, suffix="x")
    val posX          = slider("Position X", default=2f, min=0f, max=100f, suffix="%")
    val posY          = slider("Position Y", default=80f, min=0f, max=100f, suffix="%")
    val showWASD      = bool("Show WASD", default=true)
    val showSpace     = bool("Show Space", default=true)
    val showShift     = bool("Show Shift", default=false)
    val showLMB       = bool("Show LMB", default=true)
    val showRMB       = bool("Show RMB", default=true)
    val showCps       = bool("Show CPS on Mouse", default=true)
    val keySize       = slider("Key Size", default=16f, min=8f, max=40f)
    val keyGap        = slider("Key Gap", default=2f, min=0f, max=8f)
    val shadow        = bool("Text Shadow", default=true)
    val style         = dropdown("Style", options=arrayOf("Modern", "Classic", "Minimal"), default=0)
    val bgRadius      = slider("Corner Radius", default=2f, min=0f, max=8f)
    val animation     = bool("Press Animation", default=true)
    val fadeSpeed     = slider("Fade Speed", default=0.2f, min=0.05f, max=1f)
    init { enable() }
}
