/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import buildcraft.api.builder.BlockHandler;
import buildcraft.core.network.NetworkData;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public abstract class Schematic {

	@NetworkData
	public int id;

	@NetworkData
	public int x;

	@NetworkData
	public int y;

	@NetworkData
	public int z;

	@NetworkData
	public NBTTagCompound data = new NBTTagCompound();

	/**
	 * Only to be class by the serializer
	 */
	public Schematic() {

	}

	protected Schematic(int id) {
		this.id = id;
	}

	public abstract BlockHandler getHandler();

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		nbt.setCompoundTag("data", data);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		x = nbt.getInteger("x");
		y = nbt.getInteger("y");
		z = nbt.getInteger("z");
		data = nbt.getCompoundTag("data");
	}

	public static Schematic createSchematicFromNBT(NBTTagCompound nbt) {
		String schematicType = nbt.getString("schematicType");
		Schematic schematic;
		if (schematicType.equals("block")) {
			schematic = BlockSchematic.create(nbt);
		} else if (schematicType.equals("item")) {
			schematic = ItemSchematic.create(nbt);
		} else {
			return null;
		}
		schematic.x = nbt.getInteger("x");
		schematic.y = nbt.getInteger("y");
		schematic.z = nbt.getInteger("z");
		schematic.data = nbt.getCompoundTag("userData");
		return schematic;
	}
}
