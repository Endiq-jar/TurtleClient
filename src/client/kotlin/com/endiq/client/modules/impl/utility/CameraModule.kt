package com.endiq.client.modules.impl.utility
import com.endiq.client.modules.Module
class CameraModule : Module("Camera", "Screenshot tools", Category.UTILITY) {
    val copyOnCapture = bool("Copy To Clipboard", default=false)
    val flashEffect   = bool("Flash Effect", default=true)
    val flashColor    = color("Flash Color", r=255, g=255, b=255)
    val flashDuration = slider("Flash Duration", default=0.3f, min=0.1f, max=1f, suffix="s")
    val saveFolder    = bool("Custom Save Folder", default=false)
    val autoName      = bool("Auto Name Files", default=true)
    val nameFormat    = dropdown("Name Format", options=arrayOf("Date_Time", "Counter", "UUID"), default=0)
    val quality       = slider("JPEG Quality", default=95f, min=10f, max=100f, suffix="%")
    val format        = dropdown("Format", options=arrayOf("PNG", "JPEG"), default=0)
    val notification  = bool("Show Notification", default=true)
    init { enable() }
}
