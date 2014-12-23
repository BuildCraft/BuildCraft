/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.robots;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import buildcraft.core.utils.Utils;

public abstract class ResourceId {

	public BlockPos index;
	public EnumFacing side;
	public int localId = 0;

	protected ResourceId() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != getClass()) {
			return false;
		}

		ResourceId compareId = (ResourceId) obj;

		return index.equals(compareId.index)
				&& side == compareId.side
				&& localId == compareId.localId;
	}

	@Override
	public int hashCode() {
		return ((index.hashCode() * 37) + side.ordinal() * 37) + localId;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound indexNBT = new NBTTagCompound();
		Utils.writeBlockPos(nbt, index);
		nbt.setTag("index", indexNBT);
		nbt.setByte("side", (byte) side.ordinal());
		nbt.setInteger("localId", localId);
		nbt.setString("class", getClass().getCanonicalName());
	}

	protected void readFromNBT(NBTTagCompound nbt) {
		index = Utils.readBlockPos(nbt.getCompoundTag("index"));
		side = EnumFacing.values()[nbt.getByte("side")];
		localId = nbt.getInteger("localId");
	}

	public static ResourceId load(NBTTagCompound nbt) {
		try {
			Class clas = Class.forName(nbt.getString("class"));

			ResourceId id = (ResourceId) clas.newInstance();
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
