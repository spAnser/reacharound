package com.spanser.reacharound.client.gui;

import com.spanser.reacharound.client.feature.PlacementFeature;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Hud {
    private final Minecraft client;
    private final ReacharoundConfig config;

    public Hud(Minecraft client, ReacharoundConfig config) {
        this.client = client;
        this.config = config;
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!config.render2d || client.gui.screen() != null || !PlacementFeature.canReachAround(client)) {
            return;
        }

        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);

        graphics.pose().pushMatrix();
        graphics.pose().translate(
                graphics.guiWidth() / 2f + config.indicatorOffsetX,
                graphics.guiHeight() / 2f - 4 + config.indicatorOffsetY
        );

        int duration = config.indicatorAnimationDuration;
        float scale;
        if (config.indicatorAnimationDuration > 0) {
            scale = Math.min(duration, PlacementFeature.ticksDisplayed + tickDelta) / ((float) (duration));
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
        graphics.pose().scale(scale, 1f);

        int color;
        if (PlacementFeature.canPlace(client.player)) {
            color = config.indicatorColor2D;
        } else {
            color = config.indicatorColor2DObstructed;
        }

        int alpha = (int) ((color >>> 24) * fade);

        color = (alpha << 24) | (color & 0x00ffffff);

        switch (config.indicator2DStyle) {
            case 1 -> renderStyleQuark(graphics, color);
            case 2 -> renderStyleCustom(graphics, color);
            default -> renderStyleDefault(graphics, color);
        }

        graphics.pose().popMatrix();
    }

    public void renderStyleDefault(GuiGraphicsExtractor graphics, int color) {
        if (PlacementFeature.isVertical()) {
            if ((client.player != null ? client.player.getXRot() : 0) < 0) {
                graphics.pose().translate(0, -4);
            } else {
                graphics.pose().translate(0, 4);
            }
        }

        String displayVertical = "- -";
        String displayHorizontal = "-   -";
        String text = PlacementFeature.isVertical() ? displayVertical : displayHorizontal;
        renderText(graphics, color, text);
    }

    public void renderStyleQuark(GuiGraphicsExtractor graphics, int color) {
        String displayVerticalQuark = "[  ]";
        String displayHorizontalQuark = "<  >";
        String text = PlacementFeature.isVertical() ? displayVerticalQuark : displayHorizontalQuark;
        renderText(graphics, color, text);
    }

    public void renderStyleCustom(GuiGraphicsExtractor graphics, int color) {
        String text = PlacementFeature.isVertical() ? config.indicatorVertical : config.indicatorHorizontal;
        renderText(graphics, color, text);
    }

    public void renderText(GuiGraphicsExtractor graphics, int color, String text) {
        graphics.pose().translate(-client.font.width(text) / 2f, 0);
        graphics.text(client.font, text, 0, 0, color, false);
    }
}
