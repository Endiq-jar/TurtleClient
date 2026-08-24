package com.endiq.client.modules

sealed class Setting(val name: String, val description: String = "")

class BoolSetting(name: String, desc: String = "", var value: Boolean) : Setting(name, desc)
class SliderSetting(name: String, desc: String = "", var value: Float, val min: Float, val max: Float, val suffix: String = "") : Setting(name, desc)
class ColorSetting(name: String, desc: String = "", var r: Int = 255, var g: Int = 255, var b: Int = 255, var a: Int = 255) : Setting(name, desc) {
    fun toArgb() = (a.coerceIn(0,255) shl 24) or (r.coerceIn(0,255) shl 16) or (g.coerceIn(0,255) shl 8) or b.coerceIn(0,255)
}
class DropdownSetting(name: String, desc: String = "", val options: List<String>, var selected: Int = 0) : Setting(name, desc) {
    val value get() = options[selected]
}
