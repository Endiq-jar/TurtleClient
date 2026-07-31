package com.endiq.client.modules.impl.hud
import com.endiq.client.modules.Module
class AutoHideHudModule : Module("Auto Hide HUD", "Hides HUD when still", Category.HUD) {
    val hideDelay    = slider("Hide Delay", default=3f, min=0.5f, max=10f, suffix="s")
    val fadeTime     = slider("Fade Time", default=0.5f, min=0f, max=2f, suffix="s")
    val hideOnSneak  = bool("Hide When Sneaking", default=false)
    val hideOnSwim   = bool("Hide When Swimming", default=false)
    val showOnDamage = bool("Show On Damage", default=true)
    val showOnChat   = bool("Show On Chat", default=true)
    val showOnInv    = bool("Show On Inventory", default=true)
    val minAlpha     = slider("Minimum Alpha", default=0f, min=0f, max=200f)
    val hideHotbar   = bool("Hide Hotbar", default=true)
    val hideCrosshair= bool("Hide Crosshair", default=true)
    val hideHealth   = bool("Hide Health", default=true)
    val hideArmor    = bool("Hide Armor", default=true)
    val hideExp      = bool("Hide XP Bar", default=true)
    var hideTimer = 0
    fun tick(moving: Boolean) { if (moving) hideTimer = (hideDelay.value * 20).toInt() else if (hideTimer > 0) hideTimer-- }
    fun shouldShow() = !enabled || hideTimer > 0
}
