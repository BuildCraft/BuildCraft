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
    	// Begin repair recipe handler
        int itemNum = 0;
        ItemStack item1 = null;
        ItemStack item2 = null;
        int slot;
        
        for (slot = 0; slot < par1InventoryCrafting.getSizeInventory(); ++slot)
        {
            ItemStack itemInSlot = par1InventoryCrafting.getStackInSlot(slot);

            if (itemInSlot != null)
            {
                if (itemNum == 0)
                {
                    item1 = itemInSlot;
                }

                if (itemNum == 1)
                {
                    item2 = itemInSlot;
                }

                ++itemNum;
            }
        }

        if (itemNum == 2 && item1.itemID == item2.itemID && item1.stackSize == 1 && item2.stackSize == 1 && Item.itemsList[item1.itemID].isRepairable())
        {
            Item itemBase = Item.itemsList[item1.itemID];
            int item1Durability = itemBase.getMaxDamage() - item1.getItemDamageForDisplay();
            int item2Durability = itemBase.getMaxDamage() - item2.getItemDamageForDisplay();
            int repairAmt = item1Durability + item2Durability + itemBase.getMaxDamage() * 5 / 100;
            int newDamage = itemBase.getMaxDamage() - repairAmt;

            if (newDamage < 0)
            {
                newDamage = 0;
            }

            ArrayList ingredients = new ArrayList<ItemStack>(2);
            ingredients.add(item1);
            ingredients.add(item2);
            return new ShapelessRecipes(new ItemStack(item1.itemID, 1, newDamage),ingredients);
        }
        // End repair recipe handler
        else
        {
        	List recipes = CraftingManager.getInstance().getRecipeList();
            for (int index = 0; index < recipes.size(); ++index)
            {
                IRecipe currentRecipe = (IRecipe) recipes.get(index);

                if (currentRecipe.matches(par1InventoryCrafting, par2World))
                {
                    return currentRecipe;
                }
            }

            return null;
        }
    }


}
