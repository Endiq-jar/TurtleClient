package com.endiq.client.compat

import com.endiq.client.modules.ModuleManager
import com.endiq.client.modules.impl.hud.CpsModule
import com.endiq.client.modules.impl.render.*

object GameplayControls {
    @JvmStatic fun fov(original:Double,worldProjection:Boolean):Double {
        if(!worldProjection)return original
        val configured=ModuleManager.get<FovChangerModule>()?.takeIf { it.enabled }?.fov?.value?.toDouble()
        val base=ClientOptions.fov.toDouble().coerceAtLeast(1.0)
        var result=original*(configured ?: base)/base
        val zoom=ModuleManager.get<ZoomModule>()
        if(zoom!=null && zoom.enabled && MinecraftClient.getInstance().currentScreen==null)
            result+=(zoom.currentFov-result)*zoom.progress
        return result.coerceIn(1.0,170.0)
    }
    @JvmStatic fun gamma(original:Double)=if(ModuleManager.get<FullBrightModule>()?.enabled==true)16.0 else original
    @JvmStatic fun click(button:Int,action:Int) {
        val mc=MinecraftClient.getInstance()
        if(mc.currentScreen!=null || mc.player==null || action!=1 || button !in 0..1)return
        ModuleManager.get<CpsModule>()?.takeIf { it.enabled }?.registerClick(button==1)
    }
    @JvmStatic fun scroll(vertical:Double):Boolean {
        val mc=MinecraftClient.getInstance()
        return mc.currentScreen==null && mc.player!=null && ModuleManager.get<ZoomModule>()?.scroll(vertical)==true
    }
}
