/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.bpt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

public abstract class BlueprintBase {
    public BlockPos size;
    /** The offset of the builder/architect relative to the origin of this blueprint (so, this is a vector min to
     * builder) */
    public BlockPos offset;
    public EnumFacing facing = EnumFacing.EAST;

    public BlueprintBase(BlockPos size, BlockPos offset) {
        this.size = size;
        this.offset = offset;
    }

    public BlueprintBase(NBTTagCompound nbt) {
        size = NBTUtilBC.readBlockPos(nbt.getTag("size"));
        if (size == null) {
            size = new BlockPos(0, 0, 0);
        }
        offset = NBTUtilBC.readBlockPos(nbt.getTag("offset"));
        if (offset == null) {
            offset = BlockPos.ORIGIN;
        }
        facing = NBTUtilBC.readEnum(nbt.getTag("facing"), EnumFacing.class);
        if (facing == null) {
            facing = EnumFacing.EAST;
        }
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("size", NBTUtilBC.writeBlockPos(size));
        nbt.setTag("offset", NBTUtilBC.writeBlockPos(offset));
        nbt.setTag("facing", NBTUtilBC.writeEnum(facing));
        return nbt;
    }

    protected abstract void rotateContentsBy(Axis axis, Rotation rotation);

    public final void rotate(Axis axis, Rotation rotation) {
        if (rotation == Rotation.NONE || rotation == null) {
            return;
        }
        rotateContentsBy(axis, rotation);

        BlockPos oldSize = size;

        size = VecUtil.absolute(PositionUtil.rotatePos(size, axis, rotation));
        offset = PositionUtil.rotatePos(offset, axis, rotation);

        Box to = new Box(BlockPos.ORIGIN, size.add(-1, -1, -1));
        BlockPos newMax = PositionUtil.rotatePos(oldSize.add(-1, -1, -1), axis, rotation);
        BlockPos arrayOffset = to.closestInsideTo(newMax).subtract(newMax);
        offset = offset.subtract(arrayOffset);
    }

    public abstract void mirror(Axis axis);
}
