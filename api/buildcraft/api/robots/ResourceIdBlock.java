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
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;

public class ResourceIdBlock extends ResourceId {

	public BlockIndex index = new BlockIndex();
	public ForgeDirection side = ForgeDirection.UNKNOWN;

	public ResourceIdBlock() {

	}

	public ResourceIdBlock(int x, int y, int z) {
		index = new BlockIndex(x, y, z);
	}

	public ResourceIdBlock(BlockIndex iIndex) {
		index = iIndex;
	}

	public ResourceIdBlock(TileEntity tile) {
		index = new BlockIndex(tile);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}

		ResourceIdBlock compareId = (ResourceIdBlock) obj;

		return index.equals(compareId.index) && side == compareId.side;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(index.hashCode()).append(side != null ? side.ordinal() : 0).build();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagCompound indexNBT = new NBTTagCompound();
		index.writeTo(indexNBT);
		nbt.setTag("index", indexNBT);
		nbt.setByte("side", (byte) side.ordinal());
	}

	@Override
	protected void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		index = new BlockIndex(nbt.getCompoundTag("index"));
		side = ForgeDirection.values()[nbt.getByte("side")];
	}
}
