package buildcraft.transport.stripes;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.stripes.IStripesItemHandler;
import buildcraft.api.stripes.IStripesPipe;

public class StripesHandlerBucket implements IStripesItemHandler {

	@Override
	public boolean shouldHandle(ItemStack stack) {
		return stack.getItem() instanceof ItemBucket;
	}

	@Override
	public boolean handle(World world, int x, int y, int z,
			ForgeDirection direction, ItemStack stack, EntityPlayer player,
			IStripesPipe pipe) {
		if (world.getBlock(x, y, z) == Blocks.air) {
			Block underblock = world.getBlock(x, y - 1, z);
			Item newBucket = Items.bucket;

			if (underblock == Blocks.water) {
				newBucket = Items.water_bucket;
			}

			if (underblock == Blocks.lava) {
				newBucket = Items.lava_bucket;
			}
			
			boolean rollback = false;

			if (((ItemBucket) stack.getItem()).tryPlaceContainedLiquid(world,
					x, y - 1, z)) {
				rollback = true;
			} else if (newBucket != Items.bucket) {
				world.setBlockToAir(x, y - 1, z);
				rollback = true;
			}

			if (rollback) {
				stack.stackSize = 0;
				pipe.rollbackItem(new ItemStack(newBucket, 1), direction);
			}
			
			return true;
		}
		return false;
	}

}
