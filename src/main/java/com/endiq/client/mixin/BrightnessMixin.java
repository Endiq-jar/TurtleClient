package com.endiq.client.mixin;

import com.endiq.client.compat.GameplayControls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
//? if >=26.1 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.LightTexture;
@Mixin(LightTexture.class)
*///?} else {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
//? if >=1.19 {
import net.minecraft.client.option.SimpleOption;
//?} else {
/*import net.minecraft.client.option.GameOptions;
*///?}
@Mixin(LightmapTextureManager.class)
//?}
public class BrightnessMixin {
//? if >=26.1 {
/*    @Redirect(method="updateLightTexture",at=@At(value="INVOKE",target="Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    private Object turtleClient$brightness(OptionInstance<?> option) {
        return option==Minecraft.getInstance().options.gamma() ? GameplayControls.gamma((Double)option.get()) : option.get();
    }
*///?} else if >=1.19 {
    @Redirect(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"))
    private Object turtleClient$brightness(SimpleOption<?> option) {
        return option==MinecraftClient.getInstance().options.getGamma() ? GameplayControls.gamma((Double)option.getValue()) : option.getValue();
    }
//?} else {
/*    @Redirect(method="update",at=@At(value="FIELD",target="Lnet/minecraft/client/option/GameOptions;gamma:D"))
    private double turtleClient$brightness(GameOptions options) { return GameplayControls.gamma(options.gamma); }
*///?}
}
