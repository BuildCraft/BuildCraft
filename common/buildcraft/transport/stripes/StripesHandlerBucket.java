package buildcraft.transport.stripes;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import buildcraft.api.pipes.IStripesHandler;
import buildcraft.api.pipes.IStripesPipe;

public class StripesHandlerBucket implements IStripesHandler {
	private static final ItemStack emptyBucket = new ItemStack(Items.bucket, 1);
	
	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}
	
	@Override
	public boolean shouldHandle(ItemStack stack) {
		return stack.getItem() instanceof ItemBucket;
	}

	@Override
	public boolean handle(World world, int x, int y, int z,
			ForgeDirection direction, ItemStack stack, EntityPlayer player,
			IStripesPipe pipe) {
		Block block = world.getBlock(x, y, z);
		if (block == Blocks.air) {
			Block underblock = world.getBlock(x, y - 1, z);

			if (((ItemBucket) stack.getItem()).tryPlaceContainedLiquid(world, x, y - 1, z)) {
				stack.stackSize = 0;
				pipe.sendItem(emptyBucket, direction.getOpposite());
				
				return true;
			} else {
				ItemStack filledBucket = null;

				if (underblock instanceof IFluidBlock) {
					Fluid fluid = ((IFluidBlock) underblock).getFluid();
					FluidStack fluidStack = new FluidStack(fluid, 1000);
					filledBucket = FluidContainerRegistry.fillFluidContainer(fluidStack, emptyBucket);
				}

				if (underblock == Blocks.lava) {
					filledBucket = new ItemStack(Items.lava_bucket, 1);
				}

				if (underblock == Blocks.water) {
					filledBucket = new ItemStack(Items.water_bucket, 1);
				}

				if (filledBucket != null) {
					world.setBlockToAir(x, y - 1, z);

					stack.stackSize = 0;
					pipe.sendItem(filledBucket, direction.getOpposite());

					return true;
				}
			}

			return false;
		}
		return false;
	}

}
