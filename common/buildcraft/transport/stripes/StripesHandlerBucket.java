package buildcraft.transport.stripes;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesHandler.StripesHandlerType;
import buildcraft.api.transport.IStripesPipe;

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
	public boolean handle(World world, BlockPos pos,
			EnumFacing direction, ItemStack stack, EntityPlayer player,
			IStripesPipe pipe) {
		BlockPos underPos = pos.down();
		Block block = world.getBlockState(pos).getBlock();

		if (block == Blocks.air) {
			Block underblock = world.getBlockState(pos).getBlock();

			if (((ItemBucket) stack.getItem()).tryPlaceContainedLiquid(world, underPos)) {
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
					world.setBlockToAir(underPos);

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
