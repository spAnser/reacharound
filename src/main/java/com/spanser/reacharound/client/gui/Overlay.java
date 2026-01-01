package com.spanser.reacharound.client.gui;

import com.spanser.reacharound.client.feature.PlacementFeature;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class Overlay {
    private final MinecraftClient client;
    private final ReacharoundConfig config;

    private final VoxelShape shape = VoxelShapes.cuboid(new Box(0, 0, 0, 1f, 1f, 1f));

    public Overlay(MinecraftClient client, ReacharoundConfig config) {
        this.client = client;
        this.config = config;
    }

    public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        MatrixStack.Entry entry = matrices.peek();

        vertexConsumer.vertex(entry, x1, y2, z1).color(color);
        vertexConsumer.vertex(entry, x1, y2, z2).color(color);
        vertexConsumer.vertex(entry, x2, y2, z2).color(color);
        vertexConsumer.vertex(entry, x2, y2, z1).color(color);

        vertexConsumer.vertex(entry, x1, y2, z1).color(color);
        vertexConsumer.vertex(entry, x2, y2, z1).color(color);
        vertexConsumer.vertex(entry, x2, y1, z1).color(color);
        vertexConsumer.vertex(entry, x1, y1, z1).color(color);

        vertexConsumer.vertex(entry, x2, y2, z2).color(color);
        vertexConsumer.vertex(entry, x1, y2, z2).color(color);
        vertexConsumer.vertex(entry, x1, y1, z2).color(color);
        vertexConsumer.vertex(entry, x2, y1, z2).color(color);

        vertexConsumer.vertex(entry, x1, y2, z2).color(color);
        vertexConsumer.vertex(entry, x1, y2, z1).color(color);
        vertexConsumer.vertex(entry, x1, y1, z1).color(color);
        vertexConsumer.vertex(entry, x1, y1, z2).color(color);

        vertexConsumer.vertex(entry, x2, y1, z2).color(color);
        vertexConsumer.vertex(entry, x2, y1, z1).color(color);
        vertexConsumer.vertex(entry, x2, y2, z1).color(color);
        vertexConsumer.vertex(entry, x2, y2, z2).color(color);

        vertexConsumer.vertex(entry, x2, y1, z1).color(color);
        vertexConsumer.vertex(entry, x2, y1, z2).color(color);
        vertexConsumer.vertex(entry, x1, y1, z2).color(color);
        vertexConsumer.vertex(entry, x1, y1, z1).color(color);
    }

    public void render(WorldRenderContext context) {
        if (context.matrices() == null || !config.render3d || !PlacementFeature.canReachAround(client)) {
            return;
        }

        VertexConsumerProvider vertexConsumerProvider = context.consumers();

        if (vertexConsumerProvider == null) {
            return;
        }

        Camera camera = context.gameRenderer().getCamera();

        if (config.indicator3DStyle != 1) {
            VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayers.debugQuads());

            if (vertexConsumer != null) {
                int colorSolid;
                if (PlacementFeature.canPlace(client.player)) {
                    colorSolid = config.indicatorColor3DSolid;
                } else {
                    colorSolid = config.indicatorColor3DObstructedSolid;
                }

                PlacementFeature.ReacharoundTarget target = PlacementFeature.getCurrentTarget();
                drawBox(
                        context.matrices(),
                        vertexConsumer,
                        (float) (target.pos().getX() - camera.getCameraPos().getX()),
                        (float) (target.pos().getY() - camera.getCameraPos().getY()),
                        (float) (target.pos().getZ() - camera.getCameraPos().getZ()),
                        (float) (target.pos().getX() - camera.getCameraPos().getX() + 1),
                        (float) (target.pos().getY() - camera.getCameraPos().getY() + 1),
                        (float) (target.pos().getZ() - camera.getCameraPos().getZ() + 1),
                        colorSolid
                );
            }
        }

        if (config.indicator3DStyle != 2) {
            VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayers.lines());

            if (vertexConsumer != null) {
                int colorOutline;
                if (PlacementFeature.canPlace(client.player)) {
                    colorOutline = config.indicatorColor3DOutline;
                } else {
                    colorOutline = config.indicatorColor3DObstructedOutline;
                }
                float a = ((colorOutline >> 24) & 0xFF) / 255f;
                float r = ((colorOutline >> 16) & 0xFF) / 255f;
                float g = ((colorOutline >> 8) & 0xFF) / 255f;
                float b = (colorOutline & 0xFF) / 255f;
                PlacementFeature.ReacharoundTarget target = PlacementFeature.getCurrentTarget();
                VertexRendering.drawOutline(
                        context.matrices(),
                        vertexConsumer,
                        shape,
                        target.pos().getX() - camera.getCameraPos().getX(),
                        target.pos().getY() - camera.getCameraPos().getY(),
                        target.pos().getZ() - camera.getCameraPos().getZ(),
                        ColorHelper.fromFloats(a, r, g, b),
                        config.indicator3DThickness
                );
            }
        }
    }
}
