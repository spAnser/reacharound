package com.spanser.reacharound.mixin.client;

import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.client.feature.PlacementFeature;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int scaledHeight;
    @Shadow
    private int scaledWidth;

    private ReacharoundConfig config;

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void renderPlacementAssistText(DrawContext context, float tickDelta, CallbackInfo ci) {
        config = Reacharound.getInstance().config;

        if (!canReachAround()) {
            return;
        }

        context.getMatrices().push();
        context.getMatrices().translate(scaledWidth / 2F, scaledHeight / 2f - 4, 0);

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
        context.getMatrices().scale(scale, 1f, 1f);

        int color;
        if (PlacementFeature.canPlace(client.player)) {
            color = config.indicatorColor;
        } else {
            color = config.indicatorColorObstructed;
        }

        int alpha = (int) ((color >>> 24) * fade);

        color = (alpha << 24) | (color & 0x00ffffff);

        switch (config.indicatorStyle) {
            case 1 -> renderStyleQuark(context, color);
            case 2 -> renderStyleCustom(context, color);
            default -> renderStyleDefault(context, color);
        }

        context.getMatrices().pop();
    }

    public void renderStyleDefault(DrawContext context, int color) {
        if (PlacementFeature.isVertical()) {
            if (client.player.getPitch() < 0) {
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

    private boolean canReachAround() {
        return config.enabled && PlacementFeature.currentTarget != null && client.player != null && client.world != null && client.crosshairTarget != null;
    }
}
