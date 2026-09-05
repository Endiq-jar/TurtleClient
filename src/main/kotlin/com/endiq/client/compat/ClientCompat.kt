package com.endiq.client.compat

import org.lwjgl.glfw.GLFW

fun identifier(namespace: String, path: String): Identifier =
//? if >=26.1 {
/*    Identifier.fromNamespaceAndPath(namespace, path)
*///?} else if >=1.21 {
    Identifier.of(namespace, path)
//?} else {
/*    Identifier(namespace, path)
*///?}

fun literalText(value: String): Text =
//? if >=1.19 {
    Text.literal(value)
//?} else {
/*    net.minecraft.text.LiteralText(value)
*///?}

fun gameVersion(): String =
//? if >=26.1 {
/*    net.minecraft.SharedConstants.getCurrentVersion().name()
*///?} else if >=1.21.6 {
/*    net.minecraft.SharedConstants.getGameVersion().name()
*///?} else {
    net.minecraft.SharedConstants.getGameVersion().name
//?}

fun MinecraftClient.windowWidth(): Int =
//? if >=26.1 {
/*    window.guiScaledWidth
*///?} else {
    window.scaledWidth
//?}

fun MinecraftClient.windowHeight(): Int =
//? if >=26.1 {
/*    window.guiScaledHeight
*///?} else {
    window.scaledHeight
//?}

fun clientFps(): Int =
//? if >=26.1 {
/*    MinecraftClient.getInstance().fps
*///?} else if >=1.19.4 {
    MinecraftClient.getInstance().currentFps
//?} else {
/*    com.endiq.client.mixin.LegacyFpsAccessor.`turtleClient$getCurrentFps`()
*///?}

fun clientPing(): Int? {
    val client = MinecraftClient.getInstance()
    val uuid = client.player?.uuid ?: return null
//? if >=26.1 {
/*    return client.connection?.getPlayerInfo(uuid)?.latency
*///?} else {
    return client.networkHandler?.getPlayerListEntry(uuid)?.latency
//?}
}

fun serverAddress(): String? =
//? if >=26.1 {
/*    MinecraftClient.getInstance().currentServer?.ip
*///?} else {
    MinecraftClient.getInstance().currentServerEntry?.address
//?}

fun lastResourcePackName(): String =
//? if >=26.1 {
/*    MinecraftClient.getInstance().resourcePackRepository.selectedPacks.lastOrNull()?.title?.string ?: "Default"
*///?} else {
    MinecraftClient.getInstance().resourcePackManager.enabledProfiles.lastOrNull()?.displayName?.string ?: "Default"
//?}

fun effectName(effect: StatusEffectInstance): String =
//? if >=26.1 {
/*    effect.effect.value().displayName.string
*///?} else if >=1.20.5 {
    effect.effectType.value().name.string
//?} else {
/*    effect.effectType.name.string
*///?}

fun armorStacks(player: PlayerEntity): List<ItemStack> =
    listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET).map {
//? if >=26.1 {
/*        player.getItemBySlot(it)
*///?} else {
        player.getEquippedStack(it)
//?}
    }

fun isKeyDown(key: Int): Boolean {
    if (key == GLFW.GLFW_KEY_UNKNOWN) return false
    val client = MinecraftClient.getInstance()
//? if >=26.1 {
/*    return com.mojang.blaze3d.platform.InputConstants.isKeyDown(client.window, key)
*///?} else if >=1.21.9 {
/*    return net.minecraft.client.util.InputUtil.isKeyPressed(client.window, key)
*///?} else {
    return net.minecraft.client.util.InputUtil.isKeyPressed(client.window.handle, key)
//?}
}

