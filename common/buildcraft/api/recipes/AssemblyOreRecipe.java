package buildcraft.api.recipes;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.*;

public class AssemblyOreRecipe extends AssemblyRecipe {

  private ArrayList input;
	
	public AssemblyOreRecipe(Object[] recipe, int energy, ItemStack output) {
		super(null, energy, output);
		input = new ArrayList();
		for (Object x: recipe)
		{
			if (x instanceof ItemStack) input.add((ItemStack) x);
			else if (x instanceof String) input.add(new OreStack(OreDictionary.getOreID((String) x)));
			else if (x instanceof OreStack) input.add((OreStack) x);
			else
            {
                String ret = "Invalid assembly ore recipe: ";
                for (Object tmp :  recipe)
                {
                    ret += tmp + ", ";
                }
                ret += output;
                throw new RuntimeException(ret);
            }
		}
	}
	
	@Override
	public boolean canBeDone(ItemStack[] items)
	{
		for (Object in : input) {

			if (in == null) {
				continue;
			}

			if (in instanceof ItemStack && !check((ItemStack) in, items)) return false;
			if (in instanceof OreStack && !check((OreStack) in, items)) return false;
		}
		
		return true;
	}
	
	private boolean check(ItemStack in, ItemStack[] items)
	{
		int found = 0; // Amount of ingredient found in inventory

		for (ItemStack item : items) {
			if (item == null) {
				continue;
			}

			if (in.isItemEqual(item)) {
				found += item.stackSize; // Adds quantity of stack to amount
											// found
			}
		}

		return found >= in.stackSize;
	}
	
	private boolean check(OreStack in, ItemStack[] items)
	{
		int found = 0; // Amount of ingredient found in inventory

		for (ItemStack item : items) {
			if (item == null) {
				continue;
			}

			if (in.isItemEqual(item)) {
				found += item.stackSize; // Adds quantity of stack to amount
											// found
			}
		}

		return found >= in.stackSize;
	}
}
