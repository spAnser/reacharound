package com.spanser.reacharound.client.gui;

import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.client.feature.PlacementFeature;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;

public class Hud {
    private final MinecraftClient client;
    private final Reacharound reacharound;

    public Hud(MinecraftClient client, Reacharound reacharound) {
        this.client = client;
        this.reacharound = reacharound;
    }

    public void renderPlacementAssistText(MatrixStack matrices, float deltaTime) {
        ReacharoundConfig config = reacharound.config;

        if (!canReachAround()) {
            return;
        }

        matrices.push();
        matrices.translate(
                client.getWindow().getScaledWidth() / 2F + reacharound.config.indicatorOffsetX,
                client.getWindow().getScaledHeight() / 2f - 4 + reacharound.config.indicatorOffsetY,
                0
        );

        int duration = config.indicatorAnimationDuration;
        float scale;
        if (config.indicatorAnimationDuration > 0) {
            scale = Math.min(duration, PlacementFeature.ticksDisplayed + deltaTime) / ((float) (duration));
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
        matrices.scale(scale, 1f, 1f);

        int color;
        if (PlacementFeature.canPlace(client.player)) {
            color = config.indicatorColor;
        } else {
            color = config.indicatorColorObstructed;
        }

        int alpha = (int) ((color >>> 24) * fade);

        color = (alpha << 24) | (color & 0x00ffffff);

        switch (config.indicatorStyle) {
            case 1 -> renderStyleQuark(matrices, color);
            case 2 -> renderStyleCustom(matrices, color);
            default -> renderStyleDefault(matrices, color);
        }

        matrices.pop();
    }

    public void renderStyleDefault(MatrixStack matrices, int color) {
        if (PlacementFeature.isVertical()) {
            if ((client.player != null ? client.player.getPitch() : 0) < 0) {
                matrices.translate(0, -4, 0);
            } else {
                matrices.translate(0, 4, 0);
            }
        }

        String displayVertical = "- -";
        String displayHorizontal = "-   -";
        String text = PlacementFeature.isVertical() ? displayVertical : displayHorizontal;
        renderText(matrices, color, text);
    }

    public void renderStyleQuark(MatrixStack matrices, int color) {
        String displayVerticalQuark = "[  ]";
        String displayHorizontalQuark = "<  >";
        String text = PlacementFeature.isVertical() ? displayVerticalQuark : displayHorizontalQuark;
        renderText(matrices, color, text);
    }

    public void renderStyleCustom(MatrixStack matrices, int color) {
        String text = PlacementFeature.isVertical() ? reacharound.config.indicatorVertical : reacharound.config.indicatorHorizontal;
        renderText(matrices, color, text);
    }

    public void renderText(MatrixStack matrices, int color, String text) {
        matrices.translate(-client.textRenderer.getWidth(text) / 2.0f, 0, 0);
        client.textRenderer.draw(matrices, text, 0, 0, color);
    }

    private boolean canReachAround() {
        return reacharound.config.enabled &&
                PlacementFeature.currentTarget != null &&
                (PlacementFeature.currentTarget.hand() != Hand.OFF_HAND || (PlacementFeature.currentTarget.hand() == Hand.OFF_HAND && reacharound.config.offhand)) &&
                client.player != null &&
                client.world != null &&
                client.crosshairTarget != null;
    }
}
