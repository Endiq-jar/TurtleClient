package com.endiq.client.mixin;

import com.endiq.client.compat.BrandingRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1 {
/*import net.minecraft.client.gui.screens.TitleScreen;
*///?} else {
import net.minecraft.client.gui.screen.TitleScreen;
//?}

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void turtleClient$replaceTitleScreen(CallbackInfo ci) {
        BrandingRenderer.showTitleScreen();
        ci.cancel();
    }
}
