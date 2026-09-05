package com.endiq.client

import com.endiq.client.gui.ClickGui
import com.endiq.client.hud.HudRenderer
import com.endiq.client.modules.ModuleManager
import com.endiq.client.modules.impl.utility.AutoTextModule
import com.endiq.client.modules.impl.hypixel.HypixelAddonsModule
import com.endiq.client.modules.impl.pvp.ComboCounterModule
import com.endiq.client.modules.impl.pvp.HitColorModule
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
//? if >=1.19 {
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
//?}
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
//? if >=1.21.8 {
/*import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
*///?} else {
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
//?}
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import com.endiq.client.compat.*


object TurtleClientClient : ClientModInitializer {

    override fun onInitializeClient() {
        ModuleManager.init()

        // The callback's second argument changed from tick delta to a tracker;
        // the HUD does not consume it, so adapt only the native drawing context.
        //? if >=1.21.8 {
        /*HudElementRegistry.addLast(identifier("turtle-client", "hud")) { ctx, _ ->
            HudRenderer.onHudRender(GuiContext(ctx))
        }
        *///?} else {
        HudRenderCallback.EVENT.register { ctx, _ -> HudRenderer.onHudRender(GuiContext(ctx)) }
        //?}

        val guiKey = registerGuiKey()

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            ModuleManager.modules.forEach { mod ->
                val keyDown = isKeyDown(mod.key)
                if (keyDown) {
                    if (!mod.keyWasDown) { mod.toggle(); mod.keyWasDown = true }
                } else {
                    mod.keyWasDown = false
                }
            }
            while (guiKey.wasPressed()) client.setScreen(ClickGui())
            HudRenderer.onTick()
        }

        // Local attack detection -- drives Combo Counter and Hit Color flash.
        // Fires client-side when the player swings on an entity; not authoritative
        // damage confirmation, but the same approximation other PvP clients use for HUD feedback.
        AttackEntityCallback.EVENT.register { player, _world, _hand, entity, _hitResult ->
            val combo = ModuleManager.getByName("Combo Counter") as? ComboCounterModule
            if (combo?.enabled == true) combo.registerHit(entity.uuid)

            val hitColor = ModuleManager.getByName("Hit Color") as? HitColorModule
            if (hitColor?.enabled == true) hitColor.trigger()

            ActionResult.PASS
        }

        ClientPlayConnectionEvents.JOIN.register { _handler, _sender, _client ->
            val m = ModuleManager.getByName("Auto Text") as? AutoTextModule
            m?.let { it.sent = false; it.pendingSend = true }
        }

        // 1.18.2 predates Fabric's receive-message event. LegacyChatMixin
        // forwards its game messages to the same handler instead.
        //? if >=1.19 {
        ClientReceiveMessageEvents.GAME.register { msg, _ -> onGameMessage(msg.string) }
        //?}
    }

    @JvmStatic
    fun onGameMessage(message: String) {
        val module = ModuleManager.getByName("Hypixel Addons") as? HypixelAddonsModule
        if (module?.enabled == true && module.autoGg.value &&
            (message.contains("Game Over") || message.contains("Winner") || message.contains("Game ended"))) {
            sendChatMessage("/gg")
        }
    }
}
