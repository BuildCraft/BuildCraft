/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.WorldBlockIndex;

public class StackRequest {

	public static final int NULL_LIFETIME = 20 * 60;

	public WorldBlockIndex holder;
	public int indexInHolder;
	public ItemStack stack;
	public long requestDate;
	public long loadDate;
	public boolean fulfilled;

	/**
	 * when loading from NBT, this field may be null. The requested is supposed
	 * to notify the requester upon loading. If fail to do so for more than
	 * NULL_LIFETIME cycles, then the request is forgotten
	 */
	public Entity requester;

	public void saveToNBT(NBTTagCompound nbt) {
		NBTTagCompound index = new NBTTagCompound();
		holder.writeTo(index);
		nbt.setTag("index", index);

		nbt.setInteger("indexInHolder", indexInHolder);
	}

	public void loadFromNBT(NBTTagCompound nbt) {
		holder = new WorldBlockIndex(nbt.getCompoundTag("index"));
		indexInHolder = nbt.getInteger("indexInHolder");
	}
}

