package net.minecraft.src.buildcraft.core;

import net.minecraft.src.ItemStack;

public class AssemblyRecipe {

	public final ItemStack [] input;
	public final ItemStack output;
	public final float energy;

	public AssemblyRecipe (ItemStack [] input, int energy, ItemStack output) {
		this.input = input;
		this.output = output;
		this.energy = energy;
	}

	public boolean canBeDone(ItemStack[] items) {

		for (ItemStack in : input) {

			if(in == null)
				continue; //Optimisation, reduces calculation on a null ingredient

			int found = 0; //Amount of ingredient found in inventory

			for(ItemStack item : items) {
				if(item == null)
					continue; //Broken out of large if statement, increases clarity

				if(item.isItemEqual(in))
					found += item.stackSize; //Adds quantity of stack to amount found
			}

			if(found < in.stackSize)
				return false; //Return false if the amount of ingredient found is not enough
		}

		return true; //Otherwise, returns true
	}
}
