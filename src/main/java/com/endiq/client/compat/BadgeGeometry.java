package com.endiq.client.compat;

//? if >=26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
*///?} else {
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.OverlayTexture;
//?}

public final class BadgeGeometry {
    private BadgeGeometry() {}

//? if >=26.1 {
/*    public static void draw(PoseStack.Pose pose, VertexConsumer vertices, float left, int light) {
*///?} else {
    public static void draw(MatrixStack.Entry pose, VertexConsumer vertices, float left, int light) {
//?}
        vertex(pose, vertices, left, -4, 0, 0, light);
        vertex(pose, vertices, left, 4, 0, 1, light);
        vertex(pose, vertices, left + 8, 4, 1, 1, light);
        vertex(pose, vertices, left + 8, -4, 1, 0, light);
    }

//? if >=26.1 {
/*    private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, float x, float y, float u, float v, int light) {
        vertices.addVertex(pose.pose(), x, y, 0).setColor(255, 255, 255, 220)
            .setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
    }
*///?} else if >=1.21 {
    private static void vertex(MatrixStack.Entry pose, VertexConsumer vertices, float x, float y, float u, float v, int light) {
        vertices.vertex(pose.getPositionMatrix(), x, y, 0).color(255, 255, 255, 220)
            .texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(pose, 0, 0, 1);
    }
//?} else {
/*    private static void vertex(MatrixStack.Entry pose, VertexConsumer vertices, float x, float y, float u, float v, int light) {
        vertices.vertex(pose.getPositionMatrix(), x, y, 0).color(255, 255, 255, 220)
            .texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1).next();
    }
*///?}
}
