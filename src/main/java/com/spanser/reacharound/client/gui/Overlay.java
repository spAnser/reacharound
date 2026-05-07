package com.spanser.reacharound.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.spanser.reacharound.client.feature.PlacementFeature;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Overlay {
    private final Minecraft client;
    private final ReacharoundConfig config;

    private final VoxelShape shape = Shapes.create(new AABB(0, 0, 0, 1f, 1f, 1f));

    public Overlay(Minecraft client, ReacharoundConfig config) {
        this.client = client;
        this.config = config;
    }

    public static void drawBox(PoseStack matrices, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        PoseStack.Pose entry = matrices.last();

        vertexConsumer.addVertex(entry, x1, y2, z1).setColor(color);
        vertexConsumer.addVertex(entry, x1, y2, z2).setColor(color);
        vertexConsumer.addVertex(entry, x2, y2, z2).setColor(color);
        vertexConsumer.addVertex(entry, x2, y2, z1).setColor(color);

        vertexConsumer.addVertex(entry, x1, y2, z1).setColor(color);
        vertexConsumer.addVertex(entry, x2, y2, z1).setColor(color);
        vertexConsumer.addVertex(entry, x2, y1, z1).setColor(color);
        vertexConsumer.addVertex(entry, x1, y1, z1).setColor(color);

        vertexConsumer.addVertex(entry, x2, y2, z2).setColor(color);
        vertexConsumer.addVertex(entry, x1, y2, z2).setColor(color);
        vertexConsumer.addVertex(entry, x1, y1, z2).setColor(color);
        vertexConsumer.addVertex(entry, x2, y1, z2).setColor(color);

        vertexConsumer.addVertex(entry, x1, y2, z2).setColor(color);
        vertexConsumer.addVertex(entry, x1, y2, z1).setColor(color);
        vertexConsumer.addVertex(entry, x1, y1, z1).setColor(color);
        vertexConsumer.addVertex(entry, x1, y1, z2).setColor(color);

        vertexConsumer.addVertex(entry, x2, y1, z2).setColor(color);
        vertexConsumer.addVertex(entry, x2, y1, z1).setColor(color);
        vertexConsumer.addVertex(entry, x2, y2, z1).setColor(color);
        vertexConsumer.addVertex(entry, x2, y2, z2).setColor(color);

        vertexConsumer.addVertex(entry, x2, y1, z1).setColor(color);
        vertexConsumer.addVertex(entry, x2, y1, z2).setColor(color);
        vertexConsumer.addVertex(entry, x1, y1, z2).setColor(color);
        vertexConsumer.addVertex(entry, x1, y1, z1).setColor(color);
    }

    public void render(LevelRenderContext context) {
        if (context.poseStack() == null || !config.render3d || !PlacementFeature.canReachAround(client)) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = context.bufferSource();

        if (bufferSource == null) {
            return;
        }

        Vec3 cameraPos = context.levelState().cameraRenderState.pos;

        if (config.indicator3DStyle != 1) {
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypes.debugFilledBox());

            if (vertexConsumer != null) {
                int colorSolid;
                if (PlacementFeature.canPlace(client.player)) {
                    colorSolid = config.indicatorColor3DSolid;
                } else {
                    colorSolid = config.indicatorColor3DObstructedSolid;
                }

                PlacementFeature.ReacharoundTarget target = PlacementFeature.getCurrentTarget();
                drawBox(
                        context.poseStack(),
                        vertexConsumer,
                        (float) (target.pos().getX() - cameraPos.x()),
                        (float) (target.pos().getY() - cameraPos.y()),
                        (float) (target.pos().getZ() - cameraPos.z()),
                        (float) (target.pos().getX() - cameraPos.x() + 1),
                        (float) (target.pos().getY() - cameraPos.y() + 1),
                        (float) (target.pos().getZ() - cameraPos.z() + 1),
                        colorSolid
                );
            }
        }

        if (config.indicator3DStyle != 2) {
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypes.lines());

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
                ShapeRenderer.renderShape(
                        context.poseStack(),
                        vertexConsumer,
                        shape,
                        target.pos().getX() - cameraPos.x(),
                        target.pos().getY() - cameraPos.y(),
                        target.pos().getZ() - cameraPos.z(),
                        ARGB.colorFromFloat(a, r, g, b),
                        config.indicator3DThickness
                );
            }
        }
    }
}
