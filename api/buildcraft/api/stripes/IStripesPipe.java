package buildcraft.api.stripes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.IPipe;

public interface IStripesPipe extends IPipe {
	void rollbackItem(ItemStack itemStack, ForgeDirection direction);
}
