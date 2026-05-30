/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.behaviour;

// STUB(R.Chen): PipeBehaviourObsidian — heavy Forge deps: MjCapabilityHelper (Forge capability system),
// ItemTransactorHelper (not migrated), IMjRedstoneReceiver. Restoring in Phase 4F when cap layer lands.

import javax.annotation.Nonnull;

import net.minecraft.nbt.NbtCompound;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

public class PipeBehaviourObsidian extends PipeBehaviour implements IMjRedstoneReceiver {

    public PipeBehaviourObsidian(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourObsidian(IPipe pipe, NbtCompound nbt) {
        super(pipe, nbt);
    }

    // IMjRedstoneReceiver stubs

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return 0;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return microJoules;
    }
}
