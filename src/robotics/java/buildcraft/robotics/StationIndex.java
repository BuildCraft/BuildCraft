/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.robots.DockingStation;

public class StationIndex {

    public BlockPos index = new BlockPos();
    public EnumFacing side = null;

    protected StationIndex() {}

    public StationIndex(EnumFacing iSide, BlockPos pos) {
        side = iSide;
        index = new BlockPos(pos);
    }

    public StationIndex(DockingStation station) {
        side = station.side();
        index = station.index();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != getClass()) {
            return false;
        }

        StationIndex compareId = (StationIndex) obj;

        return index.equals(compareId.index) && side == compareId.side;
    }

    @Override
    public int hashCode() {
        return (index.hashCode() * 37) + side.ordinal();
    }

    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagCompound indexNBT = new NBTTagCompound();
        index.writeTo(indexNBT);
        nbt.setTag("index", indexNBT);
        nbt.setByte("side", (byte) side.ordinal());
    }

    protected void readFromNBT(NBTTagCompound nbt) {
        index = new BlockPos(nbt.getCompoundTag("index"));
        side = EnumFacing.values()[nbt.getByte("side")];
    }
}
