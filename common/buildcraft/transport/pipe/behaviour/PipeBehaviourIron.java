/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class PipeBehaviourIron extends PipeBehaviourDirectional {
    public PipeBehaviourIron(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourIron(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return face == currentDir.face ? 0 : 1;
    }

    @Override
    protected boolean canFaceDirection(EnumFacing dir) {
        return pipe.isConnected(dir);
    }

    @PipeEventHandler
    public void sideCheck(PipeEventItem.SideCheck sideCheck) {
        if (currentDir == EnumPipePart.CENTER) {
            sideCheck.disallowAll();
        } else {
            sideCheck.disallowAllExcept(currentDir.face);
        }
    }

    @PipeEventHandler
    public void fluidSideCheck(PipeEventFluid.SideCheck sideCheck) {
        if (currentDir == EnumPipePart.CENTER) {
            sideCheck.disallowAll();
        } else {
            sideCheck.disallowAllExcept(currentDir.face);
        }
    }

    @PipeEventHandler
    public static void tryBounce(PipeEventItem.TryBounce tryBounce) {
        tryBounce.canBounce = true;
    }

    @PipeEventHandler
    public void fluidInsert(PipeEventFluid.TryInsert insert) {
        if (currentDir.face == insert.from) {
            insert.cancel();
        }
    }
}
