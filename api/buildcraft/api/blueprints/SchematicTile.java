/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import buildcraft.api.core.JavaTools;

public class SchematicTile extends SchematicBlock {

	/**
	 * This tree contains additional data to be stored in the blueprint. By
	 * default, it will be initialized from Schematic.readFromWord with
	 * the standard readNBT function of the corresponding tile (if any) and will
	 * be loaded from BptBlock.buildBlock using the standard writeNBT function.
	 */
	public NBTTagCompound cpt = new NBTTagCompound();

	@Override
	public void transformToBlueprint(MappingRegistry registry, Translation transform) {
		inventorySlotsToBlueprint(registry, cpt);
	}

	@Override
	public void transformToWorld(MappingRegistry registry, Translation transform) {
		inventorySlotsToWorld(registry, cpt);
	}

	/**
	 * Places the block in the world, at the location specified in the slot.
	 */
	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		super.writeToWorld(context, x, y, z, stacks);

		if (block.hasTileEntity(meta)) {
			TileEntity tile = context.world().getTileEntity(x, y, z);

			cpt.setInteger("x", x);
			cpt.setInteger("y", y);
			cpt.setInteger("z", z);

			if (tile != null) {
				tile.readFromNBT(cpt);
			}
		}
	}

	@Override
	public void readFromWorld(IBuilderContext context, int x, int y, int z) {
		super.readFromWorld(context, x, y, z);

		if (block.hasTileEntity(meta)) {
			TileEntity tile = context.world().getTileEntity(x, y, z);

			if (tile != null) {
				tile.writeToNBT(cpt);
			}
		}
	}

	@Override
	public void readRequirementsFromWorld(IBuilderContext context, int x, int y, int z) {
		super.readRequirementsFromWorld(context, x, y, z);

		if (block.hasTileEntity(meta)) {
			TileEntity tile = context.world().getTileEntity(x, y, z);

			if (tile instanceof IInventory) {
				IInventory inv = (IInventory) tile;

				ArrayList<ItemStack> rqs = new ArrayList<ItemStack>();

				for (int i = 0; i < inv.getSizeInventory(); ++i) {
					if (inv.getStackInSlot(i) != null) {
						rqs.add(inv.getStackInSlot(i));
					}
				}

				storedRequirements = JavaTools.concat(storedRequirements,
						rqs.toArray(new ItemStack[rqs.size()]));
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
