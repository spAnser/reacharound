package com.spanser.reacharound.client.feature;

import com.spanser.reacharound.Reacharound;
import com.spanser.reacharound.client.handler.RayTraceHandler;
import com.spanser.reacharound.config.ReacharoundConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

public class PlacementFeature {
    private static final MinecraftClient client = MinecraftClient.getInstance();
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
    private static HitResult cachedRayTrace(Entity entity, World world, Vec3d startPos, Vec3d ray, RaycastContext.ShapeType blockMode, RaycastContext.FluidHandling fluidMode) {
        Vec3d endPos = startPos.add(ray);
        RayTraceKey key = new RayTraceKey(startPos, endPos, blockMode, fluidMode);

        return rayTraceCache.computeIfAbsent(key, k -> {
            RaycastContext context = new RaycastContext(startPos, endPos, blockMode, fluidMode, entity);
            return world.raycast(context);
        });
    }

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

            ReacharoundTarget target = getCurrentTarget();
            block = player.getEntityWorld().getBlockState(target.pos().add(0, isLookingDown ? 1 : -1, 0));
            if (isLookingDown && blockIsTopSlab(block)) {
                setCurrentTarget(new ReacharoundTarget(target.pos().add(0, 1, 0), target.dir(), target.hand()));
            } else if (!isLookingDown && blockIsBottomSlab(block)) {
                setCurrentTarget(new ReacharoundTarget(target.pos().add(0, -1, 0), target.dir(), target.hand()));
            }
        } else {
            Vec3i facing = player.getHorizontalFacing().getVector();
            block = player.getEntityWorld().getBlockState(getCurrentTarget().pos().add(-facing.getX(), 0, -facing.getZ()));
        }

        return block;
    }

    public static boolean canPlace(ClientPlayerEntity player) {
        BlockState block = getPlacement(player);
        ReacharoundTarget target = getCurrentTarget();
        return target != null && player.getEntityWorld().canPlace(block, target.pos(), ShapeContext.absent());
    }

    public static boolean executeReacharound(MinecraftClient client, Hand hand, ItemStack itemStack) {
        ReacharoundTarget target = getCurrentTarget();
        if (target != null) {
            BlockHitResult blockHitResult;

            int x = target.pos().getX();
            int y = target.pos().getY();
            int z = target.pos().getZ();
            Vec3d source = new Vec3d(x, y, z);

            boolean isLookingDown = client.player.getPitch() > 0;

            BlockState block = getPlacement(client.player);

            if (blockIsTopSlab(block)) {
                source = source.add(0, 1, 0);
            }

            Direction direction;
            if (isVertical()) {
                direction = Direction.fromVector(0, isLookingDown ? -1 : 1, 0, Direction.UP);
            } else {
                Vec3i facing = client.player.getHorizontalFacing().getVector();
                direction = Direction.fromVector(-facing.getX(), 0, -facing.getZ(), Direction.NORTH);
            }

            blockHitResult = new BlockHitResult(source, direction, target.pos(), false);

            int count = itemStack.getCount();
            ActionResult result = client.interactionManager.interactBlock(client.player, hand, blockHitResult);
            if (result.isAccepted()) {
                client.player.swingHand(hand);
                if (!itemStack.isEmpty() && (itemStack.getCount() != count || client.player.isInCreativeMode())) {
                    client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
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

        World world = player.getEntityWorld();

        Pair<Vec3d, Vec3d> params = RayTraceHandler.getEntityParams(player);
        double range = player.getBlockInteractionRange() - RANGE_ADJUSTMENT;
        Vec3d rayPos = params.getLeft().add(params.getRight().multiply(0.5f));
        Vec3d ray = params.getRight().multiply(range);

        HitResult normalRes = cachedRayTrace(player, world, rayPos, ray, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE);

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

    private static ReacharoundTarget getPlayerVerticalReacharoundTarget(Entity player, Hand hand, World world, Vec3d rayPos, Vec3d ray) {
        boolean isLookingDown = player.getPitch() > 0;
        if (isLookingDown) {
            rayPos = rayPos.add(0, LENIENCY_VERTICAL, 0);
        } else {
            rayPos = rayPos.add(0, -LENIENCY_VERTICAL, 0);
        }
        HitResult take2Res = cachedRayTrace(player, world, rayPos, ray, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE);

        if (take2Res.getType() == HitResult.Type.BLOCK && take2Res instanceof BlockHitResult blockHitResult) {
            BlockPos pos = blockHitResult.getBlockPos();
            if (isLookingDown) {
                pos = pos.down();
            } else {
                pos = pos.up();
            }
            BlockState state = world.getBlockState(pos);

            double distance = pos.getY() - player.getEntityPos().y;
            if (isLookingDown) {
                distance = -distance;
            }

            // Check world height limits
            if (pos.getY() < world.getBottomY() || pos.getY() >= world.getTopYInclusive()) {
                return null;
            }

            if (distance > MIN_VERTICAL_DISTANCE && (state.isAir() || state.isReplaceable()))
                return new ReacharoundTarget(pos, isLookingDown ? Direction.DOWN : Direction.UP, hand);
        }

        return null;
    }

    private static ReacharoundTarget getPlayerHorizontalReacharoundTarget(Entity player, Hand hand, World world, Vec3d rayPos, Vec3d ray) {
        Direction dir = Direction.fromHorizontalDegrees(player.getYaw());
        rayPos = rayPos.subtract(LENIENCY_HORIZONTAL * dir.getOffsetX(), 0, LENIENCY_HORIZONTAL * dir.getOffsetZ());
        HitResult take2Res = cachedRayTrace(player, world, rayPos, ray, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE);

        if (take2Res.getType() == HitResult.Type.BLOCK && take2Res instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos().offset(dir);

            // Check world height limits
            if (pos.getY() < world.getBottomY() || pos.getY() >= world.getTopYInclusive()) {
                return null;
            }

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

    public static boolean isVertical() {
        ReacharoundTarget target = getCurrentTarget();
        return target != null && target.dir().getAxis() == Direction.Axis.Y;
    }

    public static boolean canReachAround(MinecraftClient client) {
        ReacharoundTarget target = getCurrentTarget();
        return config.enabled &&
                target != null &&
                (target.hand() != Hand.OFF_HAND || (target.hand() == Hand.OFF_HAND && config.offhand)) &&
                client.player != null &&
                client.world != null &&
                client.crosshairTarget != null;
    }

    public static void tick(MinecraftClient client) {
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

    public static ActionResult useItem(PlayerEntity player, World world, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (!world.isClient()) {
            return ActionResult.PASS;
        }

        if (config.enabled && (hand != Hand.OFF_HAND || (hand == Hand.OFF_HAND && config.offhand))) {
            if (PlacementFeature.executeReacharound(client, hand, itemStack)) {
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    public static void keybindToggle(MinecraftClient client) {
        while (Reacharound.getInstance().keyBindingToggle.wasPressed()) {
            config.enabled = !config.enabled;

            Text enabledText = Text.translatable(config.enabled ? "reacharound.config.indicator.enabled" : "reacharound.config.indicator.disabled");
            client.player.sendMessage(Text.literal("Reacharound ").append(enabledText), false);
        }
    }

    public record ReacharoundTarget(BlockPos pos, Direction dir, Hand hand) {
    }

    // Ray trace cache key
    private record RayTraceKey(Vec3d start, Vec3d end, RaycastContext.ShapeType shapeType,
                               RaycastContext.FluidHandling fluidHandling) {

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
