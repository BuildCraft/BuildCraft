/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.robots;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;

public abstract class ResourceId {

	public BlockIndex index = new BlockIndex();
	public ForgeDirection side = ForgeDirection.UNKNOWN;
	public int localId = 0;

	protected ResourceId() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}

		ResourceId compareId = (ResourceId) obj;

		return index.equals(compareId.index)
				&& side == compareId.side
				&& localId == compareId.localId;
	}

	@Override
	public int hashCode() {
		return (((index != null ? index.hashCode() : 0) * 37) + (side != null ? side.ordinal() : 0) * 37) + localId;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound indexNBT = new NBTTagCompound();
		index.writeTo(indexNBT);
		nbt.setTag("index", indexNBT);
		nbt.setByte("side", (byte) side.ordinal());
		nbt.setInteger("localId", localId);
		nbt.setString("resourceName", RobotManager.getResourceIdName(getClass()));
	}

	protected void readFromNBT(NBTTagCompound nbt) {
		index = new BlockIndex(nbt.getCompoundTag("index"));
		side = ForgeDirection.values()[nbt.getByte("side")];
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
