package com.endiq.client.gui.settings

import com.endiq.client.compat.*
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import org.lwjgl.glfw.GLFW

class TextEditorScreen(private val heading:String,private val help:String,value:String,limit:Int,
                       private val parent:Screen,private val apply:(String)->Unit):ClientScreen(heading) {
    private val input=TextInput(limit).also { it.set(value) }
    private var panel=UiRect(0,0,1,1)
    private var field=panel
    private var start=0
    private val actions=ActionButtons()
    override fun init() {
        panel=centeredPanel(width,height,440,180);field=UiRect(panel.x+12,panel.y+65,panel.width-24,27)
        actions.clear();val w=(panel.width-30)/2
        actions.add(UiRect(panel.x+12,panel.bottom-36,w,24),UiAction("text.save","Save") { apply(input.text);closeGui() },true)
        actions.add(UiRect(panel.x+18+w,panel.bottom-36,w,24),UiAction("text.cancel","Cancel") { closeGui() })
    }
    override fun renderGui(ctx:GuiContext,mx:Int,my:Int,delta:Float) {
        super.renderGui(ctx,mx,my,delta);ctx.fill(0,0,width,height,0xDC0B1418.toInt());Theme.panel(ctx,panel)
        Theme.label(ctx,textRenderer,heading,panel.x+12,panel.y+14,Theme.TEXT,panel.width-24)
        Theme.label(ctx,textRenderer,help.ifEmpty { "Ctrl+A: select all / Ctrl+V: paste" },panel.x+12,panel.y+35,Theme.MUTED,panel.width-24)
        Theme.panel(ctx,field,Theme.BACKGROUND,Theme.ACCENT,4);start=0
        while(start<input.cursor && textRenderer.getWidth(input.text.substring(start,input.cursor))>field.width-20)start=input.text.offsetByCodePoints(start,1)
        ctx.enableScissor(field.x+5,field.y+2,field.right-5,field.bottom-2)
        try {
            val lo=input.selection.first.coerceAtLeast(start);val hi=input.selection.last.coerceAtLeast(start)
            if(hi>lo)ctx.fill(field.x+7+textRenderer.getWidth(input.text.substring(start,lo)),field.y+6,field.x+7+textRenderer.getWidth(input.text.substring(start,hi)),field.y+19,Theme.ACTIVE)
            Theme.label(ctx,textRenderer,input.text.substring(start),field.x+7,field.y+9)
            if(System.currentTimeMillis()/500%2==0L) {
                val cx=field.x+7+textRenderer.getWidth(input.text.substring(start,input.cursor));ctx.fill(cx,field.y+6,cx+1,field.y+19,Theme.ACCENT)
            }
        } finally { ctx.disableScissor() }
        Theme.actions(ctx,textRenderer,actions,mx,my)
    }
    override fun onMouseClicked(mx:Double,my:Double,button:Int):Boolean {
        if(actions.click(mx,my,button))return true
        if(button==0 && field.contains(mx,my)) {
            var at=start
            while(at<input.text.length && field.x+7+textRenderer.getWidth(input.text.substring(start,at))<mx)at=input.text.offsetByCodePoints(at,1)
            input.place(at);return true
        }
        return false
    }
    override fun onCharTyped(text:String):Boolean { input.insert(text);return true }
    override fun onKeyPressed(key:Int,scancode:Int,modifiers:Int):Boolean {
        val ctrl=modifiers and (GLFW.GLFW_MOD_CONTROL or GLFW.GLFW_MOD_SUPER)!=0;val shift=modifiers and GLFW.GLFW_MOD_SHIFT!=0
        when {
            ctrl && key==GLFW.GLFW_KEY_A->input.selectAll()
            ctrl && key==GLFW.GLFW_KEY_V->input.insert(clipboardText())
            ctrl && key==GLFW.GLFW_KEY_C->copyToClipboard(input.selectedText())
            ctrl && key==GLFW.GLFW_KEY_X->{copyToClipboard(input.selectedText());input.insert("")}
            key==GLFW.GLFW_KEY_BACKSPACE->input.backspace()
            key==GLFW.GLFW_KEY_DELETE->input.delete()
            key==GLFW.GLFW_KEY_LEFT->input.move(-1,shift)
            key==GLFW.GLFW_KEY_RIGHT->input.move(1,shift)
            key==GLFW.GLFW_KEY_HOME->input.home(shift)
            key==GLFW.GLFW_KEY_END->input.end(shift)
            key==GLFW.GLFW_KEY_ENTER->{apply(input.text);closeGui()}
            else->return super.onKeyPressed(key,scancode,modifiers)
        }
        return true
    }
    override fun closeGui() { com.endiq.client.config.ModulePreferences.save();MinecraftClient.getInstance().setScreen(parent) }
}
