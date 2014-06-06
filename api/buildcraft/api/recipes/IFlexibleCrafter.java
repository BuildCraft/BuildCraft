/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.recipes;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

public interface IFlexibleCrafter {

	public int getCraftingItemStackSize();

	public ItemStack getCraftingItemStack(int slotid);

	public ItemStack decrCraftingItemgStack(int slotid, int val);

	public FluidStack getCraftingFluidStack(int tankid);

	public FluidStack decrCraftingFluidStack(int tankid, int val);

	public int getCraftingFluidStackSize();

}
