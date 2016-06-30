/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.bpt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;

public abstract class BlueprintBase {
    public BlockPos size;

    public BlueprintBase(BlockPos size) {
        this.size = size;
    }

    public BlueprintBase(NBTTagCompound nbt) {
        this.size = NBTUtils.readBlockPos(nbt.getTag("size"));
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("size", NBTUtils.writeBlockPos(size));
        return nbt;
    }

    protected abstract void rotateContentsBy(Axis axis, Rotation rotation);

    public final void rotate(Axis axis, Rotation rotation) {
        rotateContentsBy(axis, rotation);
        size = VecUtil.absolute(PositionUtil.rotatePos(size, axis, rotation));
    }

    public abstract void mirror(Axis axis);
}
