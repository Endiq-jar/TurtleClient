package com.endiq.client.mixin;

import com.endiq.client.compat.GameplayControls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26.1 {
/*import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
@Mixin(MouseHandler.class)
*///?} else {
import net.minecraft.client.Mouse;
//? if >=1.21.9 {
/*import net.minecraft.client.input.MouseInput;
*///?}
@Mixin(Mouse.class)
//?}
public class GameplayMouseMixin {
//? if >=26.1 {
/*    @Inject(method="onButton",at=@At("HEAD"))
    private void turtleClient$click(long window,MouseButtonInfo input,int action,CallbackInfo ci) { GameplayControls.click(input.button(),action); }
    @Inject(method="onScroll",at=@At("HEAD"),cancellable=true)
*///?} else if >=1.21.9 {
/*    @Inject(method="onMouseButton",at=@At("HEAD"))
    private void turtleClient$click(long window,MouseInput input,int action,CallbackInfo ci) { GameplayControls.click(input.button(),action); }
    @Inject(method="onMouseScroll",at=@At("HEAD"),cancellable=true)
*///?} else {
    @Inject(method="onMouseButton",at=@At("HEAD"))
    private void turtleClient$click(long window,int button,int action,int modifiers,CallbackInfo ci) { GameplayControls.click(button,action); }
    @Inject(method="onMouseScroll",at=@At("HEAD"),cancellable=true)
//?}
    private void turtleClient$zoomScroll(long window,double horizontal,double vertical,CallbackInfo ci) {
        if(GameplayControls.scroll(vertical))ci.cancel();
    }
}
