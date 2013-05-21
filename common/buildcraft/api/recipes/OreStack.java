package buildcraft.api.recipes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;


/*
 * Helper class used to enclose both the OreDictionary ore and it's quantity
 */
public class OreStack {

	public int oreID;
	public int stackSize;

	public OreStack(String name){ this(name, 1); }
	public OreStack(String name, int size){ this(OreDictionary.getOreID(name), size); }
	public OreStack(int id){ this(id, 1); }
	
	public OreStack(int id, int size) {
		oreID = id;
		stackSize = size;
	}
	
	public OreStack copy()
	{
		return new OreStack(oreID, stackSize);
	}
	
	public boolean isItemEqual(ItemStack item)
	{
		return oreID == OreDictionary.getOreID(item);
	}

	public String toString()
	{
		return stackSize + "x" + OreDictionary.getOreName(oreID);
	}
	
}
