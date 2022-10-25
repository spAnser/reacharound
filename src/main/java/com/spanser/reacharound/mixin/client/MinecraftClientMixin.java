package com.spanser.reacharound.mixin.client;

import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.client.feature.PlacementFeature;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin extends ReentrantThreadExecutor<Runnable> {
    @Shadow
    public ClientPlayerEntity player;

    @Shadow
    static MinecraftClient instance;

    public MinecraftClientMixin(String string) {
        super(string);
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    public void onTick(CallbackInfo ci) {
        ReacharoundConfig config = Reacharound.getInstance().config;
        if (!config.enabled) {
            return;
        }

        PlacementFeature.currentTarget = null;

        if (player != null)
            PlacementFeature.checkPlayerReacharoundTarget(player);

        if (PlacementFeature.currentTarget != null) {
            if (PlacementFeature.ticksDisplayed < config.indicatorAnimationDuration) {
                PlacementFeature.ticksDisplayed++;
            }
        } else {
            PlacementFeature.ticksDisplayed = 0;
        }
    }

    @Redirect(method = "doItemUse", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"
    ))
    private ItemStack onItemUse(ClientPlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (Reacharound.getInstance().config.enabled) {
            PlacementFeature.executeReacharound(instance, hand, itemStack);
        }

        return itemStack;
    }
}
