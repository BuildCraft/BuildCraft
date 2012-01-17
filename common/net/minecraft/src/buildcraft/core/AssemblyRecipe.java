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
			boolean found = false;
			
			for (ItemStack item : items) {
				if (in != null
						&& item != null
						&& item.getItem().shiftedIndex == in.getItem().shiftedIndex
						&& item.getItemDamage() == in.getItemDamage()) {
					found = true;
				}
			}
			
			if (!found) {
				return false;
			}
		}
		
		return true;
	}
}
