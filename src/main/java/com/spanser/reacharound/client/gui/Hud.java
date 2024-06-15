package com.spanser.reacharound.client.gui;

import com.spanser.reacharound.client.feature.PlacementFeature;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class Hud {
    private final MinecraftClient client;
    private final ReacharoundConfig config;

    public Hud(MinecraftClient client, ReacharoundConfig config) {
        this.client = client;
        this.config = config;
    }

    public void renderPlacementAssistText(DrawContext context, float deltaTime) {
        if (!config.render2d || !PlacementFeature.canReachAround(client)) {
            return;
        }

        context.getMatrices().push();
        context.getMatrices().translate(
                context.getScaledWindowWidth() / 2f + config.indicatorOffsetX,
                context.getScaledWindowHeight() / 2f - 4 + config.indicatorOffsetY,
                0
        );

        int duration = config.indicatorAnimationDuration;
        float scale;
        if (config.indicatorAnimationDuration > 0) {
            scale = Math.min(duration, PlacementFeature.ticksDisplayed + deltaTime)
                    / ((float) (duration));
        } else {
            scale = 1;
        }

        float fade;
        switch (config.indicatorAnimationFadeInterpolation) {
            case 1 -> fade = scale; // linear
            case 2 -> fade = scale * scale; // quadratic
            case 3 -> fade = scale * scale * scale; // cubic
            default -> fade = 1; // none
        }

        switch (config.indicatorAnimationInterpolation) {
            case 1 -> scale *= 1; // linear
            case 2 -> scale *= scale; // quadratic
            case 3 -> scale *= scale * scale; // cubic
            default -> scale = 1; // none
        }
        context.getMatrices().scale(scale, 1f, 1f);

        int color;
        if (PlacementFeature.canPlace(client.player)) {
            color = config.indicatorColor2D;
        } else {
            color = config.indicatorColor2DObstructed;
        }

        int alpha = (int) ((color >>> 24) * fade);

        color = (alpha << 24) | (color & 0x00ffffff);

        switch (config.indicator2DStyle) {
            case 1 -> renderStyleQuark(context, color);
            case 2 -> renderStyleCustom(context, color);
            default -> renderStyleDefault(context, color);
        }

        context.getMatrices().pop();
    }

    public void renderStyleDefault(DrawContext context, int color) {
        if (PlacementFeature.isVertical()) {
            if ((client.player != null ? client.player.getPitch() : 0) < 0) {
                context.getMatrices().translate(0, -4, 0);
            } else {
                context.getMatrices().translate(0, 4, 0);
            }
        }

        String displayVertical = "- -";
        String displayHorizontal = "-   -";
        String text = PlacementFeature.isVertical() ? displayVertical : displayHorizontal;
        renderText(context, color, text);
    }

    public void renderStyleQuark(DrawContext context, int color) {
        String displayVerticalQuark = "[  ]";
        String displayHorizontalQuark = "<  >";
        String text = PlacementFeature.isVertical() ? displayVerticalQuark : displayHorizontalQuark;
        renderText(context, color, text);
    }

    public void renderStyleCustom(DrawContext context, int color) {
        String text = PlacementFeature.isVertical() ? config.indicatorVertical : config.indicatorHorizontal;
        renderText(context, color, text);
    }

    public void renderText(DrawContext context, int color, String text) {
        context.getMatrices().translate(-client.textRenderer.getWidth(text) / 2.0f, 0, 0);
        context.drawText(client.textRenderer, text, 0, 0, color, false);
    }
}
