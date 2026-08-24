package com.endiq.client.modules.impl.hypixel
import com.endiq.client.modules.Module
class HypixelAddonsModule : Module("Hypixel Addons", "Auto /gg and alerts", Category.HYPIXEL) {
    val autoGg      = bool("Auto /gg", default=true)
    val autoGl      = bool("Auto /gl", default=false)
    val autoTip     = bool("Auto /tip all", default=false)
    val levelAlert  = bool("Level Up Alert", default=true)
    val questAlert  = bool("Quest Complete Alert", default=true)
    val friendJoin  = bool("Friend Join Alert", default=true)
    val guildJoin   = bool("Guild Member Join", default=false)
    val lobbyInfo   = bool("Show Lobby Info", default=true)
    val antiGkick   = bool("Anti Guild Kick", default=false)
    val pingAnnounce= bool("Ping On Mention", default=false)
    val ggDelay     = slider("GG Delay", default=1f, min=0f, max=5f, suffix="s")
    init { enable() }
}