// Commands and ordinary chat use different packet paths since signed chat (1.19).
fun sendChatMessage(message: String) {
    val client = MinecraftClient.getInstance()
//? if >=26.1 {
/*    val connection = client.connection ?: return
    if (message.startsWith("/")) connection.sendCommand(message.removePrefix("/"))
    else connection.sendChat(message)
*///?} else if >=1.19 {
    val connection = client.networkHandler ?: return
    if (message.startsWith("/")) connection.sendChatCommand(message.removePrefix("/"))
    else connection.sendChatMessage(message)
//?} else {
/*    client.player?.sendChatMessage(message)
*///?}
}

fun registerGuiKey(): KeyBinding {
//? if >=26.1 {
/*    return net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(
        KeyBinding("key.turtle-client.gui", com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT, net.minecraft.client.KeyMapping.Category.register(identifier("turtle-client", "general"))))
*///?} else if >=1.21.9 {
/*    return net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(
        KeyBinding("key.turtle-client.gui", net.minecraft.client.util.InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT, net.minecraft.client.option.KeyBinding.Category.create(identifier("turtle-client", "general"))))
*///?} else {
    return net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(
        KeyBinding("key.turtle-client.gui", net.minecraft.client.util.InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT, "TurtleClient"))
//?}
}

fun openPath(path: java.io.File) {
//? if >=26.1 {
/*    net.minecraft.util.Util.getPlatform().openFile(path)
*///?} else {
    net.minecraft.util.Util.getOperatingSystem().open(path)
//?}
}

fun openUri(uri: java.net.URI) {
//? if >=26.1 {
/*    net.minecraft.util.Util.getPlatform().openUri(uri)
*///?} else {
    net.minecraft.util.Util.getOperatingSystem().open(uri)
//?}
}

//? if >=26.1 {
/*// Extensions translate the small Yarn-named surface used by the shared modules.
val MinecraftClient.world get() = level
val MinecraftClient.textRenderer get() = font
val MinecraftClient.runDirectory get() = gameDirectory
val MinecraftClient.crosshairTarget get() = hitResult
fun MinecraftClient.setScreen(screen: Screen?) = gui.setScreen(screen)
fun MinecraftClient.scheduleStop() = stop()
fun TextRenderer.getWidth(text: String) = width(text)
fun KeyBinding.wasPressed() = consumeClick()
val KeyBinding.isPressed get() = isDown
val net.minecraft.client.Options.forwardKey get() = keyUp
val net.minecraft.client.Options.backKey get() = keyDown
val net.minecraft.client.Options.leftKey get() = keyLeft
val net.minecraft.client.Options.rightKey get() = keyRight
val net.minecraft.client.Options.jumpKey get() = keyJump
val ClientWorld.entities: Iterable<Entity> get() = entitiesForRendering()
val Entity.uuid: java.util.UUID get() = getUUID()
val Entity.velocity: Vec3d get() = deltaMovement
val Entity.yaw: Float get() = yRot
val Entity.pitch: Float get() = xRot
val Entity.scoreboardTeam get() = team
fun Entity.squaredDistanceTo(other: Entity) = distanceToSqr(other)
fun Vec3d.lengthSquared() = lengthSqr()
val LivingEntity.forwardSpeed: Float get() = zza
val LivingEntity.armor: Int get() = armorValue
val LivingEntity.statusEffects get() = activeEffects
val PlayerEntity.hungerManager get() = foodData
fun PlayerEntity.getAttackCooldownProgress(delta: Float) = getAttackStrengthScale(delta)
val ItemStack.damage: Int get() = damageValue
*///?}

fun playerName(): String =
//? if >=26.1 {
/*    MinecraftClient.getInstance().user.name
*///?} else {
    MinecraftClient.getInstance().session.username
//?}

fun optionsScreen(parent: Screen): Screen {
    val client = MinecraftClient.getInstance()
    //? if >=26.1 {
    /*return OptionsScreen(parent, client.options, client.world != null)
    *///?} else {
    return OptionsScreen(parent, client.options)
    //?}
}

fun isLocalPlayer(entity: Entity): Boolean = entity.uuid == MinecraftClient.getInstance().player?.uuid
