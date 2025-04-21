/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics;

import buildcraft.api.robots.DockingStation;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class StationIndex {

    public BlockPos index = new BlockPos(0, 0, 0);
    public Direction side = null;

    protected StationIndex() {}

    public StationIndex(Direction iSide, BlockPos pos) {
        side = iSide;
        index = new BlockPos(pos);
    }

    public StationIndex(DockingStation station) {
        side = station.side();
        index = station.index();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        StationIndex compareId = (StationIndex) obj;

        return index.equals(compareId.index) && side == compareId.side;
    }

    @Override
    public int hashCode() {
        return (index.hashCode() * 37) + side.ordinal();
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.put("index", NBTUtilBC.writeBlockPos(index));// TODO: IS THIS RIGHT????
        nbt.putByte("side", (byte) side.ordinal());
    }

    protected void readFromNBT(CompoundTag nbt) {
        index = NBTUtilBC.readBlockPos(nbt.get("index"));
        side = Direction.values()[nbt.getByte("side")];
    }
}
