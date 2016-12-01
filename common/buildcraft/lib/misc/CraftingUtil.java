/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
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

            NonNullList<ItemStack> ingredients = new ArrayList<>(2);
            ingredients.add(item1);
            ingredients.add(item2);

            return new ShapelessRecipes(new ItemStack(item1.getItem(), 1, newDamage), ingredients);
        } else if (itemNum > 0) {
            // End repair recipe handler

            List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
            for (IRecipe recipe : recipes) {

                if (recipe.matches(par1InventoryCrafting, par2World)) {
                    return recipe;
                }
            }

            return null;
        } else {
            // No items - no recipe!

            return null;
        }
    }

}
