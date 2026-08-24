package com.endiq.client.mixin;

import com.endiq.client.modules.impl.render.CullingBridge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NOT COMPILE-VERIFIED, see EntityCullingMixin's note. The addParticle
 * overload/parameter order is the part most likely to need adjusting --
 * there are a few overloads in this class.
 *
 * Distance-only cull for particle spawns. Doesn't cap total active particle
 * count (would need a safe removal hook to track that without leaking
 * memory, which isn't wired up here) -- just skips spawning particles that
 * start out too far from the camera to matter.
 */
@Mixin(ParticleManager.class)
public class ParticleCullingMixin {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true)
    private void turtleClient$cullParticle(
            ParticleEffect effect, double x, double y, double z,
            double vx, double vy, double vz,
            CallbackInfoReturnable<Particle> cir) {

        if (!CullingBridge.getParticleCullingEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null) return;

        Vec3d camPos = camera.getPos();
        double range = CullingBridge.getParticleCullRange();
        double distSq = camPos.squaredDistanceTo(x, y, z);

        if (distSq > range * range) {
            cir.setReturnValue(null);
        }
    }
}
