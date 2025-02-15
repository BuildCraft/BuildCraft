/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import java.util.ArrayList;
import java.util.List;

public enum ParticleCountMultiplier implements IParticlePositionPipe {
    MINIMAL(2),
    DECREASED(7),
    ALL(13);

    public static ParticleCountMultiplier getForOption() {
        Options gs = Minecraft.getInstance().options;
        int count = gs.particles().get().getId() % 3;
        if (count == 0) {
            return ALL;
        } else if (count == 1) {
            return DECREASED;
        }
        return MINIMAL;
    }

    public static IParticlePositionPipe getOptionProvider() {
        return pos -> getForOption().pipe(pos);
    }

    private final int numExpanses;

    ParticleCountMultiplier(int numExpanses) {
        this.numExpanses = numExpanses;
    }

    @Override
    public List<ParticlePosition> pipe(ParticlePosition pos) {
        List<ParticlePosition> list = new ArrayList<>();

        for (int i = 0; i < numExpanses; i++) {
            list.add(new ParticlePosition(pos.position, pos.motion));
        }

        return list;
    }
}
