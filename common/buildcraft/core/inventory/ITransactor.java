package buildcraft.core.inventory;

import net.minecraft.src.ItemStack;
import buildcraft.api.core.Orientations;

public interface ITransactor {

	ItemStack add(ItemStack stack, Orientations orientation, boolean doAdd);
	ItemStack addSpecific(ItemStack stack,int slot, Orientations orientation, boolean doAdd);

}
