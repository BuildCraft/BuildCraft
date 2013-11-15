/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import java.util.UUID;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class NBTUtils {

	public static NBTTagCompound getItemData(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound("tag");
			stack.setTagCompound(nbt);
		}
		return nbt;
	}

	public static void writeUUID(NBTTagCompound data, String tag, UUID uuid) {
		if (uuid == null)
			return;
		NBTTagCompound nbtTag = new NBTTagCompound();
		nbtTag.setLong("most", uuid.getMostSignificantBits());
		nbtTag.setLong("least", uuid.getLeastSignificantBits());
		data.setTag(tag, nbtTag);
	}

	public static UUID readUUID(NBTTagCompound data, String tag) {
		if (data.hasKey(tag)) {
			NBTTagCompound nbtTag = data.getCompoundTag(tag);
			return new UUID(nbtTag.getLong("most"), nbtTag.getLong("least"));
		}
		return null;
	}
}
