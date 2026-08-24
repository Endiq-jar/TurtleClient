package com.endiq.client.modules.impl.render
import com.endiq.client.modules.Module
class NoWeatherModule : Module("Weather Changer", "Controls weather rendering", Category.RENDER) {
    val hideRain    = bool("Hide Rain", default=true)
    val hideSnow    = bool("Hide Snow", default=true)
    val hideThunder = bool("Hide Thunder", default=true)
    val hideFog     = bool("Hide Fog", default=false)
    val fogStart    = slider("Fog Start", default=0.1f, min=0f, max=1f)
    val fogEnd      = slider("Fog End", default=1f, min=0f, max=1f)
    val fogColor    = color("Fog Color", r=200, g=210, b=220)
    val opacity     = slider("Rain Opacity", default=0f, min=0f, max=1f)
    val snowOpacity = slider("Snow Opacity", default=0f, min=0f, max=1f)
}
