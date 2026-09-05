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
//? if >=26.1 {
/*    @Inject(method = "addSkyPass", at = @At("HEAD"), cancellable = true)
*///?} else {
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
//?}
    private void turtleClient$cullSky(CallbackInfo ci) {
        if (CullingBridge.getSkyCullingEnabled()) ci.cancel();
    }

//? if >=26.1 {
/*    @Inject(method = "addCloudsPass", at = @At("HEAD"), cancellable = true)
*///?} else {
    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
//?}
    private void turtleClient$cullClouds(CallbackInfo ci) {
        if (CullingBridge.getCloudCullingEnabled()) ci.cancel();
    }
}
