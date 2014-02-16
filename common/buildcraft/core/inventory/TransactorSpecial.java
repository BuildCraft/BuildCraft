/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.inventory.filters.IStackFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class TransactorSpecial extends Transactor {

	protected ISpecialInventory inventory;

	public TransactorSpecial(ISpecialInventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd) {
		return inventory.addItem(stack, doAdd, orientation);
	}

	@Override
	public ItemStack remove(IStackFilter filter, ForgeDirection orientation, boolean doRemove) {
		ItemStack[] extracted = inventory.extractItem(false, orientation, 1);
		if (extracted != null && extracted.length > 0 && filter.matches(extracted[0])) {
			if (doRemove) {
				inventory.extractItem(true, orientation, 1);
			}
			return extracted[0];
		}
		return null;
	}
}
