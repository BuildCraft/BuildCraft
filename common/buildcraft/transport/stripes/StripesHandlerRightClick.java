package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.pipes.IStripesHandler;
import buildcraft.api.pipes.IStripesPipe;

public class StripesHandlerRightClick implements IStripesHandler {

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}
	
	@Override
	public boolean shouldHandle(ItemStack stack) {
		return (stack.getItem() == Items.potionitem && ItemPotion.isSplash(stack.getItemDamage()))
				   || stack.getItem() == Items.egg
				   || stack.getItem() == Items.snowball;
	}

	@Override
	public boolean handle(World world, int x, int y, int z,
			ForgeDirection direction, ItemStack stack, EntityPlayer player,
			IStripesPipe pipe) {
		stack.getItem().onItemRightClick(stack, world, player);
		return true;
	}

}
