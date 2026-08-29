package com.endiq.client.mixin;

import com.endiq.client.modules.impl.render.CullingBridge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
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
 * Skips rendering entities beyond entityCullRange (or playerCullRange for
 * other players in multiplayer -- see CullingBridge), and (if
 * occlusionCullEnabled) further skips entities where a raycast from the
 * camera to every corner of the entity's bounding box is blocked by terrain
 * -- same technique as the standalone "EntityCulling" mod. Purely a distance/
 * visibility optimization: it never reveals anything the vanilla client
 * wouldn't already know about (no wallhack/ESP behavior), it only hides
 * things earlier.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityCullingMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void turtleClient$cullEntity(
            Entity entity, double x, double y, double z, float yaw, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            CallbackInfo ci) {

        MinecraftClient mc = MinecraftClient.getInstance();
        if (entity == mc.player || entity == mc.cameraEntity) return; // never cull the camera/self

        // Other players (remote, in multiplayer) get their own toggle+range
        // instead of the generic entity settings -- see CullingBridge for
        // why. AbstractClientPlayerEntity covers every non-local player the
        // client renders (survival, spectator-visible, etc.).
        boolean isOtherPlayer = entity instanceof AbstractClientPlayerEntity;
        boolean cullingEnabled = isOtherPlayer
                ? CullingBridge.getPlayerCullingEnabled()
                : CullingBridge.getEntityCullingEnabled();
        if (!cullingEnabled) return;

        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null) return;
        Vec3d camPos = camera.getPos();

        double range = isOtherPlayer ? CullingBridge.getPlayerCullRange() : CullingBridge.getEntityCullRange();
        double distSq = camPos.squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ());
        if (distSq > range * range) {
            ci.cancel();
            return;
        }

        if (!CullingBridge.getOcclusionCullEnabled()) return;

        // 3 sample points, not 5 -- fewer RaycastContext allocations per entity
        // per frame while still catching "fully behind a wall" cases. Two
        // opposite corners plus center is enough to avoid false-cancels on
        // partially-visible entities without doubling the raycast cost.
        Box box = entity.getBoundingBox();
        if (turtleClient$rayBlocked(camPos, new Vec3d(box.minX, box.minY, box.minZ), entity)
                && turtleClient$rayBlocked(camPos, new Vec3d(box.maxX, box.maxY, box.maxZ), entity)
                && turtleClient$rayBlocked(camPos, box.getCenter(), entity)) {
            ci.cancel();
        }
    }

    private boolean turtleClient$rayBlocked(Vec3d from, Vec3d to, Entity entity) {
        RaycastContext rc = new RaycastContext(
                from, to,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
        );
        HitResult hit = entity.getWorld().raycast(rc);
        return hit != null && hit.getType() != HitResult.Type.MISS;
    }
}
