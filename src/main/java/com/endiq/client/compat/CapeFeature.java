package com.endiq.client.compat;

import com.endiq.client.capes.CapeManager;
import com.endiq.client.capes.CapeMesh;
//? if >=26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
*///?} else {
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
//? if >=1.19.3 {
import net.minecraft.util.math.RotationAxis;
//?} else {
/*import net.minecraft.util.math.Vec3f;
*///?}
//? if >=1.21.9 {
/*import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.RenderLayers;
*///?} else {
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
//?}
//? if >=1.21.2 {
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
//?} else {
/*import net.minecraft.client.network.AbstractClientPlayerEntity;
*///?}
//?}

/**
 * The one client-side render layer this mod adds: the equipped cape, drawn on the
 * local player only. Capes come from CapeManager, which downloads and caches them.
 */
//? if >=26.1 {
/*public final class CapeFeature extends RenderLayer<AvatarRenderState, PlayerModel> {
    public CapeFeature(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }
    public static void register() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof AvatarRenderer avatar) helper.register(new CapeFeature(avatar));
        });
    }
    @Override public void submit(PoseStack matrices, SubmitNodeCollector queue, int light, AvatarRenderState state, float a, float b) {
        CapeManager.Outfit outfit = ((CapeRenderState)state).turtleClient$getCape();
        CapeManager.Cape cape = outfit.getCape();
        if (cape == null) return;
        var model = getParentModel();
        model.setupAnim(state);
*///?} else if >=1.21.2 {
public final class CapeFeature extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    public CapeFeature(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> parent) { super(parent); }
    public static void register() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof PlayerEntityRenderer player) helper.register(new CapeFeature(player));
        });
    }
//? if >=1.21.9 {
/*    @Override public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float a, float b) {
*///?} else {
    @Override public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, PlayerEntityRenderState state, float a, float b) {
//?}
        CapeManager.Outfit outfit = ((CapeRenderState)state).turtleClient$getCape();
        CapeManager.Cape cape = outfit.getCape();
        if (cape == null) return;
        var model = getContextModel();
        model.setAngles(state);
//?} else {
/*public final class CapeFeature extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public CapeFeature(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> parent) { super(parent); }
    public static void register() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof PlayerEntityRenderer player) helper.register(new CapeFeature(player));
        });
    }
    @Override public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, AbstractClientPlayerEntity player,
                                 float a, float b, float delta, float age, float headYaw, float headPitch) {
        CapeManager.Outfit outfit = CapeManager.capture(player, delta);
        CapeManager.Cape cape = outfit.getCape();
        if (cape == null) return;
        var model = getContextModel();
*///?}
        float angle = 8f + (float)Math.sin(outfit.getAge() * .08f) * 4f;
//? if >=26.1 {
/*        matrices.pushPose();
*///?} else {
        matrices.push();
//?}
        try {
//? if >=26.1 {
/*            model.body.translateAndRotate(matrices);
            matrices.mulPose(Axis.XP.rotationDegrees(angle));
*///?} else if >=1.21.9 {
/*            model.body.applyTransform(matrices);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle));
*///?} else if >=1.19.3 {
            model.body.rotate(matrices);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle));
//?} else {
/*            model.body.rotate(matrices);
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(angle));
*///?}
            matrices.scale(1/16f,1/16f,1/16f);
//? if >=26.1 {
/*            queue.submitCustomGeometry(matrices,RenderTypes.entityTranslucent(cape.getTexture()),
                (pose,vertices)->CapeGeometry.draw(pose,vertices,CapeMesh.INSTANCE.getVertices(),light));
*///?} else if >=1.21.9 {
/*            queue.submitCustom(matrices,RenderLayers.entityTranslucent(cape.getTexture()),
                (pose,vertices)->CapeGeometry.draw(pose,vertices,CapeMesh.INSTANCE.getVertices(),light));
*///?} else {
            CapeGeometry.draw(matrices.peek(),consumers.getBuffer(RenderLayer.getEntityTranslucent(cape.getTexture())),
                CapeMesh.INSTANCE.getVertices(),light);
//?}
        } finally {
//? if >=26.1 {
/*            matrices.popPose();
*///?} else {
            matrices.pop();
//?}
        }
    }
}
