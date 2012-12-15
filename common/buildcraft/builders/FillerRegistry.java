/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.filler.IFillerRegistry;

public class FillerRegistry implements IFillerRegistry {

	static class ShapedPatternRecipe {

		private int recipeWidth;
		private int recipeHeight;
		private ItemStack recipeItems[];
		private IFillerPattern recipeOutput;

		public ShapedPatternRecipe(int i, int j, ItemStack aitemstack[], IFillerPattern pattern) {
			recipeWidth = i;
			recipeHeight = j;
			recipeItems = aitemstack;
			recipeOutput = pattern;
		}

		public boolean matches(IInventory inventorycrafting) {
			for (int i = 0; i <= 3 - recipeWidth; i++) {
				for (int j = 0; j <= 3 - recipeHeight; j++) {
					if (func_21137_a(inventorycrafting, i, j, true)) {
						return true;
					}
					if (func_21137_a(inventorycrafting, i, j, false)) {
						return true;
					}
				}

			}

			return false;
		}

		private boolean func_21137_a(IInventory inventorycrafting, int i, int j, boolean flag) {
			for (int k = 0; k < 3; k++) {
				for (int l = 0; l < 3; l++) {
					int i1 = k - i;
					int j1 = l - j;
					ItemStack itemstack = null;
					if (i1 >= 0 && j1 >= 0 && i1 < recipeWidth && j1 < recipeHeight) {
						if (flag) {
							itemstack = recipeItems[(recipeWidth - i1 - 1) + j1 * recipeWidth];
						} else {
							itemstack = recipeItems[i1 + j1 * recipeWidth];
						}
					}
					ItemStack itemstack1 = inventorycrafting.getStackInSlot(k + l * 3);
					if (itemstack1 == null && itemstack == null) {
						continue;
					}
					if (itemstack1 == null && itemstack != null || itemstack1 != null && itemstack == null) {
						return false;
					}
					if (itemstack.itemID != itemstack1.itemID) {
						return false;
					}
					if (itemstack.getItemDamage() != -1 && itemstack.getItemDamage() != itemstack1.getItemDamage()) {
						return false;
					}
				}

			}

			return true;
		}

	}

	static LinkedList<ShapedPatternRecipe> recipes = new LinkedList<ShapedPatternRecipe>();

	@Override
	public void addRecipe(IFillerPattern pattern, Object aobj[]) {
		String s = "";
		int i = 0;
		int j = 0;
		int k = 0;
		if (aobj[i] instanceof String[]) {
			String as[] = (String[]) aobj[i++];
			for (int l = 0; l < as.length; l++) {
				String s2 = as[l];
				k++;
				j = s2.length();
				s = (new StringBuilder()).append(s).append(s2).toString();
			}

		} else {
			while (aobj[i] instanceof String) {
				String s1 = (String) aobj[i++];
				k++;
				j = s1.length();
				s = (new StringBuilder()).append(s).append(s1).toString();
			}
		}
		HashMap<Character, ItemStack> hashmap = new HashMap<Character, ItemStack>();
		for (; i < aobj.length; i += 2) {
			Character character = (Character) aobj[i];
			ItemStack itemstack1 = null;
			if (aobj[i + 1] instanceof Item) {
				itemstack1 = new ItemStack((Item) aobj[i + 1]);
			} else if (aobj[i + 1] instanceof Block) {
				itemstack1 = new ItemStack((Block) aobj[i + 1], 1, -1);
			} else if (aobj[i + 1] instanceof ItemStack) {
				itemstack1 = (ItemStack) aobj[i + 1];
			}
			hashmap.put(character, itemstack1);
		}

		ItemStack aitemstack[] = new ItemStack[j * k];
		for (int i1 = 0; i1 < j * k; i1++) {
			char c = s.charAt(i1);
			if (hashmap.containsKey(Character.valueOf(c))) {
				aitemstack[i1] = hashmap.get(Character.valueOf(c)).copy();
			} else {
				aitemstack[i1] = null;
			}
		}

		recipes.add(new ShapedPatternRecipe(j, k, aitemstack, pattern));
		pattern.setId(recipes.size());
	}

	@Override
	public IFillerPattern findMatchingRecipe(IInventory inventorycrafting) {
		for (int i = 0; i < recipes.size(); i++) {
			ShapedPatternRecipe irecipe = recipes.get(i);
			if (irecipe.matches(inventorycrafting)) {
				return irecipe.recipeOutput;
			}
		}

		return null;
	}

	@Override
	public int getPatternNumber(IFillerPattern pattern) {
		int i = 0;

		for (ShapedPatternRecipe recipe : recipes) {
			if (recipe.recipeOutput == pattern) {

				return i;
			}

			i++;
		}

		return -1;
	}

	@Override
	public IFillerPattern getPattern(int n) {
		if (n <= 0) {
			return null;
		}

		return recipes.get(n - 1).recipeOutput;
	}

}
