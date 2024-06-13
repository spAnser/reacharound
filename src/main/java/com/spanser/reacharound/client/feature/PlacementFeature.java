package com.spanser.reacharound.client.feature;

import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.client.handler.RayTraceHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public class PlacementFeature {
    public static double leniency = 0.5;

    public static ReacharoundTarget currentTarget;
    public static int ticksDisplayed;

    private static boolean blockIsTopSlab(BlockState block) {
        return block.contains(Properties.SLAB_TYPE) && block.get(Properties.SLAB_TYPE) == SlabType.TOP;
    }

    private static boolean blockIsBottomSlab(BlockState block) {
        return block.contains(Properties.SLAB_TYPE) && block.get(Properties.SLAB_TYPE) == SlabType.BOTTOM;
    }

    public static BlockState getPlacement(ClientPlayerEntity player) {
        BlockState block;

        if (isVertical()) {
            boolean isLookingDown = player.getPitch() > 0;

            block = player.getWorld().getBlockState(currentTarget.pos().add(0, isLookingDown ? 1 : -1, 0));
            if (isLookingDown && blockIsTopSlab(block)) {
                currentTarget = new ReacharoundTarget(currentTarget.pos().add(0, 1, 0), currentTarget.dir(), currentTarget.hand());
            } else if (!isLookingDown && blockIsBottomSlab(block)) {
                currentTarget = new ReacharoundTarget(currentTarget.pos().add(0, -1, 0), currentTarget.dir(), currentTarget.hand());
            }
        } else {
            Vec3i facing = player.getHorizontalFacing().getVector();
            block = player.getWorld().getBlockState(currentTarget.pos().add(-facing.getX(), 0, -facing.getZ()));
        }

        return block;
    }

    public static boolean canPlace(ClientPlayerEntity player) {
        BlockState block = getPlacement(player);
        return player.getWorld().canPlace(block, currentTarget.pos(), ShapeContext.absent());
    }

    public static boolean executeReacharound(MinecraftClient client, Hand hand, ItemStack itemStack) {
        if (currentTarget != null) {
            BlockHitResult blockHitResult;

            int x = currentTarget.pos().getX();
            int y = currentTarget.pos().getY();
            int z = currentTarget.pos().getZ();
            Vec3d source = new Vec3d(x, y, z);

            boolean isLookingDown = client.player.getPitch() > 0;

            BlockState block = getPlacement(client.player);

            if (blockIsTopSlab(block)) {
                source = source.add(0, 1, 0);
            }

            Direction direction;
            if (isVertical()) {
                direction = Direction.fromVector(0, isLookingDown ? -1 : 1, 0);
            } else {
                Vec3i facing = client.player.getHorizontalFacing().getVector();
                direction = Direction.fromVector(-facing.getX(), 0, -facing.getZ());
            }

            blockHitResult = new BlockHitResult(source, direction, currentTarget.pos(), false);

            int count = itemStack.getCount();
            ActionResult result = client.interactionManager.interactBlock(client.player, hand, blockHitResult);
            if (result.isAccepted()) {
                if (result.shouldSwingHand()) {
                    client.player.swingHand(hand);
                    if (!itemStack.isEmpty() && (itemStack.getCount() != count || client.interactionManager.hasCreativeInventory())) {
                        client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                    }
                }

                return true;
            }
        }
        return false;
    }

    public static void checkPlayerReacharoundTarget(ClientPlayerEntity player) {
        Hand hand = null;
        if (validateReacharoundStack(player.getMainHandStack()))
            hand = Hand.MAIN_HAND;
        else if (validateReacharoundStack(player.getOffHandStack()))
            hand = Hand.OFF_HAND;

        if (hand == null)
            return;

        World world = player.getWorld();

        Pair<Vec3d, Vec3d> params = RayTraceHandler.getEntityParams(player);
        double range = player.getBlockInteractionRange();
        Vec3d rayPos = params.getLeft().add(params.getRight().multiply(0.5f));
        Vec3d ray = params.getRight().multiply(range);

        HitResult normalRes = RayTraceHandler.rayTrace(player, world, rayPos, ray, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE);

        if (normalRes.getType() == HitResult.Type.MISS) {
            switch (Reacharound.getInstance().config.mode) {
                case 1 -> currentTarget = getPlayerHorizontalReacharoundTarget(player, hand, world, rayPos, ray);
                case 2 -> currentTarget = getPlayerVerticalReacharoundTarget(player, hand, world, rayPos, ray);
                default -> {
                    ReacharoundTarget target = getPlayerVerticalReacharoundTarget(player, hand, world, rayPos, ray);
                    if (target != null) {
                        currentTarget = target;
                        break;
                    }
                    currentTarget = getPlayerHorizontalReacharoundTarget(player, hand, world, rayPos, ray);
                }
            }
        }
    }

    private static ReacharoundTarget getPlayerVerticalReacharoundTarget(Entity player, Hand hand, World world, Vec3d rayPos, Vec3d ray) {
        boolean isLookingDown = player.getPitch() > 0;
        if (isLookingDown) {
            rayPos = rayPos.add(0, leniency, 0);
        } else {
            rayPos = rayPos.add(0, -leniency, 0);
        }
        HitResult take2Res = RayTraceHandler.rayTrace(player, world, rayPos, ray, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE);

        if (take2Res.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) take2Res).getBlockPos();
            if (isLookingDown) {
                pos = pos.down();
            } else {
                pos = pos.up();
            }
            BlockState state = world.getBlockState(pos);

            double distance = pos.getY() - player.getPos().y;
            if (isLookingDown) {
                distance = -distance;
            }

            if (distance > 1 && (state.isAir() || state.isReplaceable()))
                return new ReacharoundTarget(pos, isLookingDown ? Direction.DOWN : Direction.UP, hand);
        }

        return null;
    }

    private static ReacharoundTarget getPlayerHorizontalReacharoundTarget(Entity player, Hand hand, World world, Vec3d rayPos, Vec3d ray) {
        Direction dir = Direction.fromRotation(player.getYaw());
        rayPos = rayPos.subtract(leniency * dir.getOffsetX(), 0, leniency * dir.getOffsetZ());
        HitResult take2Res = RayTraceHandler.rayTrace(player, world, rayPos, ray, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE);

        if (take2Res.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) take2Res).getBlockPos().offset(dir);
            BlockState state = world.getBlockState(pos);

            if ((state.isAir() || state.isReplaceable()))
                return new ReacharoundTarget(pos, dir.getOpposite(), hand);
        }

        return null;
    }

    private static boolean validateReacharoundStack(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof BlockItem;
    }

    private record ReacharoundTarget(BlockPos pos, Direction dir, Hand hand) {
    }

    public static boolean isVertical() {
        return PlacementFeature.currentTarget.dir.getAxis() == Direction.Axis.Y;
    }
}
