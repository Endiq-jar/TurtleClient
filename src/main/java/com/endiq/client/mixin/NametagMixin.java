package com.endiq.client.mixin;

import com.endiq.client.compat.BadgeGeometry;
import com.endiq.client.compat.BadgeRenderState;
import com.endiq.client.compat.ClientCompatKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
*///?} else {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
//?}
//? if >=26.1 {
/*// Queued Mojang renderer types are imported above.
*///?} else if >=1.21.9 {
/*import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.RenderLayers;
*///?} else if >=1.21.2 {
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
//?} else {
/*import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
*///?}

@Mixin(EntityRenderer.class)
public class NametagMixin {
    @Unique private static final Identifier turtleClient$badge = ClientCompatKt.identifier("turtle-client", "textures/loading_icon.png");

//? if >=26.1 {
/*    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void turtleClient$badgeState(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        ((BadgeRenderState) state).turtleClient$setBadge(ClientCompatKt.isLocalPlayer(entity));
    }

    // Select the four-argument entrypoint, not its offset-taking overload.
    @Inject(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
        + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;"
        + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"))
    private void turtleClient$renderBadge(EntityRenderState state, PoseStack matrices,
                                         SubmitNodeCollector queue, CameraRenderState camera, CallbackInfo ci) {
        if (!((BadgeRenderState) state).turtleClient$hasBadge() || state.nameTag == null || state.nameTagAttachment == null) return;
        float left = -Minecraft.getInstance().font.width(state.nameTag) / 2f - 10f;
        int light = state.lightCoords;
        matrices.pushPose();
        try {
            matrices.translate(state.nameTagAttachment.x, state.nameTagAttachment.y + 0.35, state.nameTagAttachment.z);
            matrices.mulPose(camera.orientation);
            matrices.scale(-0.025f, -0.025f, 0.025f);
            queue.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(turtleClient$badge),
                (pose, vertices) -> BadgeGeometry.draw(pose, vertices, left, light));
        } finally {
            matrices.popPose();
        }
    }
*///?} else if >=1.21.9 {
/*    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void turtleClient$badgeState(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        ((BadgeRenderState) state).turtleClient$setBadge(ClientCompatKt.isLocalPlayer(entity));
    }

    @Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
    private void turtleClient$renderBadge(EntityRenderState state, MatrixStack matrices,
                                         OrderedRenderCommandQueue queue, CameraRenderState camera, CallbackInfo ci) {
        if (!((BadgeRenderState) state).turtleClient$hasBadge() || state.displayName == null || state.nameLabelPos == null) return;
        float left = -MinecraftClient.getInstance().textRenderer.getWidth(state.displayName) / 2f - 10f;
        int light = state.light;
        matrices.push();
        try {
            matrices.translate(state.nameLabelPos.x, state.nameLabelPos.y + 0.35, state.nameLabelPos.z);
            matrices.multiply(camera.orientation);
            matrices.scale(-0.025f, -0.025f, 0.025f);
            queue.submitCustom(matrices, RenderLayers.entityTranslucent(turtleClient$badge),
                (pose, vertices) -> BadgeGeometry.draw(pose, vertices, left, light));
        } finally {
            matrices.pop();
        }
    }
*///?} else if >=1.21.2 {
    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void turtleClient$badgeState(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        ((BadgeRenderState) state).turtleClient$setBadge(ClientCompatKt.isLocalPlayer(entity));
    }

    @Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
    private void turtleClient$renderBadge(EntityRenderState state, Text text, MatrixStack matrices,
                                         VertexConsumerProvider consumers, int light, CallbackInfo ci) {
        if (!((BadgeRenderState) state).turtleClient$hasBadge() || state.nameLabelPos == null) return;
        matrices.push();
        try {
            matrices.translate(state.nameLabelPos.x, state.nameLabelPos.y + 0.35, state.nameLabelPos.z);
            matrices.multiply(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
            matrices.scale(-0.025f, -0.025f, 0.025f);
            BadgeGeometry.draw(matrices.peek(), consumers.getBuffer(RenderLayer.getEntityTranslucent(turtleClient$badge)),
                -MinecraftClient.getInstance().textRenderer.getWidth(text) / 2f - 10f, light);
        } finally {
            matrices.pop();
        }
    }
//?} else if >=1.20.5 {
/*    @Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
    private void turtleClient$renderBadge(Entity entity, Text text, MatrixStack matrices,
                                         VertexConsumerProvider consumers, int light, float tickDelta, CallbackInfo ci) {
        turtleClient$drawLegacy(entity, text, matrices, consumers, light);
    }
*///?} else {
/*    @Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
    private void turtleClient$renderBadge(Entity entity, Text text, MatrixStack matrices,
                                         VertexConsumerProvider consumers, int light, CallbackInfo ci) {
        turtleClient$drawLegacy(entity, text, matrices, consumers, light);
    }
*///?}

//? if <1.21.2 {
/*    @Unique
    private void turtleClient$drawLegacy(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        if (!ClientCompatKt.isLocalPlayer(entity)) return;
        matrices.push();
        try {
            matrices.translate(0, entity.getHeight() + 0.85, 0);
            matrices.multiply(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
            matrices.scale(-0.025f, -0.025f, 0.025f);
            BadgeGeometry.draw(matrices.peek(), consumers.getBuffer(RenderLayer.getEntityTranslucent(turtleClient$badge)),
                -MinecraftClient.getInstance().textRenderer.getWidth(text) / 2f - 10f, light);
        } finally {
            matrices.pop();
        }
    }
*///?}
}
