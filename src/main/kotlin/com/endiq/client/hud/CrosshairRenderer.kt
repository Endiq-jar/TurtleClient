package com.endiq.client.hud

import com.endiq.client.compat.*
import com.endiq.client.modules.ModuleManager
import com.endiq.client.modules.impl.hud.CrosshairModule
import kotlin.math.*

object CrosshairRenderer {
    @JvmStatic fun replacesVanilla():Boolean {
        val mod=ModuleManager.get<CrosshairModule>()?:return false
        return mod.enabled && mod.hideVanilla.value && firstPersonView() && MinecraftClient.getInstance().player?.isSpectator!=true
    }
    fun draw(ctx:GuiContext) {
        val mod=ModuleManager.get<CrosshairModule>()?:return
        val player=MinecraftClient.getInstance().player?:return
        if(!mod.enabled || !firstPersonView() || player.isSpectator)return
        val x=ctx.scaledWindowWidth/2+mod.offsetX.value.toInt();val y=ctx.scaledWindowHeight/2+mod.offsetY.value.toInt()
        val size=(mod.size.value*(if(mod.attackScale.value)1+(1-player.getAttackCooldownProgress(0f))*.5f else 1f)).roundToInt()
        val thick=mod.thickness.value.roundToInt().coerceAtLeast(1);val gap=mod.gap.value.toInt()
        val tint=when(mod.colorMode.selected){1->HudStyles.rainbow(mod.rainbowSpeed.value);2->if(targetsEntity())0xFFFF8A80.toInt() else mod.color.toArgb();else->mod.color.toArgb()}
        fun color(value:Int):Int {
            val alpha=((value ushr 24)*mod.alpha.value/255f*mod.opacity.value).toInt().coerceIn(0,255)
            return (alpha shl 24) or (value and 0xFFFFFF)
        }
        fun pixel(cx:Int,cy:Int,w:Int,h:Int,tone:Int) {
            if(mod.outline.value)ctx.fill(cx-1,cy-1,cx+w+1,cy+h+1,color(mod.outlineColor.toArgb()))
            ctx.fill(cx,cy,cx+w,cy+h,color(tone))
        }
        when(mod.style.selected) {
            0,3->{
                pixel(x-size-gap,y-thick/2,size,thick,tint);pixel(x+gap+1,y-thick/2,size,thick,tint)
                pixel(x-thick/2,y+gap+1,thick,size,tint)
                if(mod.style.selected==0)pixel(x-thick/2,y-size-gap,thick,size,tint)
            }
            1->pixel(x-mod.dotSize.value.toInt()/2,y-mod.dotSize.value.toInt()/2,mod.dotSize.value.toInt(),mod.dotSize.value.toInt(),tint)
            2->{ for(i in 0 until 64) { val angle=i*PI/32;pixel(x+(cos(angle)*(size+gap)).roundToInt()-thick/2,y+(sin(angle)*(size+gap)).roundToInt()-thick/2,thick,thick,tint) } }
            4->{ for(i in gap..gap+size) for(sx in listOf(-1,1))for(sy in listOf(-1,1))pixel(x+i*sx-thick/2,y+i*sy-thick/2,thick,thick,tint) }
            5->Unit
        }
        if(mod.showDot.value) {
            val d=mod.dotSize.value.toInt();pixel(x-d/2,y-d/2,d,d,mod.dotColor.toArgb())
        }
    }
}
