/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.core.utils.NBTUtils;

public class ItemConstructionMarker extends ItemBlock {

	public ItemConstructionMarker(Block block) {
		super(block);
	}

	public static void link(ItemStack marker, World world, int x, int y, int z) {
		NBTTagCompound nbt = NBTUtils.getItemData(marker);

		if (nbt.hasKey("x")) {
			int ox = nbt.getInteger("x");
			int oy = nbt.getInteger("y");
			int oz = nbt.getInteger("z");

			TileEntity tile1 = world.getTileEntity(ox, oy, oz);

			if (tile1 != null && (tile1 instanceof TileArchitect)) {
				TileArchitect architect = (TileArchitect) tile1;
				TileEntity tile2 = world.getTileEntity(x, y, z);

				if (tile1 != tile2 && tile2 != null && (tile2 instanceof TileArchitect)) {
					architect.addSubBlueprint(tile2);

					nbt.removeTag("x");
					nbt.removeTag("y");
					nbt.removeTag("z");
				}

				return;
			}
		}

		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
	}
}
