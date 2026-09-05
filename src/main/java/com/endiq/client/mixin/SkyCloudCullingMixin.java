package com.endiq.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26.1 {
/*import net.minecraft.client.renderer.LevelRenderer;
*///?} else {
import net.minecraft.client.render.WorldRenderer;
//?}

import com.endiq.client.modules.impl.render.CullingBridge;

//? if >=26.1 {
/*@Mixin(LevelRenderer.class)
*///?} else {
@Mixin(WorldRenderer.class)
//?}
public class SkyCloudCullingMixin {
    // Older WorldRenderer versions also use these names for mesh builders.
    // Target only the render passes, never the static/return-value overloads.
//? if >=26.1 {
/*    @Inject(method = "addSkyPass", at = @At("HEAD"), cancellable = true)
*///?} else if >=1.21 {
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
//?} else if >=1.20.5 {
/*    @Inject(method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
*///?} else if >=1.19.3 {
/*    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
*///?} else {
/*    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
*///?}
    private void turtleClient$cullSky(CallbackInfo ci) {
        if (CullingBridge.getSkyCullingEnabled()) ci.cancel();
    }

//? if >=26.1 {
/*    @Inject(method = "addCloudsPass", at = @At("HEAD"), cancellable = true)
*///?} else if >=1.21 {
    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
//?} else if >=1.20.5 {
/*    @Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FDDD)V", at = @At("HEAD"), cancellable = true)
*///?} else if >=1.19.3 {
/*    @Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FDDD)V", at = @At("HEAD"), cancellable = true)
*///?} else {
/*    @Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FDDD)V", at = @At("HEAD"), cancellable = true)
*///?}
    private void turtleClient$cullClouds(CallbackInfo ci) {
        if (CullingBridge.getCloudCullingEnabled()) ci.cancel();
    }
}
