package buildcraft.api.recipes;

import java.util.ArrayList;
import java.util.LinkedList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class AssemblyRecipe {

	public static LinkedList<AssemblyRecipe> assemblyRecipes = new LinkedList<AssemblyRecipe>();

	public final ItemStack[] input;
	public final ItemStack output;
	public final float energy;

	public final Object[] inputOreDict;

	public AssemblyRecipe(ItemStack[] input, int energy, ItemStack output) {
		this.input = input;
		this.output = output;
		this.energy = energy;
		this.inputOreDict = input;
	}

	/** This version of AssemblyRecipe supports the OreDictionary
	* 
	* @param input Object[] containing either an ItemStack, or a paired string and integer(ex: "dyeBlue", 1)
	* @param energy MJ cost to produce
	* @param output resulting ItemStack
	*/
	public AssemblyRecipe(Object[] input, int energy, ItemStack output) {
		this.output = output;
		this.energy = energy;

		this.inputOreDict = new Object[input.length];

		int count = 0;
		for (int idx = 0; idx < input.length; idx++) {
			if (input[idx] == null) {
				continue;
			}

			ItemStack in;

			if (input[idx] instanceof ItemStack) {
				inputOreDict[idx] = input[idx];
				count++;
			} else if (input[idx] instanceof String) {
				ArrayList<ItemStack> oreListWithStackSize = new ArrayList<ItemStack>();

				for (ItemStack oreItem : OreDictionary.getOres((String)input[idx])) {
					ItemStack sizeAdjustedOreItem = oreItem.copy();

					//Desired recipe stacksize is on next index
					sizeAdjustedOreItem.stackSize = (int)input[idx + 1];

					oreListWithStackSize.add(sizeAdjustedOreItem);
				}

				inputOreDict[idx++] = oreListWithStackSize;
				count++;
			}
		}

		// create the recipe item array
		this.input = new ItemStack[count];
		count = 0;
		for(Object recipeItem : inputOreDict) {
			if (recipeItem == null) {
				continue;
			}

			// since the API recipe item array is an ItemStack, just grab the first item from the OreDict list
			this.input[count++] = recipeItem instanceof ItemStack ? (ItemStack)recipeItem: ((ArrayList<ItemStack>)recipeItem).get(0);
		}
	}

	public boolean canBeDone(ItemStack[] items) {

		for (Object in : inputOreDict) {

			if (in == null) {
				continue;
			}

			int found = 0; // Amount of ingredient found in inventory
			int expected = in instanceof ItemStack ? ((ItemStack)in).stackSize: in instanceof ArrayList ? ((ArrayList<ItemStack>)in).get(0).stackSize: 1;

			for (ItemStack item : items) {
				if (item == null) {
					continue;
				}

				if (in instanceof ItemStack) {
					if (item.isItemEqual((ItemStack)in)) {
						found += item.stackSize; // Adds quantity of stack to amount found
					}
				} else if (in instanceof ArrayList) {
					for (ItemStack oreItem : (ArrayList<ItemStack>)in) {
						if(OreDictionary.itemMatches(oreItem, item, true)) {
							found += item.stackSize;
							break;
						}
					}
				}
			}

			if (found < expected)
				return false; // Return false if the amount of ingredient found
								// is not enough
		}

		return true;
	}
}
