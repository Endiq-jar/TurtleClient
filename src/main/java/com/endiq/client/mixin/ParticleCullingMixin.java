package com.endiq.client.mixin;

import com.endiq.client.compat.CullingHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=26.1 {
/*import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
*///?} else {
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
//?}

//? if >=26.1 {
/*@Mixin(ParticleEngine.class)
*///?} else {
@Mixin(ParticleManager.class)
//?}
public class ParticleCullingMixin {
//? if >=26.1 {
/*    @Inject(method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true)
    private void turtleClient$cullParticle(ParticleOptions effect, double x, double y, double z,
                                           double vx, double vy, double vz, CallbackInfoReturnable<Particle> cir) {
*///?} else {
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true)
    private void turtleClient$cullParticle(ParticleEffect effect, double x, double y, double z,
                                           double vx, double vy, double vz, CallbackInfoReturnable<Particle> cir) {
//?}
        if (CullingHooks.shouldCullParticle(x, y, z)) cir.setReturnValue(null);
    }
}
