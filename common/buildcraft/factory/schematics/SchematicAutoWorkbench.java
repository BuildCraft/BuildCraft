/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.schematics;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftFactory;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.JavaTools;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.factory.TileAutoWorkbench;

public class SchematicAutoWorkbench extends SchematicTile {

	@Override
	public void storeRequirements(IBuilderContext context, int x, int y, int z) {
		TileAutoWorkbench autoWb = getTile(context, x, y, z);
		if (autoWb != null) {
			ArrayList<ItemStack> rqs = new ArrayList<ItemStack>();
			rqs.add(new ItemStack(BuildCraftFactory.autoWorkbenchBlock));

			for (IInvSlot slot : InventoryIterator.getIterable(autoWb.craftMatrix, ForgeDirection.UP)) {
				ItemStack stack = slot.getStackInSlot();
				if (stack != null) {
					stack = stack.copy();
					stack.stackSize = 1;
					rqs.add(stack);
				}
			}

			storedRequirements = JavaTools.concat(storedRequirements, rqs
					.toArray(new ItemStack[rqs.size()]));
		}
	}

	@Override
	public void initializeFromObjectAt(IBuilderContext context, int x, int y, int z) {
		super.initializeFromObjectAt(context, x, y, z);

		tileNBT.removeTag("Items");
	}

	@Override
	public void placeInWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		super.placeInWorld(context, x, y, z, stacks);

		TileAutoWorkbench autoWb = getTile(context, x, y, z);
		if (autoWb != null) {
			for (IInvSlot slot : InventoryIterator.getIterable(autoWb.craftMatrix, ForgeDirection.UP)) {
				ItemStack stack = slot.getStackInSlot();
				if (stack != null) {
					stack.stackSize = 1;
				}
			}
		}
	}

	@Override
	public BuildingStage getBuildStage() {
		return BuildingStage.STANDALONE;
	}

	private TileAutoWorkbench getTile(IBuilderContext context, int x, int y, int z) {
		TileEntity tile = context.world().getTileEntity(x, y, z);
		if (tile != null && tile instanceof TileAutoWorkbench) {
			return (TileAutoWorkbench) tile;
		}
		return null;
	}
}
