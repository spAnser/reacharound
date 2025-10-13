package com.spanser.reacharound.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.client.feature.PlacementFeature;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private WorldRenderState worldRenderState;
    @Shadow private ClientWorld world;

    // We won't draw manually here; vanilla will call renderTargetBlockOutline later if we set the state
    @Inject(
            method = "render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;fillEntityOutlineRenderStates(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/state/WorldRenderState;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void reacharound$afterFill(
            ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera,
            Matrix4f positionMatrix, Matrix4f extraMatrix, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer,
            Vector4f fogColor, boolean renderSky, CallbackInfo ci
    ) {
        // If vanilla already set an outline (player is looking at a block), leave it alone.
        if (this.worldRenderState.outlineRenderState != null)
            return;

        var target = PlacementFeature.getCurrentTarget();
        BlockPos ghostPos = (target != null) ? target.pos() : null;
        if (ghostPos == null || this.world == null)
            return;

        // World border check (uses X/Z; safe for negative Y)
        if (!this.world.getWorldBorder().contains(ghostPos))
            return;

        // Build a non-empty shape for the outline
        BlockState placingState = getPlacingStateFromHeldItem(this.client.player, ghostPos);
        VoxelShape shape = VoxelShapes.fullCube();
        if (placingState != null && !placingState.isAir()) {
            var s = placingState.getOutlineShape(this.world, ghostPos, ShapeContext.of(this.client.player));
            if (!s.isEmpty()) shape = s;
        }

        var config = Reacharound.getInstance().config;

        // Decide translucency: we can safely force false so it gets drawn in the first call,
        // because renderMain will call renderTargetBlockOutline twice (false, then true).
        boolean translucent = config.indicator3DStyle == 2 || config.indicator3DStyle == 0;
        var color = config.indicatorColor3DSolid;
        boolean highContrast = this.client.options.getHighContrastBlockOutline().getValue();

        // IMPORTANT: clone the position so it can't mutate later
        this.worldRenderState.outlineRenderState = new OutlineRenderState(new BlockPos(ghostPos),
                translucent, highContrast, shape);
    }

    // Helper: try to infer what block would be placed; fall back to default state
    @Unique
    private static BlockState getPlacingStateFromHeldItem(PlayerEntity player, BlockPos pos) {
        if (player == null) return null;
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof BlockItem bi)) return null;
        BlockState base = bi.getBlock().getDefaultState();
        try {
            var eye = player.getCameraPosVec(1.0F);
            var hit = new BlockHitResult(eye, player.getHorizontalFacing().getOpposite(), pos, false);
            var ctx = new ItemPlacementContext(player, Hand.MAIN_HAND, stack, hit);
            BlockState placed = bi.getBlock().getPlacementState(ctx);
            return placed != null ? placed : base;
        } catch (Throwable t) {
            return base;
        }
    }
}
