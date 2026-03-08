/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.particle.ParticlePipes;
import buildcraft.lib.particle.ParticlePosition;
import net.minecraft.item.DyeColor;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ParticleUtil {
    public static void showChangeColour(World world, Vector3d pos, @Nullable DyeColor colour) {
        if (colour == null) {
            showWaterParticles(world, pos);
        }
    }

    private static void showWaterParticles(World world, Vector3d pos) {

    }

    public static void showTempPower(World world, BlockPos pos, Direction face, long microJoules) {
        double x = pos.getX() + 0.5 + face.getStepX() * 0.5;
        double y = pos.getY() + 0.5 + face.getStepY() * 0.5;
        double z = pos.getZ() + 0.5 + face.getStepZ() * 0.5;

        Vector3d startingMotion = Vector3d.atLowerCornerOf(face.getNormal());
        startingMotion = VecUtil.scale(startingMotion, 0.05);

        ParticlePosition nPos = new ParticlePosition(new Vector3d(x, y, z), startingMotion);

        for (ParticlePosition pp : ParticlePipes.DUPLICATE_SPREAD.pipe(nPos)) {
            world.addParticle(ParticleTypes.FLAME, x, y, z, pp.motion.x, pp.motion.y, pp.motion.z);
        }
    }
}
