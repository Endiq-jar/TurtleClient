package com.endiq.client.compat;

import com.endiq.client.cosmetics.CosmeticMeshes;
import java.util.List;
//? if >=26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
*///?} else {
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.OverlayTexture;
//?}

public final class CosmeticGeometry {
    private CosmeticGeometry() {}
//? if >=26.1 {
/*    public static void draw(PoseStack.Pose pose, VertexConsumer consumer, List<CosmeticMeshes.Vertex> vertices, int light) {
*///?} else {
    public static void draw(MatrixStack.Entry pose, VertexConsumer consumer, List<CosmeticMeshes.Vertex> vertices, int light) {
//?}
        for (CosmeticMeshes.Vertex v : vertices) {
            int color = v.getColor();
//? if >=26.1 {
/*            consumer.addVertex(pose.pose(),v.getX(),v.getY(),v.getZ()).setColor(color)
                .setUv(v.getU(),v.getV()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose,v.getNx(),v.getNy(),v.getNz());
*///?} else if >=1.21 {
            consumer.vertex(pose.getPositionMatrix(),v.getX(),v.getY(),v.getZ()).color(color)
                .texture(v.getU(),v.getV()).overlay(OverlayTexture.DEFAULT_UV).light(light)
                .normal(pose,v.getNx(),v.getNy(),v.getNz());
//?} else {
/*            consumer.vertex(pose.getPositionMatrix(),v.getX(),v.getY(),v.getZ()).color(color)
                .texture(v.getU(),v.getV()).overlay(OverlayTexture.DEFAULT_UV).light(light)
                .normal(pose.getNormalMatrix(),v.getNx(),v.getNy(),v.getNz()).next();
*///?}
        }
    }
}
