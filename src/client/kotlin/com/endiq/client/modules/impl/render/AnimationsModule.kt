package com.endiq.client.modules.impl.render
import com.endiq.client.modules.Module
class AnimationsModule : Module("Animations", "Custom item animations", Category.RENDER) {
    val swingSpeed  = slider("Swing Speed", default=1f, min=0.1f, max=3f, suffix="x")
    val swingStyle  = dropdown("Swing Style", options=arrayOf("1.7 Style", "1.8 Style", "Modern", "Custom"), default=0)
    val hitEffect   = bool("Hit Effect", default=true)
    val itemBob     = bool("Item Bobbing", default=true)
    val bobSpeed    = slider("Bob Speed", default=1f, min=0f, max=3f)
    val bobHeight   = slider("Bob Height", default=1f, min=0f, max=3f)
    val handX       = slider("Hand X Offset", default=0f, min=-1f, max=1f)
    val handY       = slider("Hand Y Offset", default=0f, min=-1f, max=1f)
    val handZ       = slider("Hand Z Offset", default=0f, min=-1f, max=1f)
    val handScale   = slider("Hand Scale", default=1f, min=0.5f, max=2f)
    val eatingAnim  = bool("Eating Animation", default=true)
    val drinkAnim   = bool("Drinking Animation", default=true)
    val blockAnim   = bool("Block Animation", default=true)
    val bowAnim     = bool("Bow Animation", default=true)
    val throwAnim   = bool("Throw Animation", default=true)
    val offhandAnim = bool("Offhand Animation", default=true)
    init { enable() }
}
