/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.behaviour;

// STUB(R.Chen): PipeBehaviourWoodPower — Forge CapabilityEnergy/IEnergyStorage not available.
// getTextureIndex always returns 0 (stub); restore in Phase 4E RF layer.

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

public class PipeBehaviourWoodPower extends PipeBehaviour {

    public PipeBehaviourWoodPower(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWoodPower(IPipe pipe, NbtCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWoodPower);
    }

    @Override
    public int getTextureIndex(Direction face) {
        // STUB(R.Chen): Forge CapabilityEnergy/MjAPI.CAP_RECEIVER not available — always returns 0.
        return 0;
    }
}
