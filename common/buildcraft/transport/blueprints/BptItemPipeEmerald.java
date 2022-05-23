/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.blueprints;

import java.util.LinkedList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.blueprints.BptBlockUtils;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.core.blueprints.BptItem;

public class BptItemPipeEmerald extends BptItem {

	public BptItemPipeEmerald() {
	}

	@Override
	public void addRequirements(BptSlotInfo slot, LinkedList<ItemStack> requirements) {

	}
	
	@Override
	public void initializeFromWorld(BptSlotInfo bptSlot, IBptContext context, int x, int y, int z) {
		TileEntity te = context.world().getBlockTileEntity(x, y, z);
		if (!(te instanceof IInventory)) return;
		IInventory inventory = (IInventory) te;

		BptBlockUtils.initializeInventoryContents(bptSlot, context, inventory);
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		TileEntity te = context.world().getBlockTileEntity(slot.x, slot.y, slot.z);
		if (!(te instanceof IInventory)) return;
		IInventory inventory = (IInventory) te;

		BptBlockUtils.buildInventoryContents(slot, context, inventory);
	}

}
