/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;

public class ResourceIdRequest extends ResourceId {

	private BlockIndex index;
	private ForgeDirection side;
	private int slot;

	public ResourceIdRequest() {

	}

	public ResourceIdRequest(DockingStation station, int slot) {
		index = station.index();
		side = station.side();
		this.slot = slot;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}

		ResourceIdRequest compareId = (ResourceIdRequest) obj;

		return index.equals(compareId.index) && side.equals(compareId.side) && slot == compareId.slot;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(index.hashCode()).append(side.hashCode()).append(slot).build();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagCompound indexNBT = new NBTTagCompound();
		index.writeTo(indexNBT);
		nbt.setTag("index", indexNBT);
		nbt.setByte("side", (byte) side.ordinal());
		nbt.setInteger("localId", slot);
	}

	@Override
	protected void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		index = new BlockIndex(nbt.getCompoundTag("index"));
		side = ForgeDirection.getOrientation(nbt.getByte("side"));
		slot = nbt.getInteger("localId");
	}
}
