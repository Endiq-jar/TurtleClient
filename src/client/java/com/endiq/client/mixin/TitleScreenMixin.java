package com.endiq.client.mixin;

import com.endiq.client.gui.CustomTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Whenever vanilla is about to set up a TitleScreen (game launch, disconnect,
// "Back to menu" from Realms, etc.), swap it out for our custom one instead.
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void turtle$replaceWithCustomMenu(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof CustomTitleScreen)) {
            client.setScreen(new CustomTitleScreen());
        }
        ci.cancel();
    }
}
