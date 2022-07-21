package com.spanser.reacharound.mixin.client;

import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.client.feature.PlacementFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
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

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void renderPlacementAssistText(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (!canReachAround()) {
            return;
        }

        matrices.push();
        matrices.translate(scaledWidth / 2F, scaledHeight / 2f - 4, 0);

        float scale = Math.min(5, PlacementFeature.ticksDisplayed + tickDelta) / 5F;
        scale *= scale;
        matrices.scale(scale, 1f, 1f);

        int color;
        if (PlacementFeature.canPlace(client.player)) {
            color = 0xffffff | (((int) (255 * scale)) << 24);
        } else {
            color = 0xff5555 | (((int) (255 * scale)) << 24);
        }

        switch (Reacharound.getInstance().config.indicatorStyle) {
            case 1 -> renderStyleQuark(matrices, color);
            case 2 -> renderStyleCustom(matrices, color);
            default -> renderStyleDefault(matrices, color);
        }

        matrices.pop();
    }

    public void renderStyleDefault(MatrixStack matrices, int color) {
        if (PlacementFeature.isVertical()) {
            if (client.player.getPitch() < 0) {
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
        String text = PlacementFeature.isVertical() ? Reacharound.getInstance().config.indicatorVertical : Reacharound.getInstance().config.indicatorHorizontal;
        renderText(matrices, color, text);
    }

    public void renderText(MatrixStack matrices, int color, String text) {
        matrices.translate(-client.textRenderer.getWidth(text) / 2.0f, 0, 0);
        client.textRenderer.draw(matrices, text, 0, 0, color);
    }

    private boolean canReachAround() {
        return Reacharound.getInstance().config.enabled && PlacementFeature.currentTarget != null && client.player != null && client.world != null && client.crosshairTarget != null;
    }
}
