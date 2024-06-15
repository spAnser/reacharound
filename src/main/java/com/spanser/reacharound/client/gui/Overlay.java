package com.spanser.reacharound.client.gui;

import com.spanser.reacharound.client.feature.PlacementFeature;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import static com.spanser.reacharound.Reacharound.LOGGER;

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

        vertexConsumer.vertex(entry, x1, y2, z1).color(color).next();
        vertexConsumer.vertex(entry, x1, y2, z2).color(color).next();
        vertexConsumer.vertex(entry, x2, y2, z2).color(color).next();
        vertexConsumer.vertex(entry, x2, y2, z1).color(color).next();

        vertexConsumer.vertex(entry, x1, y2, z1).color(color).next();
        vertexConsumer.vertex(entry, x2, y2, z1).color(color).next();
        vertexConsumer.vertex(entry, x2, y1, z1).color(color).next();
        vertexConsumer.vertex(entry, x1, y1, z1).color(color).next();

        vertexConsumer.vertex(entry, x2, y2, z2).color(color).next();
        vertexConsumer.vertex(entry, x1, y2, z2).color(color).next();
        vertexConsumer.vertex(entry, x1, y1, z2).color(color).next();
        vertexConsumer.vertex(entry, x2, y1, z2).color(color).next();

        vertexConsumer.vertex(entry, x1, y2, z2).color(color).next();
        vertexConsumer.vertex(entry, x1, y2, z1).color(color).next();
        vertexConsumer.vertex(entry, x1, y1, z1).color(color).next();
        vertexConsumer.vertex(entry, x1, y1, z2).color(color).next();

        vertexConsumer.vertex(entry, x2, y1, z2).color(color).next();
        vertexConsumer.vertex(entry, x2, y1, z1).color(color).next();
        vertexConsumer.vertex(entry, x2, y2, z1).color(color).next();
        vertexConsumer.vertex(entry, x2, y2, z2).color(color).next();

        vertexConsumer.vertex(entry, x2, y1, z1).color(color).next();
        vertexConsumer.vertex(entry, x2, y1, z2).color(color).next();
        vertexConsumer.vertex(entry, x1, y1, z2).color(color).next();
        vertexConsumer.vertex(entry, x1, y1, z1).color(color).next();
    }

    public void render(WorldRenderContext context) {
        if (context.matrixStack() == null || !config.render3d || !PlacementFeature.canReachAround(client)) {
            return;
        }

        VertexConsumerProvider vertexConsumerProvider = context.consumers();

        if (vertexConsumerProvider == null) {
            return;
        }

        Camera camera = context.camera();

        if (config.indicator3DStyle != 1) {
            VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayer.getGui());

            if (vertexConsumer != null) {
                int colorSolid;
                if (PlacementFeature.canPlace(client.player)) {
                    colorSolid = config.indicatorColor3DSolid;
                } else {
                    colorSolid = config.indicatorColor3DObstructedSolid;
                }

                drawBox(
                        context.matrixStack(),
                        vertexConsumer,
                        (float) (PlacementFeature.currentTarget.pos().getX() - camera.getPos().getX()),
                        (float) (PlacementFeature.currentTarget.pos().getY() - camera.getPos().getY()),
                        (float) (PlacementFeature.currentTarget.pos().getZ() - camera.getPos().getZ()),
                        (float) (PlacementFeature.currentTarget.pos().getX() - camera.getPos().getX() + 1),
                        (float) (PlacementFeature.currentTarget.pos().getY() - camera.getPos().getY() + 1),
                        (float) (PlacementFeature.currentTarget.pos().getZ() - camera.getPos().getZ() + 1),
                        colorSolid
                );
            }
        }

        if (config.indicator3DStyle != 2) {
            VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayer.getLines());

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
                WorldRenderer.drawShapeOutline(
                        context.matrixStack(),
                        vertexConsumer,
                        shape,
                        PlacementFeature.currentTarget.pos().getX() - camera.getPos().getX(),
                        PlacementFeature.currentTarget.pos().getY() - camera.getPos().getY(),
                        PlacementFeature.currentTarget.pos().getZ() - camera.getPos().getZ(),
                        r, g, b, a, true
                );
            }
        }
    }
}
