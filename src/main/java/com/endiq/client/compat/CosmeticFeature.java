package com.endiq.client.compat;

import com.endiq.client.cosmetics.CosmeticManager;
import com.endiq.client.cosmetics.CosmeticMeshes;
//? if >=26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
*///?} else {
import net.minecraft.client.model.ModelPart;
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

//? if >=26.1 {
/*public final class CosmeticFeature extends RenderLayer<AvatarRenderState, PlayerModel> {
    public CosmeticFeature(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }
    public static void register() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof AvatarRenderer avatar) helper.register(new CosmeticFeature(avatar));
        });
    }
    @Override public void submit(PoseStack matrices, SubmitNodeCollector queue, int light, AvatarRenderState state, float a, float b) {
        CosmeticManager.Loadout outfit = ((CosmeticRenderState)state).turtleClient$getCosmetics();
        var model = getParentModel();
        if (outfit.getItems().isEmpty()) return;
        model.setupAnim(state);
*///?} else if >=1.21.2 {
public final class CosmeticFeature extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    public CosmeticFeature(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> parent) { super(parent); }
    public static void register() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof PlayerEntityRenderer player) helper.register(new CosmeticFeature(player));
        });
    }
//? if >=1.21.9 {
/*    @Override public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float a, float b) {
*///?} else {
    @Override public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, PlayerEntityRenderState state, float a, float b) {
//?}
        CosmeticManager.Loadout outfit = ((CosmeticRenderState)state).turtleClient$getCosmetics();
        var model = getContextModel();
        if (outfit.getItems().isEmpty()) return;
        model.setAngles(state);
//?} else {
/*public final class CosmeticFeature extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public CosmeticFeature(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> parent) { super(parent); }
    public static void register() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof PlayerEntityRenderer player) helper.register(new CosmeticFeature(player));
        });
    }
    @Override public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, AbstractClientPlayerEntity player,
                                 float a, float b, float delta, float age, float headYaw, float headPitch) {
        CosmeticManager.Loadout outfit = CosmeticManager.capture(player, delta);
        var model = getContextModel();
*///?}
        for (CosmeticManager.CosmeticEntry item : outfit.getItems()) {
            for (CosmeticMeshes.Part part : CosmeticMeshes.INSTANCE.getModels().get(item.getType())) {
//? if >=26.1 {
/*                matrices.pushPose();
*///?} else {
                matrices.push();
//?}
                try {
                    ModelPart bone = switch (part.getAnchor()) {
                        case HEAD -> model.head;
                        case BODY -> model.body;
                        case LEFT_ARM -> model.leftArm;
                        case RIGHT_ARM -> model.rightArm;
                        case LEFT_LEG -> model.leftLeg;
                        case RIGHT_LEG -> model.rightLeg;
                        case ROOT -> null;
                    };
//? if >=26.1 {
/*                    if (bone != null) bone.translateAndRotate(matrices);
*///?} else {
                    if (bone != null) bone.rotate(matrices);
//?}
                    float angle = 0;
                    boolean wing = part.getMotion().equals("wing");
                    if (wing) {
                        matrices.translate(part.getPivotX()/16f,1/16f,0);
                        angle = Math.signum(part.getPivotX()) * (18f + (float)Math.sin(outfit.getAge()*.12f)*14f);
                    } else if (part.getMotion().equals("cape")) angle = 8f+(float)Math.sin(outfit.getAge()*.08f)*4f;
                    else if (part.getMotion().equals("pet")) matrices.translate(0,Math.sin(outfit.getAge()*.12f)*.025,0);
//? if >=26.1 {
/*                    if (angle != 0) matrices.mulPose((wing?Axis.YP:Axis.XP).rotationDegrees(angle));
*///?} else if >=1.19.3 {
                    if (angle != 0) matrices.multiply((wing?RotationAxis.POSITIVE_Y:RotationAxis.POSITIVE_X).rotationDegrees(angle));
//?} else {
/*                    if (angle != 0) matrices.multiply((wing?Vec3f.POSITIVE_Y:Vec3f.POSITIVE_X).getDegreesQuaternion(angle));
*///?}
                    matrices.scale(1/16f,1/16f,1/16f);
//? if >=26.1 {
/*                    queue.submitCustomGeometry(matrices,RenderTypes.entityTranslucent(item.getTexture()),
                        (pose,vertices)->CosmeticGeometry.draw(pose,vertices,part.getVertices(),light));
*///?} else if >=1.21.9 {
/*                    queue.submitCustom(matrices,RenderLayers.entityTranslucent(item.getTexture()),
                        (pose,vertices)->CosmeticGeometry.draw(pose,vertices,part.getVertices(),light));
*///?} else {
                    CosmeticGeometry.draw(matrices.peek(),consumers.getBuffer(RenderLayer.getEntityTranslucent(item.getTexture())),part.getVertices(),light);
//?}
                } finally {
//? if >=26.1 {
/*                    matrices.popPose();
*///?} else {
                    matrices.pop();
//?}
                }
            }
        }
    }
}
