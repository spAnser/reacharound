package com.spanser.reacharound.client.handler;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

public class RayTraceHandler {
    public static HitResult rayTrace(Entity entity, Level world, Vec3 startPos, Vec3 ray, ClipContext.Block blockMode, ClipContext.Fluid fluidMode) {
        Vec3 end = startPos.add(ray);
        ClipContext context = new ClipContext(startPos, end, blockMode, fluidMode, entity);
        return world.clip(context);
    }

    public static Pair<Vec3, Vec3> getEntityParams(Entity player) {
        // Get the player's current rotation
        float pitch = player.getXRot();
        float yaw = player.getYRot();

        // Calculate eye position
        Vec3 pos = player.position();
        double eyeHeight = player instanceof Player ? player.getEyeHeight(player.getPose()) : 0;
        Vec3 rayPos = new Vec3(pos.x, pos.y + eyeHeight, pos.z);

        // Calculate look direction vector
        float yawRad = yaw * (float) Math.PI / 180;
        float pitchRad = pitch * (float) Math.PI / 180;

        float horizontalFactor = -Mth.cos(pitchRad);
        float x = Mth.sin(yawRad) * horizontalFactor;
        float y = -Mth.sin(pitchRad);
        float z = -Mth.cos(yawRad) * horizontalFactor;

        Vec3 ray = new Vec3(x, y, z).normalize();

        return Pair.of(rayPos, ray);
    }
}
