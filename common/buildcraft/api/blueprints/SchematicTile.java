/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import net.minecraft.block.BlockContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class SchematicTile extends Schematic {

	/**
	 * This tree contains additional data to be stored in the blueprint. By
	 * default, it will be initialized from Schematic.readFromWord with
	 * the standard readNBT function of the corresponding tile (if any) and will
	 * be loaded from BptBlock.buildBlock using the standard writeNBT function.
	 */
	public NBTTagCompound cpt = new NBTTagCompound();

	@SuppressWarnings("unchecked")
	@Override
	public Schematic clone() {
		SchematicTile s = (SchematicTile) super.clone();

		s.cpt = (NBTTagCompound) cpt.copy();

		return s;
	}

	/**
	 * Places the block in the world, at the location specified in the slot.
	 */
	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z) {
		super.writeToWorld(context, x, y, z);

		if (block instanceof BlockContainer) {
			TileEntity tile = context.world().getTileEntity(x, y, z);

			cpt.setInteger("x", x);
			cpt.setInteger("y", y);
			cpt.setInteger("z", z);

			if (tile != null) {
				tile.readFromNBT(cpt);
			}

			// By default, clear the inventory to avoid possible dupe bugs
			if (tile instanceof IInventory) {
				IInventory inv = (IInventory) tile;

				for (int i = 0; i < inv.getSizeInventory(); ++i) {
					inv.setInventorySlotContents(i, null);
				}
			}
		}
	}

	/**
	 * Initializes a slot from the blueprint according to an objet placed on {x,
	 * y, z} on the world. This typically means adding entries in slot.cpt. Note
	 * that "id" and "meta" will be set automatically, corresponding to the
	 * block id and meta.
	 *
	 * By default, if the block is a BlockContainer, tile information will be to
	 * save / load the block.
	 */
	@Override
	public void readFromWorld(IBuilderContext context, int x, int y, int z) {
		super.readFromWorld(context, x, y, z);

		if (block instanceof BlockContainer) {
			TileEntity tile = context.world().getTileEntity(x, y, z);

			if (tile != null) {
				tile.writeToNBT(cpt);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		super.writeToNBT(nbt, registry);

		nbt.setTag("blockCpt", cpt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt,	MappingRegistry registry) {
		super.readFromNBT(nbt, registry);

		cpt = nbt.getCompoundTag("blockCpt");
	}
}
