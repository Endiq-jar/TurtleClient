package com.endiq.client.compat

object CosmeticTextures {
    fun upload(id: Identifier, bytes: ByteArray) {
//? if >=26.1 {
/*        val image = com.mojang.blaze3d.platform.NativeImage.read(bytes.inputStream())
        val texture = net.minecraft.client.renderer.texture.DynamicTexture({ "Turtle cosmetic" }, image)
        MinecraftClient.getInstance().textureManager.register(id, texture)
*///?} else if >=1.21.5 {
/*        val image = net.minecraft.client.texture.NativeImage.read(bytes.inputStream())
        val texture = net.minecraft.client.texture.NativeImageBackedTexture({ "Turtle cosmetic" }, image)
        MinecraftClient.getInstance().textureManager.registerTexture(id, texture)
*///?} else {
        val image = net.minecraft.client.texture.NativeImage.read(bytes.inputStream())
        val texture = net.minecraft.client.texture.NativeImageBackedTexture(image)
        MinecraftClient.getInstance().textureManager.registerTexture(id, texture)
//?}
    }
    fun release(id: Identifier) {
//? if >=26.1 {
/*        MinecraftClient.getInstance().textureManager.release(id)
*///?} else {
        MinecraftClient.getInstance().textureManager.destroyTexture(id)
//?}
    }
}
