package buildcraft.api.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IStripesItemHandler {
	boolean shouldHandle(ItemStack stack);
	
	boolean handle(World world, int x, int y, int z, ForgeDirection direction,
			ItemStack stack, EntityPlayer player, IStripesPipe pipe);
}
