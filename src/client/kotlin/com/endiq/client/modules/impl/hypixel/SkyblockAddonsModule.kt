package com.endiq.client.modules.impl.hypixel
import com.endiq.client.modules.Module
class SkyblockAddonsModule : Module("Skyblock Addons", "Skyblock helpers", Category.HYPIXEL) {
    val showHealth  = bool("Skill Bar", default=true)
    val showMana    = bool("Mana Display", default=true)
    val showDefense = bool("Defense Display", default=true)
    val farmHelper  = bool("Farming Helper", default=false)
    val miningHelper= bool("Mining Helper", default=false)
    val dungeonHelper = bool("Dungeon Helper", default=false)
    val petDisplay  = bool("Pet Display", default=true)
    val auctionAlerts = bool("Auction Alerts", default=false)
    val bitsAlert   = bool("Bits Cap Alert", default=true)
    val purseDisplay= bool("Purse Display", default=true)
    val skillTracker= bool("Skill XP Tracker", default=false)
    val slayerTracker = bool("Slayer Tracker", default=false)
}
