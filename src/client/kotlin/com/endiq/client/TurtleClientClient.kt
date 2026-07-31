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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.ActionResult
import org.lwjgl.glfw.GLFW

object TurtleClientClient : ClientModInitializer {

    override fun onInitializeClient() {
        ModuleManager.init()
        HudRenderCallback.EVENT.register(HudRenderer::onHudRender)

        val guiKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding("key.turtle-client.gui", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, "TurtleClient")
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            ModuleManager.modules.forEach { mod ->
                if (mod.key != GLFW.GLFW_KEY_UNKNOWN &&
                    InputUtil.isKeyPressed(client.window.handle, mod.key)) {
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

        ClientReceiveMessageEvents.GAME.register { msg, _ ->
            val m = ModuleManager.getByName("Hypixel Addons") as? HypixelAddonsModule
            if (m?.enabled == true) {
                val t = msg.string
                if (t.contains("Game Over") || t.contains("Winner") || t.contains("Game ended"))
                    MinecraftClient.getInstance().player?.networkHandler?.sendChatMessage("/gg")
            }
        }
    }
}
