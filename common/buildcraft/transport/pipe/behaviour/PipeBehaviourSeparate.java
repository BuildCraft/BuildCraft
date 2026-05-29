/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

public abstract class PipeBehaviourSeparate extends PipeBehaviour {
    public PipeBehaviourSeparate(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourSeparate(IPipe pipe, NbtCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        if (other instanceof PipeBehaviourSeparate) {
            return other.getClass() == getClass();
        } else {
            return true;
        }
    }
}
