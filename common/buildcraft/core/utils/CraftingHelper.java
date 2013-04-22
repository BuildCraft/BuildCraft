package buildcraft.core.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

public class CraftingHelper {
	public static IRecipe findMatchingRecipe(InventoryCrafting crafting, World world) {
		boolean repair = false;
		ItemStack stack1 = null;
		ItemStack stack2 = null;
		int var6;
		{
			int slotIndex = 0;

			for (int slot = 0; slot < crafting.getSizeInventory(); slot++) {
				ItemStack stack = crafting.getStackInSlot(slot);
				if (stack != null) {
					if (stack.stackSize != 1 || !stack.getItem().isRepairable())
						break;
					slotIndex++;
					
					if (slotIndex == 1) {
						stack1 = stack;
					} else if (slotIndex == 2) {
						repair = (stack.itemID == stack1.itemID);
						stack2 = stack;
					} else {
						repair = false;
						break;
					}
				}
			}
		}

		if (repair) {
			int maxDamage = stack1.getItem().getMaxDamage();
			int damage = stack1.getItemDamageForDisplay() + stack2.getItemDamageForDisplay() - maxDamage - maxDamage * 5 / 100;

			if (damage > 0) {
				ArrayList<ItemStack> ingredients = new ArrayList<ItemStack>(2);
				ingredients.add(stack1);
				ingredients.add(stack2);
				return new ShapelessRecipes(new ItemStack(stack1.itemID, 1, damage), ingredients);
			}
		} else {
			List recipes = CraftingManager.getInstance().getRecipeList();
			for (var6 = 0; var6 < recipes.size(); ++var6) {
				IRecipe recipe = (IRecipe) recipes.get(var6);

				if (recipe.matches(crafting, world)) {
					return recipe;
				}
			}

		}
		return null;
	}
}
package buildcraft.core.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

public class CraftingHelper {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static IRecipe findMatchingRecipe(InventoryCrafting par1InventoryCrafting, World par2World)
    {
        int var3 = 0;
        ItemStack var4 = null;
        ItemStack var5 = null;
        int var6;
        
        for (var6 = 0; var6 < par1InventoryCrafting.getSizeInventory(); ++var6)
        {
            ItemStack var7 = par1InventoryCrafting.getStackInSlot(var6);

            if (var7 != null)
            {
                if (var3 == 0)
                {
                    var4 = var7;
                }

                if (var3 == 1)
                {
                    var5 = var7;
                }

                ++var3;
            }
        }

        if (var3 == 2 && var4.itemID == var5.itemID && var4.stackSize == 1 && var5.stackSize == 1 && Item.itemsList[var4.itemID].isRepairable())
        {
            Item var11 = Item.itemsList[var4.itemID];
            int var13 = var11.getMaxDamage() - var4.getItemDamageForDisplay();
            int var8 = var11.getMaxDamage() - var5.getItemDamageForDisplay();
            int var9 = var13 + var8 + var11.getMaxDamage() * 5 / 100;
            int var10 = var11.getMaxDamage() - var9;

            if (var10 < 0)
            {
                var10 = 0;
            }

            ArrayList ingredients = new ArrayList<ItemStack>(2);
            ingredients.add(var4);
            ingredients.add(var5);
            return new ShapelessRecipes(new ItemStack(var4.itemID, 1, var10),ingredients);
        }
        else
        {
        	List recipes = CraftingManager.getInstance().getRecipeList();
            for (var6 = 0; var6 < recipes.size(); ++var6)
            {
                IRecipe var12 = (IRecipe) recipes.get(var6);

                if (var12.matches(par1InventoryCrafting, par2World))
                {
                    return var12;
                }
            }

            return null;
        }
    }


}
