package com.endiq.client.mixin;

import com.endiq.client.modules.impl.render.CullingBridge;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NOT COMPILE-VERIFIED, see EntityCullingMixin's note.
 *
 * Distance-only cull for block entity rendering (chests, signs, banners,
 * skulls, beds, etc.) -- these covers the "blocks, signs, chest" part of the
 * culling request. No occlusion raycast here: block entities are cheap
 * enough individually that a distance cap covers the real cost (large chest
 * rooms, sign farms, banner walls).
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityCullingMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void turtleClient$cullBlockEntity(
            BlockEntity blockEntity, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, CallbackInfo ci) {

        if (!CullingBridge.getBlockEntityCullingEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null) return;

        BlockPos pos = blockEntity.getPos();
        Vec3d camPos = camera.getPos();
        double range = CullingBridge.getBlockEntityCullRange();
        double distSq = camPos.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (distSq > range * range) {
            ci.cancel();
        }
    }
}
