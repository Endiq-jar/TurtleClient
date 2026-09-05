package com.endiq.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26.1 {
/*import net.minecraft.client.renderer.extract.LevelExtractor;
*///?} else {
import net.minecraft.client.render.WorldRenderer;
//?}

import com.endiq.client.compat.CullingHooks;

//? if >=26.1 {
/*@Mixin(LevelExtractor.class)
*///?} else {
@Mixin(WorldRenderer.class)
//?}
public class ChunkOcclusionCullingMixin {
//? if >=26.1 {
/*    @Inject(method = "extract", at = @At("RETURN"))
*///?} else if >=1.21.9 {
/*    @Inject(method = "updateCamera", at = @At("RETURN"))
*///?} else {
    @Inject(method = "setupTerrain", at = @At("RETURN"))
//?}
    private void turtleClient$cullChunkSections(CallbackInfo ci) {
//? if >=26.1 {
/*        CullingHooks.cullChunkSections(net.minecraft.client.Minecraft.getInstance().levelRenderer);
*///?} else {
        CullingHooks.cullChunkSections(this);
//?}
    }
}
