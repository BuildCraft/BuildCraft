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
import net.minecraftforge.oredict.OreDictionary;

public interface IIntegrationRecipe {
	public static class IntegrationResult {
		public double energyCost;
		public ItemStack output;
		public ItemStack[] usedComponents;

		public static IntegrationResult create(double energyCost, ItemStack output, ItemStack... usedComponents) {
			IntegrationResult result = new IntegrationResult();
			result.energyCost = energyCost;
			result.output = output;
			result.usedComponents = usedComponents;
			return result;
		}

		public static boolean enoughComponents(ItemStack[] components, ItemStack... needs) {
			for (ItemStack need : needs) {
				int found = 0;
				for (ItemStack component : components) {
					if (isMatchingItem(need, component)) {
						found += component.stackSize;
					}
				}
				if (found < need.stackSize) {
					return false;
				}
			}
			return true;
		}

		public static boolean isMatchingItem(ItemStack a, ItemStack b) {
			if (a == null || b == null) {
				return false;
			}
			if (a.getItem() != b.getItem()) {
				return false;
			}
			if (a.getHasSubtypes() && !isWildcard(a) && !isWildcard(b) && a.getItemDamage() != b.getItemDamage()) {
				return false;
			}
			return a.stackTagCompound == null ? b.stackTagCompound == null : a.stackTagCompound.equals(b.stackTagCompound);
		}

		private static boolean isWildcard(ItemStack stack) {
			final int damage = stack.getItemDamage();
			return damage == -1 || damage == OreDictionary.WILDCARD_VALUE;
		}
	}

	boolean isValidInputA(ItemStack inputA);

	boolean isValidInputB(ItemStack inputB);

	IntegrationResult integrate(ItemStack inputA, ItemStack inputB, ItemStack[] components);
}