/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.utils;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

public final class NBTUtils {

	/**
	 * Deactivate constructor
	 */
	private NBTUtils() {

	}

	public static NBTBase getTag(NBTTagCompound cptBase, String nameBase) {
		NBTTagCompound cpt = cptBase;
		String name = nameBase;

		while (cpt != null && name.contains("/")) {
			String cptName = name.substring(0, name.indexOf("/"));
			if (cpt.hasKey(cptName, 10)) {
				cpt = cpt.getCompoundTag(cptName);
				name = name.substring(name.indexOf("/") + 1);
			}
		}

		return cpt != null ? cpt.getTag(name) : null;
	}

	public static void setTag(NBTTagCompound cptBase, String nameBase, NBTBase tag) {
		NBTTagCompound cpt = cptBase;
		String name = nameBase;

		while (cpt != null && name.contains("/")) {
			String cptName = name.substring(0, name.indexOf("/"));
			if (cpt.hasKey(cptName, 10)) {
				cpt = cpt.getCompoundTag(cptName);
				name = name.substring(name.indexOf("/") + 1);
			}
		}

		if (cpt != null) {
			cpt.setTag(name, tag);
		}
	}

	public static NBTTagCompound load(byte[] data) {
		try {
			NBTTagCompound nbt = CompressedStreamTools.func_152457_a(data, NBTSizeTracker.field_152451_a);
			return nbt;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static NBTTagCompound getItemData(ItemStack stack) {
		if (stack == null) {
			return null;
		}
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		return nbt;
	}

	public static void writeUUID(NBTTagCompound data, String tag, UUID uuid) {
		if (uuid == null) {
			return;
		}
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

	public static byte[] save(NBTTagCompound compound) {
		try {
			return CompressedStreamTools.compress(compound);
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}
}
