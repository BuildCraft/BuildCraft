/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.robots;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.BCLog;

public abstract class ResourceId {
    public BlockPos pos = BlockPos.ORIGIN;
    public EnumFacing side = null;
    public int localId = 0;

    protected ResourceId() {}

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != getClass()) {
            return false;
        }

        ResourceId compareId = (ResourceId) obj;

        return pos.equals(compareId.pos) && side == compareId.side && localId == compareId.localId;
    }

    @Override
    public int hashCode() {
        return (((pos != null ? pos.hashCode() : 0) * 37) + (side != null ? side.ordinal() : 6) * 37) + localId;
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.setByte("side", (byte) (side == null ? 6 : side.ordinal()));
        nbt.setInteger("localId", localId);
        nbt.setString("resourceName", RobotManager.getResourceIdName(getClass()));
    }

    protected void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("index")) {
            // For compatibility with older versions of minecraft and buildcraft
            NBTTagCompound indexNBT = nbt.getCompoundTag("index");
            int x = indexNBT.getInteger("i");
            int y = indexNBT.getInteger("j");
            int z = indexNBT.getInteger("k");
            pos = new BlockPos(pos);
        } else {
            int[] array = nbt.getIntArray("pos");
            if (array.length == 3) {
                pos = new BlockPos(array[0], array[1], array[2]);
            } else if (array.length != 0) {
                BCLog.logger.warn("Found an integer array that wwas not the right length! (" + array + ")");
            } else {
                BCLog.logger.warn("Did not find any integer positions! This is a bug!");
            }
        }
        byte sid = nbt.getByte("side");
        side = sid == 6 ? null : EnumFacing.values()[sid];
        localId = nbt.getInteger("localId");
    }

    public static ResourceId load(NBTTagCompound nbt) {
        try {
            Class cls = null;
            if (nbt.hasKey("class")) {
                // Migration support for 6.4.x
                cls = RobotManager.getResourceIdByLegacyClassName(nbt.getString("class"));
            } else {
                cls = RobotManager.getResourceIdByName(nbt.getString("resourceName"));
            }

            ResourceId id = (ResourceId) cls.newInstance();
            id.readFromNBT(nbt);

            return id;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    public void taken(long robotId) {

    }

    public void released(long robotId) {

    }
}
