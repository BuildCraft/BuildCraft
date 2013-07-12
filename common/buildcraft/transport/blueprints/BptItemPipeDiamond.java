/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.blueprints;

import buildcraft.api.blueprints.BptBlockUtils;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.core.blueprints.BptItem;
import java.util.LinkedList;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public class BptItemPipeDiamond extends BptItem {

	public BptItemPipeDiamond() {
	}

	@Override
	public void addRequirements(BptSlotInfo slot, LinkedList<ItemStack> requirements) {

	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		ItemStack inv[] = BptBlockUtils.getItemStacks(slot, context);
		ItemStack newInv[] = new ItemStack[54];

		for (int dir = 0; dir <= 1; ++dir) {
			for (int s = 0; s < 9; ++s) {
				newInv[dir * 9 + s] = inv[dir * 9 + s];
			}
		}

		for (int dir = 2; dir <= 5; ++dir) {
			ForgeDirection r = ForgeDirection.values()[dir].getRotation(ForgeDirection.DOWN);

			for (int s = 0; s < 9; ++s) {
				newInv[r.ordinal() * 9 + s] = inv[dir * 9 + s];
			}
		}

		BptBlockUtils.setItemStacks(slot, context, newInv);
	}

	@Override
	public void initializeFromWorld(BptSlotInfo bptSlot, IBptContext context, int x, int y, int z) {
		IInventory inventory = (IInventory) context.world().getBlockTileEntity(x, y, z);

		BptBlockUtils.initializeInventoryContents(bptSlot, context, inventory);
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		IInventory inventory = (IInventory) context.world().getBlockTileEntity(slot.x, slot.y, slot.z);

		BptBlockUtils.buildInventoryContents(slot, context, inventory);
	}

}
