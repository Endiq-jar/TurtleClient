package com.endiq.client.modules.impl.utility

import com.endiq.client.compat.*
import com.endiq.client.modules.Module

class CameraModule : Module("Camera","Capture a clean PNG or open the screenshots folder",Category.UTILITY) {
    val flashEffect=bool("Flash Effect",default=true)
    val flashColor=color("Flash Color",r=255,g=255,b=255,a=60)
    val flashDuration=slider("Flash Duration",default=.3f,min=.1f,max=1f,suffix="s")
    val notification=bool("Show Notification",default=true)
    var notice=""
        private set
    var noticeAt=0L
        private set
    fun notifyCapture(message:String) { notice=message;noticeAt=System.currentTimeMillis() }
    private var frames=0
    var flashAt=0L
        private set
    fun frame() {
        if(frames<=0 || MinecraftClient.getInstance().currentScreen!=null)return
        frames--
        if(frames==0) { captureScreenshot(notification.value);flashAt=System.currentTimeMillis() }
    }
    init {
        action("Capture clean screenshot","Join a world before capturing.",{MinecraftClient.getInstance().world!=null}) {
            enable();frames=2;MinecraftClient.getInstance().setScreen(null);"Capturing after the menu closes..."
        }
        action("Open screenshots folder") {
            val folder=MinecraftClient.getInstance().runDirectory.resolve("screenshots");check(folder.isDirectory || folder.mkdirs());openPath(folder);"Opened screenshots folder."
        }
        enable()
    }
    override fun onDisable() { frames=0 }
}
