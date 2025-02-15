/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.particle.ParticlePipes;
import buildcraft.lib.particle.ParticlePosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class ParticleUtil {
    public static void showChangeColour(Level world, Vec3 pos, @Nullable DyeColor colour) {
        if (colour == null) {
            showWaterParticles(world, pos);
        }
    }

    private static void showWaterParticles(Level world, Vec3 pos) {

    }

    public static void showTempPower(Level world, BlockPos pos, Direction face, long microJoules) {
        double x = pos.getX() + 0.5 + face.getStepX() * 0.5;
        double y = pos.getY() + 0.5 + face.getStepY() * 0.5;
        double z = pos.getZ() + 0.5 + face.getStepZ() * 0.5;

        Vec3 startingMotion = Vec3.atLowerCornerOf(face.getNormal());
        startingMotion = VecUtil.scale(startingMotion, 0.05);

        ParticlePosition nPos = new ParticlePosition(new Vec3(x, y, z), startingMotion);

        for (ParticlePosition pp : ParticlePipes.DUPLICATE_SPREAD.pipe(nPos)) {
            world.addParticle(ParticleTypes.FLAME, x, y, z, pp.motion.x, pp.motion.y, pp.motion.z);
        }
    }
}
