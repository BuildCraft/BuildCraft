/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.particle;

import net.minecraft.util.math.Vec3d;

public class ParticlePosition {
    public final Vec3d position, motion;

    public ParticlePosition(Vec3d position, Vec3d motion) {
        this.position = position;
        this.motion = motion;
    }
}
