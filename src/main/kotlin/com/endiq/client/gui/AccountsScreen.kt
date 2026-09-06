package com.endiq.client.gui

import com.endiq.client.accounts.*
import com.endiq.client.compat.*
import com.endiq.client.gui.components.*
import com.endiq.client.gui.components.TurtleTheme as Theme
import org.lwjgl.glfw.GLFW
import java.net.URI
import java.util.UUID

class AccountsScreen(private val parent:Screen) : ClientScreen("Accounts") {
    private enum class Mode { LIST, OFFLINE, SETUP, DEVICE }
    private var mode=Mode.LIST
    private var panel=UiRect(0,0,1,1)
    private var viewport=panel
    private var track=panel
    private var inputRect=panel
    private val input=TextInput(64)
    private val scroll=ScrollState()
    private val buttons=ActionButtons { feedback=it }
    private var selected:UUID?=null
    private var feedback=""
    private var buttonFocus=-1
    private var visibleStart=0
    override fun init() {
        Accounts.initialize();selected=selected ?: Accounts.active?.id
        panel=centeredPanel(width,height,490,386)
        viewport=UiRect(panel.x+8,panel.y+52,panel.width-28,(panel.height-147).coerceAtLeast(1))
        track=UiRect(panel.right-14,viewport.y+2,6,(viewport.height-4).coerceAtLeast(1))
        inputRect=UiRect(panel.x+14,panel.y+103,panel.width-28,24)
        layoutButtons();scroll.endDrag();updateBounds()
    }
    private fun mode(value:Mode) { mode=value;feedback="";buttonFocus=-1;layoutButtons() }
    private fun layoutButtons() {
        buttons.clear()
        val x=panel.x+10;val gap=6;val w=(panel.width-32)/3;val y=panel.bottom-62
        fun add(id:String,label:String,rect:UiRect,primary:Boolean=false,enabled:()->Boolean={true},why:String="",run:()->Unit) {
            buttons.add(rect,UiAction(id,label,enabled,why,run),primary,id=="account.remove")
        }
        when(mode) {
            Mode.LIST -> {
                add("account.add.offline","Add offline",UiRect(x,y,w,23)) { input.set("");mode(Mode.OFFLINE) }
                add("account.add.microsoft","Microsoft",UiRect(x+w+gap,y,w,23)) {
                    if(!MicrosoftLogin.validClientId(Accounts.repository.microsoftClientId)) { input.set(Accounts.repository.microsoftClientId);mode(Mode.SETUP) }
                    else { Accounts.signIn();mode(Mode.DEVICE) }
                }
                add("account.setup","App setup",UiRect(x+(w+gap)*2,y,w,23)) { input.set(Accounts.repository.microsoftClientId);mode(Mode.SETUP) }
                add("account.use","Use account",UiRect(x,y+29,w,23),true,{ selectedProfile()?.let { Accounts.ready(it) }==true && !Accounts.busy && MinecraftClient.getInstance().world==null },
                    "Disconnect first; Microsoft profiles may need a new sign-in.") { selectedProfile()?.let(Accounts::use) }
                add("account.remove","Remove",UiRect(x+w+gap,y+29,w,23),false,{ Accounts.canForget(selectedProfile()) },"Switch away first. The launcher account cannot be removed.") {
                    selectedProfile()?.let(Accounts::forget);selected=Accounts.active?.id;updateBounds()
                }
                add("account.back","Back",UiRect(x+(w+gap)*2,y+29,w,23)) { closeGui() }
            }
            Mode.OFFLINE,Mode.SETUP -> {
                add("account.form.save",if(mode==Mode.OFFLINE)"Add profile" else "Save app ID",UiRect(x,panel.bottom-33,w,23),true) {
                    runCatching {
                        if(mode==Mode.OFFLINE)selected=Accounts.addOffline(input.text).id else Accounts.setClientId(input.text)
                        mode(Mode.LIST)
                    }.onFailure { feedback=it.message ?: "Please check the entered value." }
                }
                if(mode==Mode.SETUP)add("account.registration.help","Setup guide",UiRect(x+w+gap,panel.bottom-33,w,23)) {
                    openUri(URI("https://learn.microsoft.com/entra/identity-platform/quickstart-register-app"))
                }
                add("account.form.cancel","Cancel",UiRect(x+(w+gap)*2,panel.bottom-33,w,23)) { mode(Mode.LIST) }
            }
            Mode.DEVICE -> {
                add("account.microsoft.open","Open Microsoft",UiRect(x,y,(panel.width-26)/2,23),true,{Accounts.challenge!=null},"Waiting for Microsoft...") {
                    Accounts.challenge?.verificationUri?.let { if(MicrosoftLogin.safeBrowserUri(it))openUri(it) }
                }
                add("account.microsoft.copy","Copy code",UiRect(panel.x+panel.width/2+3,y,(panel.width-26)/2,23),false,{Accounts.challenge!=null},"Waiting for Microsoft...") {
                    Accounts.challenge?.let { copyToClipboard(it.userCode);feedback="Code copied. Paste it only on Microsoft's sign-in page." }
                }
                add("account.microsoft.cancel","Cancel sign-in",UiRect(x,panel.bottom-33,panel.width-20,23)) { Accounts.cancel();mode(Mode.LIST) }
            }
        }
    }
    private fun selectedProfile()=Accounts.profiles().firstOrNull { it.id==selected }
    private fun updateBounds()=scroll.update(Accounts.profiles().size*47+6,viewport.height)
    private fun lines(ctx:GuiContext,text:String,x:Int,y:Int,maxWidth:Int,maxLines:Int=3,color:Int=Theme.MUTED) {
        var line="";var row=0
        for(word in text.split(' ')) {
            if(textRenderer.getWidth("$line $word")>maxWidth && line.isNotEmpty()) {
                Theme.label(ctx,textRenderer,line,x,y+row*11,color,maxWidth);row++;line=""
                if(row>=maxLines)return
            }
            line=if(line.isEmpty())word else "$line $word"
        }
        if(row<maxLines)Theme.label(ctx,textRenderer,line,x,y+row*11,color,maxWidth)
    }
    override fun renderGui(ctx:GuiContext,mx:Int,my:Int,delta:Float) {
        super.renderGui(ctx,mx,my,delta)
        if(mode==Mode.DEVICE && !Accounts.busy && Accounts.challenge==null) { selected=Accounts.lastAdded ?: selected;mode(Mode.LIST);updateBounds() }
        ctx.fill(0,0,width,height,0xD00B1418.toInt());Theme.panel(ctx,panel)
        ctx.drawTexture(Theme.icon("account"),panel.x+12,panel.y+12,22,22,Theme.ACCENT)
        Theme.label(ctx,textRenderer,when(mode){Mode.LIST->"ACCOUNTS";Mode.OFFLINE->"OFFLINE PROFILE";Mode.SETUP->"MICROSOFT SETUP";Mode.DEVICE->"MICROSOFT SIGN-IN"},panel.x+42,panel.y+13)
        Theme.label(ctx,textRenderer,"Active: ${Accounts.active?.name.orEmpty()}",panel.x+42,panel.y+27,Theme.MUTED,panel.width-58)
        when(mode) {
            Mode.LIST -> {
                updateBounds();ctx.enableScissor(viewport.x,viewport.y,viewport.right,viewport.bottom)
                try {
                    Accounts.profiles().forEachIndexed { i,profile ->
                        val row=UiRect(viewport.x,viewport.y+3+i*47-scroll.pixels,viewport.width,42)
                        if(row.bottom>viewport.y && row.y<viewport.bottom) {
                            Theme.panel(ctx,row,if(profile.id==selected)Theme.ACTIVE else Theme.CARD,if(profile.id==selected)Theme.ACCENT else Theme.BORDER,5)
                            ctx.drawTexture(Theme.icon("account"),row.x+8,row.y+9,22,22,if(profile.kind==AccountProfile.Kind.OFFLINE)0xFFF3D38A.toInt() else Theme.ACCENT)
                            Theme.label(ctx,textRenderer,profile.name+(if(profile.id==Accounts.active?.id)"  / active" else ""),row.x+39,row.y+8,Theme.TEXT,row.width-46)
                            val status=when(profile.kind) {
                                AccountProfile.Kind.OFFLINE->"Offline - no authenticated multiplayer"
                                AccountProfile.Kind.LAUNCHER->"Original launcher account"
                                AccountProfile.Kind.MICROSOFT->if(Accounts.ready(profile))"Microsoft - ready this session" else "Microsoft - sign-in required"
                            }
                            Theme.label(ctx,textRenderer,status,row.x+39,row.y+23,Theme.MUTED,row.width-46)
                        }
                    }
                } finally { ctx.disableScissor() }
                Theme.scrollbar(ctx,scroll,track,mx,my)
                lines(ctx,if(feedback.isNotEmpty())feedback else Accounts.message,panel.x+11,panel.bottom-90,panel.width-22,2)
            }
            Mode.OFFLINE,Mode.SETUP -> {
                val note=if(mode==Mode.OFFLINE)"Local profiles work in singleplayer and offline-mode servers only. Use 3-16 letters, numbers or underscores."
                    else "Enter an approved Microsoft public-client application ID with Xbox/Minecraft permissions. This is NOT a password or client secret."
                lines(ctx,note,panel.x+14,panel.y+52,panel.width-28,4)
                Theme.panel(ctx,inputRect,Theme.BACKGROUND,Theme.ACCENT,4)
                visibleStart=0
                while(visibleStart<input.cursor && textRenderer.getWidth(input.text.substring(visibleStart,input.cursor))>inputRect.width-18)
                    visibleStart=input.text.offsetByCodePoints(visibleStart,1)
                ctx.enableScissor(inputRect.x+5,inputRect.y+2,inputRect.right-5,inputRect.bottom-2)
                try {
                    val start=input.selection.first.coerceAtLeast(visibleStart)
                    val end=input.selection.last.coerceAtLeast(visibleStart)
                    val sx=inputRect.x+7+textRenderer.getWidth(input.text.substring(visibleStart,start))
                    val ex=inputRect.x+7+textRenderer.getWidth(input.text.substring(visibleStart,end))
                    if(end>start)ctx.fill(sx,inputRect.y+5,ex,inputRect.y+18,Theme.ACTIVE)
                    Theme.label(ctx,textRenderer,input.text.substring(visibleStart),inputRect.x+7,inputRect.y+8,Theme.TEXT)
                    if(buttonFocus<0 && System.currentTimeMillis()/500%2==0L) {
                        val cx=inputRect.x+7+textRenderer.getWidth(input.text.substring(visibleStart,input.cursor))
                        ctx.fill(cx,inputRect.y+6,cx+1,inputRect.y+17,Theme.ACCENT)
                    }
                } finally { ctx.disableScissor() }
                lines(ctx,feedback,panel.x+14,inputRect.bottom+9,panel.width-28,3,Theme.DANGER)
                if(panel.height>290)lines(ctx,"Ctrl+A selects all. Ctrl+V pastes. Tokens stay in memory and are never written to the account file.",panel.x+14,panel.bottom-81,panel.width-28,3)
            }
            Mode.DEVICE -> {
                lines(ctx,if(feedback.isNotEmpty())feedback else Accounts.message,panel.x+14,panel.y+52,panel.width-28,3)
                val code=Accounts.challenge?.userCode ?: "Requesting code..."
                val rect=UiRect(panel.x+14,panel.y+91,panel.width-28,32)
                Theme.panel(ctx,rect,Theme.ACTIVE,Theme.ACCENT,5)
                Theme.label(ctx,textRenderer,code,rect.x+(rect.width-textRenderer.getWidth(code))/2,rect.y+12,Theme.TEXT)
                if(panel.height>250)lines(ctx,"Use only the official Microsoft page. No password is entered in TurtleClient.",panel.x+14,rect.bottom+12,panel.width-28,3)
            }
        }
        Theme.actions(ctx,textRenderer,buttons,mx,my)
        buttons.entries.getOrNull(buttonFocus)?.bounds?.let { ctx.fill(it.x+5,it.bottom-3,it.right-5,it.bottom-2,Theme.ACCENT) }
    }
    override fun onMouseClicked(mx:Double,my:Double,button:Int):Boolean {
        feedback=""
        if(buttons.click(mx,my,button))return true
        if(button==0 && mode in listOf(Mode.OFFLINE,Mode.SETUP) && inputRect.contains(mx,my)) {
            buttonFocus=-1
            var at=visibleStart
            while(at<input.text.length && inputRect.x+7+textRenderer.getWidth(input.text.substring(visibleStart,at))<mx) at=input.text.offsetByCodePoints(at,1)
            input.place(at);return true
        }
        if(mode==Mode.LIST && button==0) {
            if(scroll.beginDrag(mx,my,track))return true
            if(viewport.contains(mx,my)) {
                val index=((my-viewport.y-3+scroll.pixels)/47).toInt()
                Accounts.profiles().getOrNull(index)?.let { selected=it.id;return true }
            }
        }
        return super.onMouseClicked(mx,my,button)
    }
    override fun onMouseScrolled(mx:Double,my:Double,horizontal:Double,vertical:Double)=
        (mode==Mode.LIST && (viewport.contains(mx,my)||track.contains(mx,my)) && scroll.wheel(vertical,horizontal)) || super.onMouseScrolled(mx,my,horizontal,vertical)
    override fun onMouseDragged(mx:Double,my:Double,button:Int,dx:Double,dy:Double)=
        (button==0 && scroll.drag(my,track)) || super.onMouseDragged(mx,my,button,dx,dy)
    override fun onMouseReleased(mx:Double,my:Double,button:Int)=scroll.endDrag() || super.onMouseReleased(mx,my,button)
    override fun onCharTyped(text:String):Boolean {
        if(buttonFocus<0 && (mode==Mode.OFFLINE || mode==Mode.SETUP)) { input.insert(text);feedback="";return true }
        return super.onCharTyped(text)
    }
    override fun onKeyPressed(key:Int,scancode:Int,modifiers:Int):Boolean {
        if(key==GLFW.GLFW_KEY_TAB) {
            val step=if(modifiers and GLFW.GLFW_MOD_SHIFT!=0)-1 else 1
            buttonFocus=if(buttonFocus<0 && step<0)buttons.entries.lastIndex else Math.floorMod(buttonFocus+step,buttons.entries.size)
            return true
        }
        if(buttonFocus>=0 && (key==GLFW.GLFW_KEY_ENTER || key==GLFW.GLFW_KEY_KP_ENTER || key==GLFW.GLFW_KEY_SPACE)) {
            val entry=buttons.entries[buttonFocus];if(!entry.action.invoke())feedback=entry.action.disabledReason;return true
        }
        if(mode==Mode.LIST && (key==GLFW.GLFW_KEY_UP || key==GLFW.GLFW_KEY_DOWN)) {
            val all=Accounts.profiles();val index=all.indexOfFirst { it.id==selected };val next=(index+if(key==GLFW.GLFW_KEY_UP)-1 else 1).coerceIn(0,all.lastIndex)
            selected=all[next].id;scroll.moveTo((next*47).toDouble());return true
        }
        if(mode==Mode.LIST && key==GLFW.GLFW_KEY_ENTER) { selectedProfile()?.let(Accounts::use);return true }
        if(key==GLFW.GLFW_KEY_ESCAPE && mode!=Mode.LIST) { Accounts.cancel();mode(Mode.LIST);return true }
        if(mode==Mode.OFFLINE || mode==Mode.SETUP) {
            val ctrl=modifiers and (GLFW.GLFW_MOD_CONTROL or GLFW.GLFW_MOD_SUPER)!=0
            val shift=modifiers and GLFW.GLFW_MOD_SHIFT!=0
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
                key==GLFW.GLFW_KEY_ENTER->{buttons.entries.firstOrNull { it.action.id=="account.form.save" }?.action?.invoke()}
                else->return super.onKeyPressed(key,scancode,modifiers)
            }
            return true
        }
        return super.onKeyPressed(key,scancode,modifiers)
    }
    override fun closeGui() { Accounts.cancel();MinecraftClient.getInstance().setScreen(parent) }
}
