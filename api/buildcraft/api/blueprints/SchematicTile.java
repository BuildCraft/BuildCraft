/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
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
	 * default, it will be initialized from Schematic.readFromWord with the
	 * standard readNBT function of the corresponding tile (if any) and will be
	 * loaded from BptBlock.writeToWorld using the standard writeNBT function.
	 */
	public NBTTagCompound tileNBT = new NBTTagCompound();


	@Override
	public void idsToBlueprint(MappingRegistry registry) {
		registry.scanAndTranslateStacksToRegistry(tileNBT);
	}

	@Override
	public void idsToWorld(MappingRegistry registry) {
		try {
			registry.scanAndTranslateStacksToWorld(tileNBT);
		} catch (MappingNotFoundException e) {
			tileNBT = new NBTTagCompound();
		}
	}

	public void onNBTLoaded() {

	}

	/**
	 * Places the block in the world, at the location specified in the slot.
	 */
	@Override
	public void placeInWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		super.placeInWorld(context, x, y, z, stacks);

		if (block.hasTileEntity(meta)) {
			tileNBT.setInteger("x", x);
			tileNBT.setInteger("y", y);
			tileNBT.setInteger("z", z);

			context.world().setTileEntity(x, y, z, TileEntity.createAndLoadEntity(tileNBT));
		}
	}

	@Override
	public void initializeFromObjectAt(IBuilderContext context, int x, int y, int z) {
		super.initializeFromObjectAt(context, x, y, z);

		if (block.hasTileEntity(meta)) {
			TileEntity tile = context.world().getTileEntity(x, y, z);

			if (tile != null) {
				tile.writeToNBT(tileNBT);
			}

			tileNBT = (NBTTagCompound) tileNBT.copy();
			onNBTLoaded();
		}
	}

	@Override
	public void storeRequirements(IBuilderContext context, int x, int y, int z) {
		super.storeRequirements(context, x, y, z);

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
	public void writeSchematicToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		super.writeSchematicToNBT(nbt, registry);

		nbt.setTag("blockCpt", tileNBT);
	}

	@Override
	public void readSchematicFromNBT(NBTTagCompound nbt,	MappingRegistry registry) {
		super.readSchematicFromNBT(nbt, registry);

		tileNBT = nbt.getCompoundTag("blockCpt");
		onNBTLoaded();
	}

	@Override
	public int buildTime() {
		return 5;
	}
}
