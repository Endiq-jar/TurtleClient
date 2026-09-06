package com.endiq.client.mixin;

import com.endiq.client.hud.CrosshairRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26.1 {
/*import net.minecraft.client.gui.Hud;
@Mixin(Hud.class)
*///?} else {
import net.minecraft.client.gui.hud.InGameHud;
@Mixin(InGameHud.class)
//?}
public class CrosshairMixin {
//? if >=26.1 {
/*    @Inject(method="extractCrosshair",at=@At("HEAD"),cancellable=true)
*///?} else {
    @Inject(method="renderCrosshair",at=@At("HEAD"),cancellable=true)
//?}
    private void turtleClient$hideVanilla(CallbackInfo ci) { if(CrosshairRenderer.replacesVanilla())ci.cancel(); }
}
