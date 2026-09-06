package com.endiq.client.mixin;

import com.endiq.client.compat.CullingHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=26.1 {
/*import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
*///?} else {
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Vec3d;
//?}

// This default visibility method survives both immediate and queued rendering.
@Mixin(BlockEntityRenderer.class)
public interface BlockEntityCullingMixin {
//? if >=26.1 {
/*    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private void turtleClient$cullBlockEntity(BlockEntity entity, Vec3 camera, CallbackInfoReturnable<Boolean> cir) {
*///?} else {
    @Inject(method = "isInRenderDistance", at = @At("RETURN"), cancellable = true)
    private void turtleClient$cullBlockEntity(BlockEntity entity, Vec3d camera, CallbackInfoReturnable<Boolean> cir) {
//?}
        if (cir.getReturnValueZ() && CullingHooks.shouldCullBlockEntity(entity, camera)) cir.setReturnValue(false);
    }
}
