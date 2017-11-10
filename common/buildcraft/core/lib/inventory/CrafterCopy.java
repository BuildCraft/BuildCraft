/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.inventory;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.recipes.IFlexibleCrafter;

public class CrafterCopy implements IFlexibleCrafter {

	private ItemStack[] items;
	private FluidStack[] fluids;

	public CrafterCopy(IFlexibleCrafter origin) {
		items = new ItemStack[origin.getCraftingItemStackSize()];

		for (int i = 0; i < items.length; ++i) {
			ItemStack s = origin.getCraftingItemStack(i);

			if (s != null) {
				items[i] = s.copy();
			} else {
				items[i] = null;
			}
		}

		fluids = new FluidStack[origin.getCraftingFluidStackSize()];

		for (int i = 0; i < fluids.length; ++i) {
			FluidStack f = origin.getCraftingFluidStack(i);

			if (f != null) {
				fluids[i] = origin.getCraftingFluidStack(i).copy();
			} else {
				fluids[i] = null;
			}
		}
	}

	@Override
	public int getCraftingItemStackSize() {
		return items.length;
	}

	@Override
	public ItemStack getCraftingItemStack(int slotid) {
		return items[slotid];
	}

	@Override
	public ItemStack decrCraftingItemStack(int slotid, int val) {
		ItemStack result;

		if (val >= items[slotid].stackSize) {
			result = items[slotid];
			items[slotid] = null;
		} else {
			result = items[slotid].copy();
			result.stackSize = val;
			items[slotid].stackSize -= val;
		}

		return result;
	}

	@Override
	public FluidStack getCraftingFluidStack(int tankid) {
		return fluids[tankid];
	}

	@Override
	public FluidStack decrCraftingFluidStack(int tankid, int val) {
		FluidStack result;

		if (val >= fluids[tankid].amount) {
			result = fluids[tankid];
			fluids[tankid] = null;
		} else {
			result = fluids[tankid].copy();
			result.amount = val;
			fluids[tankid].amount -= val;
		}

		return result;
	}

	@Override
	public int getCraftingFluidStackSize() {
		return fluids.length;
	}

}
