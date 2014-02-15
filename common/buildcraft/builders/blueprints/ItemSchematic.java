/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import buildcraft.api.builder.BlockHandler;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

public final class ItemSchematic extends Schematic {

	public static ItemSchematic create(NBTTagCompound nbt) {
		return null;
	}

	public static ItemSchematic create(Item item) {
		return new ItemSchematic(item);
	}
	public Item item;

	/**
	 * For serializer use only
	 */
	public ItemSchematic() {

	}

	private ItemSchematic(Item item) {
		super(Item.getIdFromItem(item));
		this.item = item;
	}

	private ItemSchematic(String itemName) {
//		String blockName = nbt.getString("blockName");
		this((Item) null); // TODO: Add item from name code
	}

	@Override
	public BlockHandler getHandler() {
		return BlockHandler.get(item);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("schematicType", "item");
		nbt.setString("itemName", item.getUnlocalizedName());
	}
}
