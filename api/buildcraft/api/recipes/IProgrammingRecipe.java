package buildcraft.api.recipes;

import java.util.List;
import net.minecraft.item.ItemStack;

public interface IProgrammingRecipe {
	public String getId();

	public List<ItemStack> getOptions(int width, int height);
	public int getEnergyCost(ItemStack option);

	public boolean canCraft(ItemStack input);
	public ItemStack craft(ItemStack input, ItemStack option);
}
