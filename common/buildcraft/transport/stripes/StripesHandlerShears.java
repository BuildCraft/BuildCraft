package buildcraft.transport.stripes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.stripes.IStripesItemHandler;
import buildcraft.api.stripes.IStripesPipe;

public class StripesHandlerShears implements IStripesItemHandler {

	@Override
	public boolean shouldHandle(ItemStack stack) {
		return stack.getItem() instanceof ItemShears;
	}

	@Override
	public boolean handle(World world, int x, int y, int z,
			ForgeDirection direction, ItemStack stack, EntityPlayer player,
			IStripesPipe pipe) {
		Block block = world.getBlock(x, y, z);

		if (block instanceof BlockLeavesBase) {
			world.playSoundEffect(x, y, z, Block.soundTypeGrass.getBreakSound(), 1, 1);
			world.setBlockToAir(x, y, z);
			stack.damageItem(1, player);
			return true;
		}
		
		return false;
	}

}
