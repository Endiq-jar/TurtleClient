package com.endiq.client.mixin;

import com.endiq.client.compat.GameplayControls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=26.1 {
/*import net.minecraft.client.Camera;
@Mixin(Camera.class)
*///?} else {
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;
@Mixin(GameRenderer.class)
//?}
public class FovMixin {
//? if >=26.1 {
/*    // Native camera extraction separates world FOV from the hand/HUD FOV.
    @Inject(method="calculateFov",at=@At("RETURN"),cancellable=true)
    private void turtleClient$fov(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue((float)GameplayControls.fov(cir.getReturnValue(),true));
    }
*///?} else {
    @Inject(method="getFov",at=@At("RETURN"),cancellable=true)
    private void turtleClient$fov(Camera camera,float delta,boolean changing,CallbackInfoReturnable<Object> cir) {
        Object original=cir.getReturnValue();
        if(original instanceof Number number) {
            double value=GameplayControls.fov(number.doubleValue(),changing);
            // getFov changed from double to float; preserve the target's boxed primitive.
            if(original instanceof Float)cir.setReturnValue((float)value);
            else cir.setReturnValue(value);
        }
    }
//?}
}
