/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import ct.buildcraft.api.mj.IMjReceiver;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PipeBehaviourWoodPower extends PipeBehaviour {

    public PipeBehaviourWoodPower(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWoodPower(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWoodPower);
    }

    @Override
    public int getTextureIndex(Direction face) {
        if (face == null) {
            return 0;
        }
        if (pipe.getConnectedPipe(face) != null) {
            return 0;
        }
        BlockEntity tile = pipe.getConnectedTile(face);
        if (tile == null) {
            return 0;
        }
        IMjReceiver recv = tile.getCapability(MjAPI.CAP_RECEIVER, face.getOpposite()).orElse(null);
        return recv == null ? 1 : recv.canReceive() ? 0 : 1;
    }
}
