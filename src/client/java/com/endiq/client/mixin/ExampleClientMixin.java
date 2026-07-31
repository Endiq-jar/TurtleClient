package com.endiq.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ExampleClientMixin {

    // ── 1. Replace Mojang loading screen with turtle loading_icon ────
    @Mixin(SplashOverlay.class)
    public static class SplashMixin {
        private static final Identifier LOADING = Identifier.of("turtle-client", "textures/loading_icon.png");

        @Inject(method = "render", at = @At("TAIL"))
        private void onRender(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
            MinecraftClient client = MinecraftClient.getInstance();
            int sw = ctx.getScaledWindowWidth();
            int sh = ctx.getScaledWindowHeight();
            int cx = sw / 2;
            int cy = sh / 2;

            // Cover Mojang screen with dark bg
            ctx.fill(0, 0, sw, sh, 0xFF0A0A0F);

            // Pulse ring
            double t = System.currentTimeMillis() / 1000.0;
            int ring = (int)(72 + Math.sin(t * 2.0) * 4);
            int ra   = Math.min(180, Math.max(0, (int)((Math.sin(t * 2.0) * 0.4 + 0.5) * 140)));
            int teal = (ra << 24) | 0x002EBFA5;
            ctx.fill(cx - ring, cy - ring, cx + ring, cy - ring + 2, teal);
            ctx.fill(cx - ring, cy + ring - 2, cx + ring, cy + ring, teal);
            ctx.fill(cx - ring, cy - ring, cx - ring + 2, cy + ring, teal);
            ctx.fill(cx + ring - 2, cy - ring, cx + ring, cy + ring, teal);

            // Shadow
            ctx.fill(cx - 44, cy - 39, cx + 44, cy + 49, 0x44000000);

            // Turtle loading icon 88x88
            try {
                ctx.drawTexture(LOADING, cx - 44, cy - 44, 0f, 0f, 88, 88, 88, 88);
            } catch (Exception ignored) {}

            // Progress bar
            float progress = (float)((System.currentTimeMillis() % 3000) / 3000.0);
            try {
                var f = SplashOverlay.class.getDeclaredField("reload");
                f.setAccessible(true);
                var reload = f.get(this);
                if (reload != null) {
                    for (var m : reload.getClass().getMethods()) {
                        if (m.getName().equals("getProgress") || m.getName().equals("progress")) {
                            progress = Math.min(1f, (float)m.invoke(reload));
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}

            int barW = 160; int barX = cx - barW / 2; int barY = cy + 58;
            ctx.fill(barX, barY, barX + barW, barY + 4, 0xFF1A1A1A);
            ctx.fill(barX, barY, barX + (int)(barW * progress), barY + 4, 0xFF2EBFA5);

            String txt = "TurtleClient v1.0";
            ctx.drawTextWithShadow(client.textRenderer, txt,
                cx - client.textRenderer.getWidth(txt) / 2, cy + 68, 0xFFFFFFFF);
        }
    }

    // ── 2. Draw turtle_logo bottom-right on every Screen ─────────────
    @Mixin(Screen.class)
    public static class ScreenMixin {
        private static final Identifier LOGO = Identifier.of("turtle-client", "turtle_logo.png");

        @Inject(method = "render", at = @At("TAIL"))
        private void onRender(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
            try {
                int sw = ctx.getScaledWindowWidth();
                int sh = ctx.getScaledWindowHeight();
                // Draw 48x16 turtle logo bottom-right
                ctx.setShaderColor(1f, 1f, 1f, 0.7f);
                ctx.drawTexture(LOGO, sw - 52, sh - 18, 0f, 0f, 48, 16, 48, 16);
                ctx.setShaderColor(1f, 1f, 1f, 1f);
            } catch (Exception ignored) {}
        }

        // Cancel blur for ClickGui and ModSettingsGui
        @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
        private void cancelBlur(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
            String name = ((Screen)(Object)this).getClass().getSimpleName();
            if (name.equals("ClickGui") || name.equals("ModSettingsGui")) {
                ci.cancel();
            }
        }
    }
}
