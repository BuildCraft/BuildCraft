package buildcraft.api.recipes;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

public class AssemblyOreRecipe extends AssemblyRecipe {

	public ArrayList recipe = new ArrayList();
	
	public AssemblyOreRecipe(Object[] input, int energy, ItemStack output) {
		super(null, energy, output);
		for (Object x: input)
		{
			if (x instanceof ItemStack) recipe.add(((ItemStack) x).copy());
			else if (x instanceof OreStack) recipe.add(((OreStack) x).copy());
			else if (x instanceof String) recipe.add(new OreStack((String) x));
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
	public boolean canBeDone(ItemStack[] items) {

		for (Object in : recipe) {

			if (in == null) {
				continue;
			}
			
			StackWrapper stack = new StackWrapper(in);
			
			int found = 0; // Amount of ingredient found in inventory

			for (ItemStack item : items) {
				if (item == null) {
					continue;
				}

				if (stack.isItemEqual(item)) {
					found += item.stackSize; // Adds quantity of stack to amount
												// found
				}
			}

			if (found < stack.stackSize)
				return false; // Return false if the amount of ingredient found
								// is not enough
		}

		return true;
	}
}
