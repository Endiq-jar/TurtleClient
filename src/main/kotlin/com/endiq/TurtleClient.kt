package com.endiq

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object TurtleClient : ModInitializer {
    const val MOD_ID = "turtle-client"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        LOGGER.info("TurtleClient initializing...")
    }
}
