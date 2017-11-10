/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

public final class CraftingUtils {

	/**
	 * Deactivate constructor
	 */
	private CraftingUtils() {
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static IRecipe findMatchingRecipe(
			InventoryCrafting par1InventoryCrafting, World par2World) {
		// Begin repair recipe handler
		int itemNum = 0;
		ItemStack item1 = null;
		ItemStack item2 = null;
		int slot;

		for (slot = 0; slot < par1InventoryCrafting.getSizeInventory(); ++slot) {
			ItemStack itemInSlot = par1InventoryCrafting.getStackInSlot(slot);

			if (itemInSlot != null) {
				if (itemNum == 0) {
					item1 = itemInSlot;
				}

				if (itemNum == 1) {
					item2 = itemInSlot;
				}

				++itemNum;
			}
		}

		if (itemNum == 2 && item1 != null && item2 != null
				&& item1.getItem() == item2.getItem()
				&& item1.stackSize == 1 && item2.stackSize == 1
				&& item1.getItem().isRepairable()) {
			Item itemBase = item1.getItem();
			int item1Durability = itemBase.getMaxDamage() - item1.getItemDamageForDisplay();
			int item2Durability = itemBase.getMaxDamage() - item2.getItemDamageForDisplay();
			int repairAmt = item1Durability + item2Durability + itemBase.getMaxDamage() * 5 / 100;
			int newDamage = itemBase.getMaxDamage() - repairAmt;

			if (newDamage < 0) {
				newDamage = 0;
			}

			ArrayList ingredients = new ArrayList<ItemStack>(2);
			ingredients.add(item1);
			ingredients.add(item2);

			return new ShapelessRecipes(new ItemStack(item1.getItem(), 1, newDamage), ingredients);
		} else if (itemNum > 0) {
			// End repair recipe handler

			List recipes = CraftingManager.getInstance().getRecipeList();
			for (Object recipe : recipes) {
				IRecipe currentRecipe = (IRecipe) recipe;

				if (currentRecipe.matches(par1InventoryCrafting, par2World)) {
					return currentRecipe;
				}
			}

			return null;
		} else {
			// No items - no recipe!

			return null;
		}
	}


}
