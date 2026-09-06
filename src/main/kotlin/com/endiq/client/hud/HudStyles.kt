package com.endiq.client.hud

import com.endiq.client.compat.*
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import com.endiq.client.modules.*
import com.endiq.client.modules.Module
import com.endiq.client.modules.impl.hud.*
import kotlin.math.sin

/** Common settings used to be drawn as controls but ignored by the HUD. */
object HudStyles {
    fun rainbow(speed:Float=1f):Int=java.awt.Color.HSBtoRGB(((System.nanoTime()/1_000_000%100000)*speed/5000f)%1f,.55f,1f) or (255 shl 24)
    fun text(ctx:GuiContext,module:Module,raw:String,fallbackX:Int=2,fallbackY:Int=2,lineColors:List<Int>?=null) {
        if(raw.isEmpty())return
        fun slider(name:String,default:Float)=module.settings.filterIsInstance<SliderSetting>().firstOrNull { it.name==name }?.value ?: default
        fun bool(name:String,default:Boolean)=module.settings.filterIsInstance<BoolSetting>().firstOrNull { it.name==name }?.value ?: default
        fun color(names:List<String>,default:Int)=module.settings.filterIsInstance<ColorSetting>().firstOrNull { it.name in names }?.toArgb() ?: default
        fun option(name:String)=module.settings.filterIsInstance<DropdownSetting>().firstOrNull { it.name==name }?.selected ?: 0
        val font=MinecraftClient.getInstance().textRenderer
        val value=if(bool("Bold Text",false))"§l$raw" else raw
        val lines=value.split('\n');val textWidth=lines.maxOf { font.getWidth(it) };val height=lines.size*10
        val extraWidth=when {
            module is FpsModule && module.showGraph.value->module.history.size
            module is CpsModule && module.showBar.value->module.barWidth.value.toInt()
            module is MemoryHudModule && module.showBar.value->module.barWidth.value.toInt()
            module is PingModule && module.showBar.value->textWidth+16
            else->0
        }
        val extraHeight=when {
            module is FpsModule && module.showGraph.value->module.graphHeight.value.toInt()+2
            module is CpsModule && module.showBar.value->5
            module is MemoryHudModule && module.showBar.value->5
            else->0
        }
        val width=maxOf(textWidth,extraWidth);val boxHeight=height+extraHeight
        val scale=slider("Scale",1f).coerceIn(.3f,4f);val pad=slider("BG Padding",2f).toInt()
        val sw=ctx.scaledWindowWidth;val sh=ctx.scaledWindowHeight
        val anchor=slider("Position X",fallbackX*100f/sw)*sw/100f
        val x=(anchor-when(option("Alignment")){1->width*scale/2;2->width*scale;else->0f}).coerceIn(0f,(sw-width*scale).coerceAtLeast(0f))
        val y=(slider("Position Y",fallbackY*100f/sh)*sh/100f).coerceIn(0f,(sh-boxHeight*scale).coerceAtLeast(0f))
        var tint=color(listOf("Text Color","Color"),Theme.TEXT)
        if(option("Color Mode")==1)tint=rainbow(slider("Rainbow Speed",1f))
        if(option("Color Mode")==2) {
            val alpha=(180+sin(System.nanoTime()/1e9*3)*75).toInt();tint=(tint and 0xFFFFFF) or (alpha shl 24)
        }
        if(module is FpsModule && clientFps()<module.warnBelow.value)tint=module.warnColor.toArgb()
        if(module is PingModule && module.colorCode.value)clientPing()?.let { ping -> tint=when { ping<module.goodBelow.value->module.colorGood.toArgb();ping>=module.badAbove.value->module.colorBad.toArgb();else->module.colorMid.toArgb() } }
        if(module is MemoryHudModule && module.fraction()*100>=module.warnAt.value)tint=module.warnColor.toArgb()
        ctx.transformed(x,y,scale) {
            if(bool("Show Background",false))Theme.rounded(ctx,UiRect(-pad,-pad,width+pad*2,boxHeight+pad*2-2),color(listOf("Background Color","Background"),0x88000000.toInt()),slider("BG Radius",2f).toInt())
            lines.forEachIndexed { i,line -> ctx.drawText(font,line,0,i*10,lineColors?.getOrNull(i) ?: tint,bool("Text Shadow",true)) }
            if(module is FpsModule && module.showGraph.value && module.history.isNotEmpty()) {
                val max=(module.history.maxOrNull() ?: 1).coerceAtLeast(1);val h=module.graphHeight.value.toInt()
                module.history.forEachIndexed { i,v -> ctx.fill(i,height+h-v*h/max,i+1,height+h,module.graphColor.toArgb()) }
            }
            if(module is CpsModule && module.showBar.value) {
                val w=module.barWidth.value.toInt();val amount=((module.getLCps()+module.getRCps())/module.maxCps.value).coerceIn(0f,1f)
                ctx.fill(0,height,w,height+3,Theme.CARD);ctx.fill(0,height,(w*amount).toInt(),height+3,module.barColor.toArgb())
            }
            if(module is MemoryHudModule && module.showBar.value) {
                val w=module.barWidth.value.toInt();val fraction=module.fraction()
                ctx.fill(0,height,w,height+3,Theme.CARD)
                ctx.fill(0,height,(w*fraction).toInt(),height+3,if(fraction*100>=module.warnAt.value)module.warnColor.toArgb() else module.barColor.toArgb())
            }
            if(module is PingModule && module.showBar.value) {
                val ping=clientPing()?:0;val level=if(ping<module.goodBelow.value)4 else if(ping<module.badAbove.value)3 else 1
                for(i in 0..3)ctx.fill(textWidth+4+i*3,8-i*2,textWidth+6+i*3,10,if(i<level)tint else Theme.SUBTLE)
            }
        }
    }
}
