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

import com.endiq.client.compat.CullingHooks;

//? if >=26.1 {
/*@Mixin(LevelRenderer.class)
*///?} else {
@Mixin(WorldRenderer.class)
//?}
public class ChunkOcclusionCullingMixin {
//? if >=26.1 {
/*    @Inject(method = "setupTerrain", at = @At("RETURN"))
*///?} else if >=1.21.9 {
/*    @Inject(method = "updateCamera", at = @At("RETURN"))
*///?} else {
    @Inject(method = "setupTerrain", at = @At("RETURN"))
//?}
    private void turtleClient$cullChunkSections(CallbackInfo ci) {
        CullingHooks.cullChunkSections(this);
    }
}
