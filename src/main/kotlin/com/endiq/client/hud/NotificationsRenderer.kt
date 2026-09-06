package com.endiq.client.hud

import com.endiq.client.compat.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import com.endiq.client.gui.components.UiRect
import com.endiq.client.modules.impl.utility.PopupEventsModule

object NotificationsRenderer {
    fun render(ctx:GuiContext,module:PopupEventsModule) {
        if(!module.enabled)return
        val font=MinecraftClient.getInstance().textRenderer
        val sw=ctx.scaledWindowWidth;val sh=ctx.scaledWindowHeight
        val popups=module.visible();val rowHeight=if(module.style.selected==1)24 else 20
        val top=(sh*module.posY.value/100f).toInt().coerceIn(0,(sh-popups.size*(rowHeight+3)).coerceAtLeast(0))
        popups.forEachIndexed { i,popup ->
            val opacity=module.opacity(popup)
            fun fade(color:Int)=(((color ushr 24)*opacity).toInt() shl 24) or (color and 0xFFFFFF)
            val inset=if(module.showIcon.value)24 else 8
            val text=Theme.fit(font,popup.msg,(sw-inset-16).coerceAtLeast(1))
            val width=if(module.style.selected==1)(sw-16).coerceAtLeast(1) else (font.getWidth(text)+inset+8).coerceAtMost(sw)
            val left=(sw*module.posX.value/100f-width/2).toInt().coerceIn(0,(sw-width).coerceAtLeast(0))
            val y=top+i*(rowHeight+3)
            if(module.style.selected!=2)Theme.rounded(ctx,UiRect(left,y,width,rowHeight),fade(module.bgColor.toArgb()),4)
            ctx.fill(left,y+3,left+2,y+rowHeight-3,fade(module.accentColor.toArgb()))
            if(module.showIcon.value)ctx.drawTexture(Theme.icon("check"),left+7,y+(rowHeight-12)/2,12,12,fade(module.accentColor.toArgb()))
            ctx.drawText(font,text,left+inset,y+(rowHeight-8)/2,fade(module.textColor.toArgb()),false)
        }
    }
}
