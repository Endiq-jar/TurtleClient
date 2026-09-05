package com.endiq.client.mixin;

import com.endiq.client.compat.BrandingRenderer;
import com.endiq.client.compat.ClientScreen;
import com.endiq.client.compat.GuiContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
*///?} else {
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
//?}
//? if >=26.1 {
/*// GuiGraphicsExtractor is imported above.
*///?} else if >=1.20 {
import net.minecraft.client.gui.DrawContext;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}

public class ExampleClientMixin {
//? if >=26.1 {
/*    @Mixin(LoadingOverlay.class)
*///?} else {
    @Mixin(SplashOverlay.class)
//?}
    public static class SplashMixin {
//? if >=26.1 {
/*        @Shadow @Final private ReloadInstance reload;

        @Inject(method = "extractRenderState", at = @At("TAIL"))
        private void turtleClient$renderSplash(GuiGraphicsExtractor nativeCtx, int mx, int my, float delta, CallbackInfo ci) {
            BrandingRenderer.renderSplash(new GuiContext(nativeCtx), reload.getActualProgress());
        }
*///?} else if >=1.20 {
        @Shadow @Final private ResourceReload reload;

        @Inject(method = "render", at = @At("TAIL"))
        private void turtleClient$renderSplash(DrawContext nativeCtx, int mx, int my, float delta, CallbackInfo ci) {
            BrandingRenderer.renderSplash(new GuiContext(nativeCtx), reload.getProgress());
        }
//?} else {
/*        @Shadow @Final private ResourceReload reload;

        @Inject(method = "render", at = @At("TAIL"))
        private void turtleClient$renderSplash(MatrixStack nativeCtx, int mx, int my, float delta, CallbackInfo ci) {
            BrandingRenderer.renderSplash(new GuiContext(nativeCtx), reload.getProgress());
        }
*///?}
    }

    @Mixin(Screen.class)
    public static class ScreenMixin {
//? if >=26.1 {
/*        @Inject(method = "extractRenderState", at = @At("TAIL"))
        private void turtleClient$renderWatermark(GuiGraphicsExtractor nativeCtx, int mx, int my, float delta, CallbackInfo ci) {
*///?} else if >=1.20 {
        @Inject(method = "render", at = @At("TAIL"))
        private void turtleClient$renderWatermark(DrawContext nativeCtx, int mx, int my, float delta, CallbackInfo ci) {
//?} else {
/*        @Inject(method = "render", at = @At("TAIL"))
        private void turtleClient$renderWatermark(MatrixStack nativeCtx, int mx, int my, float delta, CallbackInfo ci) {
*///?}
            BrandingRenderer.renderWatermark(new GuiContext(nativeCtx));
        }

        // These screens draw their own background; don't overlay vanilla blur.
//? if >=26.1 {
/*        @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
*///?} else {
        @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
//?}
        private void turtleClient$cancelBackground(CallbackInfo ci) {
            if ((Object) this instanceof ClientScreen) ci.cancel();
        }
    }
}
