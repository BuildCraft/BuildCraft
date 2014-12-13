package buildcraft.transport.stripes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesHandler.StripesHandlerType;
import buildcraft.api.transport.IStripesPipe;

public class StripesHandlerShears implements IStripesHandler {

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}
	
	@Override
	public boolean shouldHandle(ItemStack stack) {
		return stack.getItem() instanceof ItemShears;
	}

	@Override
	public boolean handle(World world, BlockPos pos,
			EnumFacing direction, ItemStack stack, EntityPlayer player,
			IStripesPipe pipe) {
		Block block = world.getBlockState(pos).getBlock();

		if (block instanceof BlockLeavesBase) {
			world.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), Block.soundTypeGrass.getBreakSound(), 1, 1);
			world.setBlockToAir(pos);
			stack.damageItem(1, player);
			return true;
		}
		
		return false;
	}

}
