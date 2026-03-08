/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class PipeBehaviourSandstone extends PipeBehaviour {
    private static final double SPEED_DELTA = PipeBehaviourStone.SPEED_DELTA;
    private static final double SPEED_TARGET = PipeBehaviourStone.SPEED_TARGET;

    public PipeBehaviourSandstone(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourSandstone(IPipe pipe, CompoundNBT nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        return true;
    }

    @Override
    public boolean canConnect(Direction face, TileEntity oTile) {
        return false;
    }

    @PipeEventHandler
    public static void modifySpeed(PipeEventItem.ModifySpeed event) {
        event.modifyTo(SPEED_TARGET, SPEED_DELTA);
    }
}
