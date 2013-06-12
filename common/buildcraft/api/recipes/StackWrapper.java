package buildcraft.api.recipes;

import net.minecraft.item.ItemStack;

public class StackWrapper {
	
	private ItemStack item;
	private OreStack ore;
	public int stackSize;
	
	public StackWrapper(Object x) {
		if (x instanceof ItemStack)
		{
			item = (ItemStack) x;
			stackSize = item.stackSize;
		}
		else item = null;
		if (x instanceof OreStack)
		{
			ore = (OreStack) x;
			stackSize = ore.stackSize;
		}
		else ore = null;
	}
	
	public boolean isItemEqual(ItemStack x)
	{
		return (item != null && item.isItemEqual(x)) || (ore != null && ore.isItemEqual(x));
	}
}
