package com.spanser.reacharound.client.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public class RayTraceHandler {
    public static HitResult rayTrace(Entity entity, World world, Vec3d startPos, Vec3d ray, RaycastContext.ShapeType blockMode, RaycastContext.FluidHandling fluidMode) {
        Vec3d end = startPos.add(ray);
        RaycastContext context = new RaycastContext(startPos, end, blockMode, fluidMode, entity);
        return world.raycast(context);
    }

    public static Pair<Vec3d, Vec3d> getEntityParams(Entity player) {
        // Get the player's current rotation
        float pitch = player.getPitch();
        float yaw = player.getYaw();
        
        // Calculate eye position
        Vec3d pos = player.getPos();
        double eyeHeight = player instanceof PlayerEntity ? player.getEyeHeight(player.getPose()) : 0;
        Vec3d rayPos = new Vec3d(pos.x, pos.y + eyeHeight, pos.z);

        // Calculate look direction vector
        float yawRad = yaw * (float) Math.PI / 180;
        float pitchRad = pitch * (float) Math.PI / 180;
        
        float horizontalFactor = -MathHelper.cos(pitchRad);
        float x = MathHelper.sin(yawRad) * horizontalFactor;
        float y = -MathHelper.sin(pitchRad);
        float z = -MathHelper.cos(yawRad) * horizontalFactor;
        
        Vec3d ray = new Vec3d(x, y, z).normalize();

        return Pair.of(rayPos, ray);
    }

}