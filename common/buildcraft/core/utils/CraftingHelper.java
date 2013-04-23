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
		{
			int slotIndex = 0;

			for (int slot = 0; slot < crafting.getSizeInventory(); slot++) {
				ItemStack stack = crafting.getStackInSlot(slot);
				if (stack != null) {
					if (stack.stackSize != 1 || !stack.getItem().isRepairable()){
						repair = false;
						break;
					}
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
			for (int index = 0; index < recipes.size(); index++) {
				IRecipe recipe = (IRecipe) recipes.get(index);

				if (recipe.matches(crafting, world)) {
					return recipe;
				}
			}

		}
		return null;
	}
}
