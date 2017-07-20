/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.particle;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public enum ParticleDirectionalSpread implements IParticlePositionPipe {
    SMALL(0.01),
    MEDIUM(0.02),
    LARGE(0.04),
    MASSIVE(0.08);

    private final double motionDiff;

    ParticleDirectionalSpread(double motionDiff) {
        this.motionDiff = motionDiff;
    }

    @Override
    public List<ParticlePosition> pipe(ParticlePosition pos) {
        List<ParticlePosition> list = new ArrayList<>();

        Vec3d nMotion = modifyMotion(pos.motion);
        list.add(new ParticlePosition(pos.position, nMotion));

        return list;
    }

    private Vec3d modifyMotion(Vec3d motion) {
        double dx = getRandom();
        double dy = getRandom();
        double dz = getRandom();
        return motion.addVector(dx, dy, dz);
    }

    private double getRandom() {
        return (Math.random() - 0.5) * motionDiff;
    }
}
