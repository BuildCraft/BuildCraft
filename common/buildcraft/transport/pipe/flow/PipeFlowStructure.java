/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeFlow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class PipeFlowStructure extends PipeFlow {
    public PipeFlowStructure(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowStructure(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowStructure;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return false;
    }
}
