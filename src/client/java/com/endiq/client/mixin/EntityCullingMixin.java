package com.endiq.client.mixin;

import com.endiq.client.modules.impl.render.CullingBridge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NOT COMPILE-VERIFIED. Written against remembered 1.21.4 Yarn names; the
 * dev sandbox that produced this can't reach Fabric/Mojang's maven to build
 * against the real mappings. If Mixin can't resolve "render" as unambiguous,
 * or the game crashes on world load, paste the stack trace back -- the fix
 * is almost always the injected method's exact parameter types below.
 *
 * Skips rendering entities beyond entityCullRange, and (if occlusionCullEnabled)
 * further skips entities where a raycast from the camera to every corner of
 * the entity's bounding box is blocked by terrain -- same technique as the
 * standalone "EntityCulling" mod.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityCullingMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void turtleClient$cullEntity(
            Entity entity, double x, double y, double z, float yaw, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            CallbackInfo ci) {

        if (!CullingBridge.getEntityCullingEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (entity == mc.player || entity == mc.cameraEntity) return; // never cull the camera/self

        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null) return;
        Vec3d camPos = camera.getPos();

        double range = CullingBridge.getEntityCullRange();
        double distSq = camPos.squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ());
        if (distSq > range * range) {
            ci.cancel();
            return;
        }

        if (!CullingBridge.getOcclusionCullEnabled()) return;

        Box box = entity.getBoundingBox();
        Vec3d[] targets = new Vec3d[]{
                new Vec3d(box.minX, box.minY, box.minZ),
                new Vec3d(box.maxX, box.minY, box.maxZ),
                new Vec3d(box.minX, box.maxY, box.minZ),
                new Vec3d(box.maxX, box.maxY, box.maxZ),
                new Vec3d((box.minX + box.maxX) / 2.0, (box.minY + box.maxY) / 2.0, (box.minZ + box.maxZ) / 2.0),
        };

        for (Vec3d target : targets) {
            RaycastContext rc = new RaycastContext(
                    camPos, target,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    entity
            );
            HitResult hit = entity.getWorld().raycast(rc);
            if (hit == null || hit.getType() == HitResult.Type.MISS) {
                return; // at least one point is visible -- render normally
            }
        }

        ci.cancel(); // every sampled point was blocked
    }
}
