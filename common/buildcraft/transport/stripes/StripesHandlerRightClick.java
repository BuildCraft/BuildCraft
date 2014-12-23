package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesHandler.StripesHandlerType;
import buildcraft.api.transport.IStripesPipe;

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
	public boolean handle(World world, BlockPos pos,
			EnumFacing direction, ItemStack stack, EntityPlayer player,
			IStripesPipe pipe) {
		stack.getItem().onItemRightClick(stack, world, player);
		return true;
	}

}
