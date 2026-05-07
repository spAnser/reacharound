package com.spanser.reacharound.client.feature;

import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.client.handler.RayTraceHandler;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PlacementFeature {
    private static final Minecraft client = Minecraft.getInstance();
    private static final ReacharoundConfig config = Reacharound.getInstance().config;
    // Constants
    private static final double MIN_VERTICAL_DISTANCE = 1.0;
    private static final float LENIENCY_HORIZONTAL = 0.5f;
    private static final float LENIENCY_VERTICAL = 0.5f;
    private static final float RANGE_ADJUSTMENT = 0.5f;
    // Ray trace cache to avoid duplicate calculations within the same tick
    private static final Map<RayTraceKey, HitResult> rayTraceCache = new ConcurrentHashMap<>();
    public static int ticksDisplayed;
    private static volatile ReacharoundTarget currentTarget;

    public static synchronized ReacharoundTarget getCurrentTarget() {
        return currentTarget;
    }

    private static synchronized void setCurrentTarget(ReacharoundTarget target) {
        currentTarget = target;
    }

    // Cached ray trace method to avoid duplicate calculations
    private static HitResult cachedRayTrace(Entity entity, Level world, Vec3 startPos, Vec3 ray, ClipContext.Block blockMode, ClipContext.Fluid fluidMode) {
        Vec3 endPos = startPos.add(ray);
        RayTraceKey key = new RayTraceKey(startPos, endPos, blockMode, fluidMode);

        return rayTraceCache.computeIfAbsent(key, k -> {
            ClipContext context = new ClipContext(startPos, endPos, blockMode, fluidMode, entity);
            return world.clip(context);
        });
    }

    private static boolean blockIsTopSlab(BlockState block) {
        return block.hasProperty(BlockStateProperties.SLAB_TYPE) && block.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.TOP;
    }

    private static boolean blockIsBottomSlab(BlockState block) {
        return block.hasProperty(BlockStateProperties.SLAB_TYPE) && block.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM;
    }

    public static BlockState getPlacement(LocalPlayer player) {
        BlockState block;

        if (isVertical()) {
            boolean isLookingDown = player.getXRot() > 0;

            ReacharoundTarget target = getCurrentTarget();
            block = player.level().getBlockState(target.pos().offset(0, isLookingDown ? 1 : -1, 0));
            if (isLookingDown && blockIsTopSlab(block)) {
                setCurrentTarget(new ReacharoundTarget(target.pos().offset(0, 1, 0), target.dir(), target.hand()));
            } else if (!isLookingDown && blockIsBottomSlab(block)) {
                setCurrentTarget(new ReacharoundTarget(target.pos().offset(0, -1, 0), target.dir(), target.hand()));
            }
        } else {
            Direction facing = player.getDirection();
            block = player.level().getBlockState(getCurrentTarget().pos().offset(-facing.getStepX(), 0, -facing.getStepZ()));
        }

        return block;
    }

    public static boolean canPlace(LocalPlayer player) {
        BlockState block = getPlacement(player);
        ReacharoundTarget target = getCurrentTarget();
        return target != null && player.level().isUnobstructed(block, target.pos(), CollisionContext.empty());
    }

    public static boolean executeReacharound(Minecraft client, InteractionHand hand, ItemStack itemStack) {
        ReacharoundTarget target = getCurrentTarget();
        if (target != null) {
            BlockHitResult blockHitResult;

            int x = target.pos().getX();
            int y = target.pos().getY();
            int z = target.pos().getZ();
            Vec3 source = new Vec3(x, y, z);

            boolean isLookingDown = client.player.getXRot() > 0;

            BlockState block = getPlacement(client.player);

            if (blockIsTopSlab(block)) {
                source = source.add(0, 1, 0);
            }

            Direction direction;
            if (isVertical()) {
                direction = Direction.getNearest(0, isLookingDown ? -1 : 1, 0, Direction.UP);
            } else {
                Direction facing = client.player.getDirection();
                direction = Direction.getNearest(-facing.getStepX(), 0, -facing.getStepZ(), Direction.NORTH);
            }

            blockHitResult = new BlockHitResult(source, direction, target.pos(), false);

            int count = itemStack.getCount();
            InteractionResult result = client.gameMode.useItemOn(client.player, hand, blockHitResult);
            if (result.consumesAction()) {
                client.player.swing(hand);
                if (!itemStack.isEmpty() && (itemStack.getCount() != count || client.player.isCreative())) {
                    client.gameRenderer.itemInHandRenderer.itemUsed(hand);
                }

                return true;
            }
        }
        return false;
    }

    public static void checkPlayerReacharoundTarget(LocalPlayer player) {
        InteractionHand hand = null;
        if (validateReacharoundStack(player.getMainHandItem()))
            hand = InteractionHand.MAIN_HAND;
        else if (validateReacharoundStack(player.getOffhandItem()))
            hand = InteractionHand.OFF_HAND;

        if (hand == null)
            return;

        Level world = player.level();

        Pair<Vec3, Vec3> params = RayTraceHandler.getEntityParams(player);
        double range = player.blockInteractionRange() - RANGE_ADJUSTMENT;
        Vec3 rayPos = params.getLeft().add(params.getRight().scale(0.5f));
        Vec3 ray = params.getRight().scale(range);

        HitResult normalRes = cachedRayTrace(player, world, rayPos, ray, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);

        if (normalRes.getType() == HitResult.Type.MISS) {
            switch (config.axis) {
                case 1 -> setCurrentTarget(getPlayerHorizontalReacharoundTarget(player, hand, world, rayPos, ray));
                case 2 -> setCurrentTarget(getPlayerVerticalReacharoundTarget(player, hand, world, rayPos, ray));
                default -> {
                    ReacharoundTarget target = getPlayerVerticalReacharoundTarget(player, hand, world, rayPos, ray);
                    if (target != null) {
                        setCurrentTarget(target);
                        break;
                    }
                    setCurrentTarget(getPlayerHorizontalReacharoundTarget(player, hand, world, rayPos, ray));
                }
            }
        }
    }

    private static ReacharoundTarget getPlayerVerticalReacharoundTarget(Entity player, InteractionHand hand, Level world, Vec3 rayPos, Vec3 ray) {
        boolean isLookingDown = player.getXRot() > 0;
        if (isLookingDown) {
            rayPos = rayPos.add(0, LENIENCY_VERTICAL, 0);
        } else {
            rayPos = rayPos.add(0, -LENIENCY_VERTICAL, 0);
        }
        HitResult take2Res = cachedRayTrace(player, world, rayPos, ray, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);

        if (take2Res.getType() == HitResult.Type.BLOCK && take2Res instanceof BlockHitResult blockHitResult) {
            BlockPos pos = blockHitResult.getBlockPos();
            if (isLookingDown) {
                pos = pos.below();
            } else {
                pos = pos.above();
            }
            BlockState state = world.getBlockState(pos);

            double distance = pos.getY() - player.position().y;
            if (isLookingDown) {
                distance = -distance;
            }

            // Check world height limits
            if (pos.getY() < world.getMinY() || pos.getY() > world.getMaxY()) {
                return null;
            }

            if (distance > MIN_VERTICAL_DISTANCE && (state.isAir() || state.canBeReplaced()))
                return new ReacharoundTarget(pos, isLookingDown ? Direction.DOWN : Direction.UP, hand);
        }

        return null;
    }

    private static ReacharoundTarget getPlayerHorizontalReacharoundTarget(Entity player, InteractionHand hand, Level world, Vec3 rayPos, Vec3 ray) {
        Direction dir = Direction.fromYRot(player.getYRot());
        rayPos = rayPos.subtract(LENIENCY_HORIZONTAL * dir.getStepX(), 0, LENIENCY_HORIZONTAL * dir.getStepZ());
        HitResult take2Res = cachedRayTrace(player, world, rayPos, ray, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);

        if (take2Res.getType() == HitResult.Type.BLOCK && take2Res instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos().relative(dir);

            // Check world height limits
            if (pos.getY() < world.getMinY() || pos.getY() > world.getMaxY()) {
                return null;
            }

            BlockState state = world.getBlockState(pos);
            if ((state.isAir() || state.canBeReplaced()))
                return new ReacharoundTarget(pos, dir.getOpposite(), hand);
        }

        return null;
    }

    private static boolean validateReacharoundStack(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof BlockItem;
    }

    public static boolean isVertical() {
        ReacharoundTarget target = getCurrentTarget();
        return target != null && target.dir().getAxis() == Direction.Axis.Y;
    }

    public static boolean canReachAround(Minecraft client) {
        ReacharoundTarget target = getCurrentTarget();
        return config.enabled &&
                target != null &&
                (target.hand() != InteractionHand.OFF_HAND || (target.hand() == InteractionHand.OFF_HAND && config.offhand)) &&
                client.player != null &&
                client.level != null &&
                client.hitResult != null;
    }

    public static void tick(Minecraft client) {
        if (!config.enabled) {
            return;
        }

        // Clear ray trace cache at the start of each tick
        rayTraceCache.clear();

        setCurrentTarget(null);

        if (client.player != null)
            PlacementFeature.checkPlayerReacharoundTarget(client.player);

        if (getCurrentTarget() != null) {
            if (PlacementFeature.ticksDisplayed < config.indicatorAnimationDuration) {
                PlacementFeature.ticksDisplayed++;
            }
        } else {
            PlacementFeature.ticksDisplayed = 0;
        }
    }

    public static InteractionResult useItem(Player player, Level world, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!world.isClientSide()) {
            return InteractionResult.PASS;
        }

        if (config.enabled && (hand != InteractionHand.OFF_HAND || (hand == InteractionHand.OFF_HAND && config.offhand))) {
            if (PlacementFeature.executeReacharound(client, hand, itemStack)) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    public static void keybindToggle(Minecraft client) {
        while (Reacharound.getInstance().keyBindingToggle.consumeClick()) {
            config.enabled = !config.enabled;

            Component enabledText = Component.translatable(config.enabled ? "reacharound.config.indicator.enabled" : "reacharound.config.indicator.disabled");
            client.player.sendSystemMessage(Component.literal("Reacharound ").append(enabledText));
        }
    }

    public record ReacharoundTarget(BlockPos pos, Direction dir, InteractionHand hand) {
    }

    // Ray trace cache key
    private record RayTraceKey(Vec3 start, Vec3 end, ClipContext.Block shapeType,
                               ClipContext.Fluid fluidHandling) {

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            RayTraceKey that = (RayTraceKey) obj;
            return Objects.equals(start, that.start) &&
                    Objects.equals(end, that.end) &&
                    shapeType == that.shapeType &&
                    fluidHandling == that.fluidHandling;
        }
    }
}
