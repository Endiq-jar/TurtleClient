package com.endiq.client.gui.settings

import com.endiq.client.compat.*
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import com.endiq.client.modules.ColorSetting

class ColorEditorScreen(private val setting:ColorSetting,private val parent:Screen):ClientScreen("Color") {
    private val values=intArrayOf(setting.r,setting.g,setting.b,setting.a)
    private var panel=UiRect(0,0,1,1)
    private var tracks=emptyList<UiRect>()
    private var dragging=-1
    private val buttons=ActionButtons()
    override fun init() {
        panel=centeredPanel(width,height,352,274);val row=((panel.height-100)/4).coerceIn(20,36)
        tracks=(0..3).map { UiRect(panel.x+12,panel.y+47+it*row,panel.width-24,6) }
        buttons.clear();val w=(panel.width-30)/2
        buttons.add(UiRect(panel.x+12,panel.bottom-32,w,22),UiAction("color.apply","Apply color") {
            setting.r=values[0];setting.g=values[1];setting.b=values[2];setting.a=values[3];closeGui()
        },true)
        buttons.add(UiRect(panel.x+18+w,panel.bottom-32,w,22),UiAction("color.cancel","Cancel") { closeGui() })
    }
    override fun renderGui(ctx:GuiContext,mx:Int,my:Int,delta:Float) {
        super.renderGui(ctx,mx,my,delta);ctx.fill(0,0,width,height,0xD00B1418.toInt());Theme.panel(ctx,panel)
        Theme.label(ctx,textRenderer,setting.name,panel.x+12,panel.y+12,Theme.TEXT,panel.width-24)
        val labels=listOf("Red","Green","Blue","Opacity");val colors=listOf(0xFFEF9F99.toInt(),Theme.ACCENT,0xFF83C9F2.toInt(),Theme.TEXT)
        tracks.forEachIndexed { i,track ->
            Theme.label(ctx,textRenderer,"${labels[i]}  ${values[i]}",track.x,track.y-13,colors[i])
            Theme.rounded(ctx,track,Theme.CARD,3)
            val filled=(track.width*values[i]/255f).toInt();Theme.rounded(ctx,UiRect(track.x,track.y,filled,6),colors[i],3)
            Theme.rounded(ctx,UiRect(track.x+filled-3,track.y-3,6,12),Theme.TEXT,3)
        }
        val preview=UiRect(panel.x+12,panel.bottom-62,panel.width-24,22)
        ctx.fill(preview.x,preview.y,preview.right,preview.bottom,0xFF6C7773.toInt())
        for(x in preview.x until preview.right step 8) for(y in preview.y until preview.bottom step 8)
            if(((x-preview.x)/8+(y-preview.y)/8)%2==0)ctx.fill(x,y,minOf(x+8,preview.right),minOf(y+8,preview.bottom),0xFF9AABA3.toInt())
        val color=(values[3] shl 24) or (values[0] shl 16) or (values[1] shl 8) or values[2]
        ctx.fill(preview.x,preview.y,preview.right,preview.bottom,color)
        Theme.label(ctx,textRenderer,"#${"%08X".format(color)}",preview.x+6,preview.y+7,Theme.TEXT)
        Theme.actions(ctx,textRenderer,buttons,mx,my)
    }
    private fun set(mx:Double,index:Int) { values[index]=(((mx-tracks[index].x)/tracks[index].width)*255).toInt().coerceIn(0,255) }
    override fun onMouseClicked(mx:Double,my:Double,button:Int):Boolean {
        if(buttons.click(mx,my,button))return true
        if(button==0) tracks.forEachIndexed { i,r ->
            if(UiRect(r.x,r.y-6,r.width,18).contains(mx,my)) { dragging=i;set(mx,i);return true }
        }
        return false
    }
    override fun onMouseDragged(mx:Double,my:Double,button:Int,dx:Double,dy:Double):Boolean {
        if(button==0 && dragging>=0) { set(mx,dragging);return true };return false
    }
    override fun onMouseReleased(mx:Double,my:Double,button:Int):Boolean { val active=dragging>=0;dragging=-1;return active }
    override fun onMouseScrolled(mx:Double,my:Double,horizontal:Double,vertical:Double):Boolean {
        tracks.forEachIndexed { i,r -> if(UiRect(r.x,r.y-15,r.width,24).contains(mx,my)) { values[i]=(values[i]+if(vertical>0)1 else -1).coerceIn(0,255);return true } }
        return false
    }
    override fun closeGui() { com.endiq.client.config.ModulePreferences.save();MinecraftClient.getInstance().setScreen(parent) }
}
