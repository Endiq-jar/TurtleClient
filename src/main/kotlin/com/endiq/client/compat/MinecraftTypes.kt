package com.endiq.client.compat

// Yarn before 26.1, Mojang's published names afterwards. These are compile-time
// aliases, not reflection or substitute Minecraft classes.
//? if >=26.1 {
/*typealias MinecraftClient = net.minecraft.client.Minecraft
typealias Screen = net.minecraft.client.gui.screens.Screen
typealias TextRenderer = net.minecraft.client.gui.Font
typealias Text = net.minecraft.network.chat.Component
typealias Identifier = net.minecraft.resources.Identifier
typealias BlockEntity = net.minecraft.world.level.block.entity.BlockEntity
typealias BuiltChunk = net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection
typealias Entity = net.minecraft.world.entity.Entity
typealias LivingEntity = net.minecraft.world.entity.LivingEntity
typealias PlayerEntity = net.minecraft.world.entity.player.Player
typealias ClientPlayerEntity = net.minecraft.client.player.LocalPlayer
typealias ClientWorld = net.minecraft.client.multiplayer.ClientLevel
typealias BlockPos = net.minecraft.core.BlockPos
typealias Vec3d = net.minecraft.world.phys.Vec3
typealias BlockHitResult = net.minecraft.world.phys.BlockHitResult
typealias EquipmentSlot = net.minecraft.world.entity.EquipmentSlot
typealias ItemStack = net.minecraft.world.item.ItemStack
typealias StatusEffectInstance = net.minecraft.world.effect.MobEffectInstance
typealias KeyBinding = net.minecraft.client.KeyMapping
typealias ActionResult = net.minecraft.world.InteractionResult
typealias MultiplayerScreen = net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
typealias SelectWorldScreen = net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
typealias ControlsOptionsScreen = net.minecraft.client.gui.screens.options.controls.ControlsScreen
typealias OptionsScreen = net.minecraft.client.gui.screens.options.OptionsScreen
*///?} else {
typealias MinecraftClient = net.minecraft.client.MinecraftClient
typealias Screen = net.minecraft.client.gui.screen.Screen
typealias TextRenderer = net.minecraft.client.font.TextRenderer
typealias Text = net.minecraft.text.Text
typealias Identifier = net.minecraft.util.Identifier
typealias BlockEntity = net.minecraft.block.entity.BlockEntity
typealias BuiltChunk = net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk
typealias Entity = net.minecraft.entity.Entity
typealias LivingEntity = net.minecraft.entity.LivingEntity
typealias PlayerEntity = net.minecraft.entity.player.PlayerEntity
typealias ClientPlayerEntity = net.minecraft.client.network.ClientPlayerEntity
typealias ClientWorld = net.minecraft.client.world.ClientWorld
typealias BlockPos = net.minecraft.util.math.BlockPos
typealias Vec3d = net.minecraft.util.math.Vec3d
typealias BlockHitResult = net.minecraft.util.hit.BlockHitResult
typealias EquipmentSlot = net.minecraft.entity.EquipmentSlot
typealias ItemStack = net.minecraft.item.ItemStack
typealias StatusEffectInstance = net.minecraft.entity.effect.StatusEffectInstance
typealias KeyBinding = net.minecraft.client.option.KeyBinding
typealias ActionResult = net.minecraft.util.ActionResult
typealias MultiplayerScreen = net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
typealias SelectWorldScreen = net.minecraft.client.gui.screen.world.SelectWorldScreen
typealias ControlsOptionsScreen = net.minecraft.client.gui.screen.option.ControlsOptionsScreen
typealias OptionsScreen = net.minecraft.client.gui.screen.option.OptionsScreen
//?}

//? if >=26.1 {
/*typealias NativeGuiContext = net.minecraft.client.gui.GuiGraphicsExtractor
*///?} else if >=1.20 {
typealias NativeGuiContext = net.minecraft.client.gui.DrawContext
//?} else {
/*typealias NativeGuiContext = net.minecraft.client.util.math.MatrixStack
*///?}
