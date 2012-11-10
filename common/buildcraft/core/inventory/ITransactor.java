package buildcraft.core.inventory;

import net.minecraft.src.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public interface ITransactor {

	ItemStack add(ItemStack stack, ForgeDirection orientation, boolean doAdd);

}
