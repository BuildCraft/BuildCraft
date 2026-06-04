/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import javax.annotation.Nullable;

import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.lib.particle.ParticlePipes;
import buildcraft.lib.particle.ParticlePosition;

public class ParticleUtil {
    public static void showChangeColour(World world, Vec3d pos, @Nullable DyeColor colour) {
        if (colour == null) {
            showWaterParticles(world, pos);
        }
    }

    private static void showWaterParticles(World world, Vec3d pos) {

    }

    public static void showTempPower(World world, BlockPos pos, Direction face, long microJoules) {
        double x = pos.getX() + 0.5 + face.getOffsetX() * 0.5;
        double y = pos.getY() + 0.5 + face.getOffsetY() * 0.5;
        double z = pos.getZ() + 0.5 + face.getOffsetZ() * 0.5;

        Vec3d startingMotion = new Vec3d(face.getVector().getX(), face.getVector().getY(), face.getVector().getZ());
        startingMotion = VecUtil.multiply(startingMotion, 0.05);

        ParticlePosition nPos = new ParticlePosition(new Vec3d(x, y, z), startingMotion);

        for (ParticlePosition pp : ParticlePipes.DUPLICATE_SPREAD.pipe(nPos)) {
            // STUB: spawnParticle via server world
            if (world instanceof net.minecraft.server.world.ServerWorld) {
                ((net.minecraft.server.world.ServerWorld) world).spawnParticles(ParticleTypes.FLAME, x, y, z, 1, pp.motion.x, pp.motion.y, pp.motion.z, 0.0);
            }
        }
    }
}
