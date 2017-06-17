/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.misc;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public final class CraftingUtil {

    /** Deactivate constructor */
    private CraftingUtil() {}

    public static IRecipe findMatchingRecipe(InventoryCrafting par1InventoryCrafting, World par2World) {
        // Begin repair recipe handler
        int itemNum = 0;
        ItemStack item1 = ItemStack.EMPTY;
        ItemStack item2 = ItemStack.EMPTY;
        int slot;

        for (slot = 0; slot < par1InventoryCrafting.getSizeInventory(); ++slot) {
            ItemStack itemInSlot = par1InventoryCrafting.getStackInSlot(slot);

            if (!itemInSlot.isEmpty()) {
                if (itemNum == 0) {
                    item1 = itemInSlot;
                }

                if (itemNum == 1) {
                    item2 = itemInSlot;
                }

                ++itemNum;
            }
        }

        if (itemNum == 2 && item1.getItem() == item2.getItem() && item1.getCount() == 1 && item2.getCount() == 1 && item1.getItem().isRepairable()) {
            int item1Durability = item1.getMaxDamage() - item1.getItemDamage();
            int item2Durability = item2.getMaxDamage() - item2.getItemDamage();
            int repairAmt = item1Durability + item2Durability + item1.getMaxDamage() * 5 / 100;
            int newDamage = item1.getMaxDamage() - repairAmt;

            if (newDamage < 0) {
                newDamage = 0;
            }

            return null; //new ShapelessRecipes(new ItemStack(item1.getItem(), 1, newDamage), StackUtil.listOf(item1, item2));
        } else if (itemNum > 0) {
            // End repair recipe handler

            /*List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
            for (IRecipe recipe : recipes) {

                if (recipe.matches(par1InventoryCrafting, par2World)) {
                    return recipe;
                }
            }*/

            return null;
        } else {
            // No items - no recipe!

            return null;
        }
    }

}
