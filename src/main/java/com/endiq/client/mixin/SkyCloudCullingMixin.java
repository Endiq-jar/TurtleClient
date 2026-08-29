package com.endiq.client.mixin;

import com.endiq.client.modules.impl.render.CullingBridge;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NOT COMPILE-VERIFIED, see EntityCullingMixin's note -- this one especially:
 * renderSky/renderClouds parameter lists have moved around across versions
 * (matrices+projection+tickDelta+camera+thickFog+fogCallback is the 1.21.4
 * Yarn shape this was written against) and this is exactly the kind of
 * signature that split further around 1.21.5's RenderPipelines change. If
 * Mixin can't resolve either method as unambiguous, dump
 * WorldRenderer.class's declared methods and paste them back -- don't guess
 * a new descriptor twice in a row.
 *
 * On/off only (no distance concept for a full-screen sky/cloud draw). Sky
 * and clouds are pure background dressing -- skipping them costs no
 * gameplay information, same "hide earlier, reveal nothing" rule as
 * EntityCullingMixin.
 */
@Mixin(WorldRenderer.class)
public class SkyCloudCullingMixin {

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void turtleClient$cullSky(CallbackInfo ci) {
        if (CullingBridge.getSkyCullingEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void turtleClient$cullClouds(CallbackInfo ci) {
        if (CullingBridge.getCloudCullingEnabled()) {
            ci.cancel();
        }
    }
}
