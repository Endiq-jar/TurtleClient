package com.endiq.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class NametagMixin {

    private static final Identifier BADGE = Identifier.of("turtle-client", "textures/loading_icon.png");

    @Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
    private void onRenderLabel(Entity entity, Text text,
                               MatrixStack matrices,
                               VertexConsumerProvider vcp,
                               int light, float tickDelta,
                               CallbackInfo ci) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;
            // Only add badge above local player's nametag
            if (!entity.getUuid().equals(mc.player.getUuid())) return;

            TextRenderer tr = mc.textRenderer;
            float nameWidth = tr.getWidth(text.getString());

            matrices.push();
            // Position: slightly above where the nametag text sits
            matrices.translate(0.0, 0.35, 0.0);
            matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
            matrices.scale(-0.025f, -0.025f, 0.025f);

            Matrix4f mat = matrices.peek().getPositionMatrix();

            // Badge: 10x10 pixels in nametag space, centred on name
            float bw = 8f; float bh = 8f;
            float bx = -nameWidth / 2f - bw - 2f; // left of name
            float by = -bh / 2f;

            var vc = vcp.getBuffer(
                net.minecraft.client.render.RenderLayer.getEntityTranslucent(BADGE));

            // Simple quad for the badge icon
            vc.vertex(mat, bx,      by,      0).color(255,255,255,220).texture(0f,0f).light(light);
            vc.vertex(mat, bx,      by + bh, 0).color(255,255,255,220).texture(0f,1f).light(light);
            vc.vertex(mat, bx + bw, by + bh, 0).color(255,255,255,220).texture(1f,1f).light(light);
            vc.vertex(mat, bx + bw, by,      0).color(255,255,255,220).texture(1f,0f).light(light);

            matrices.pop();
        } catch (Exception ignored) {}
    }
}
