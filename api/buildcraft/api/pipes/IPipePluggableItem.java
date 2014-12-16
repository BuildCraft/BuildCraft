package buildcraft.api.pipes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public interface IPipePluggableItem {
	PipePluggable createPipePluggable(IPipe pipe, ForgeDirection side, ItemStack stack);
}
