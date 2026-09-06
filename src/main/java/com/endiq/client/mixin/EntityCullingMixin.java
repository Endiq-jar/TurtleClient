package com.endiq.client.mixin;

import com.endiq.client.compat.CullingHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=26.1 {
/*import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
*///?} else if >=1.21.9 {
/*import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
*///?} else {
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
//?}

// Cull at the visibility decision, before render-state extraction. The old
// render(Entity, ...) hook no longer exists in the render-state pipeline.
//? if >=1.21.9 && <26.1 {
/*@Mixin(EntityRenderManager.class)
*///?} else {
@Mixin(EntityRenderDispatcher.class)
//?}
public class EntityCullingMixin {
    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private void turtleClient$cullEntity(Entity entity, Frustum frustum, double x, double y, double z,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && CullingHooks.shouldCullEntity(entity)) cir.setReturnValue(false);
    }
}
