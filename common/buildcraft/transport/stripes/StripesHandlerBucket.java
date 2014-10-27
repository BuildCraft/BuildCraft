package buildcraft.transport.stripes;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
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
	public boolean handle(World world, int x, int y, int z,
			ForgeDirection direction, ItemStack stack, EntityPlayer player,
			IStripesPipe pipe) {
		Block block = world.getBlock(x, y, z);
		if (block == Blocks.air) {
			Block underblock = world.getBlock(x, y - 1, z);
			
			boolean rollback = false;

			if (((ItemBucket) stack.getItem()).tryPlaceContainedLiquid(world, x, y - 1, z)) {
				stack.stackSize = 0;
				pipe.sendItem(emptyBucket, direction.getOpposite());
				
				return true;
			} else if (underblock instanceof IFluidBlock) {
				Fluid fluid = ((IFluidBlock) underblock).getFluid();
				FluidStack fluidStack = new FluidStack(fluid, 1000);
				ItemStack filledBucket = FluidContainerRegistry.fillFluidContainer(fluidStack, emptyBucket);
				if (filledBucket != null) {
					world.setBlockToAir(x, y - 1, z);
				
					stack.stackSize = 0;
					pipe.sendItem(filledBucket, direction.getOpposite());
				}
				
				return true;
			}

			return false;
		}
		return false;
	}

}
