/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.particle;

import net.minecraft.world.phys.Vec3;

public class ParticlePosition {
    public final Vec3 position, motion;

    public ParticlePosition(Vec3 position, Vec3 motion) {
        this.position = position;
        this.motion = motion;
    }
}
