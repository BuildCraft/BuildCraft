package buildcraft.transport.stripes;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.core.lib.utils.BlockUtils;

public class StripesHandlerBucket implements IStripesHandler {
    private static final ItemStack emptyBucket = new ItemStack(Items.bucket, 1);

    private ItemStack getFilledBucket(FluidStack fluidStack, Block underblock) {
        if (underblock == Blocks.lava) {
            return new ItemStack(Items.lava_bucket, 1);
        } else if (underblock == Blocks.water) {
            return new ItemStack(Items.water_bucket, 1);
        } else {
            return FluidContainerRegistry.fillFluidContainer(fluidStack, emptyBucket);
        }
    }

    @Override
    public StripesHandlerType getType() {
        return StripesHandlerType.ITEM_USE;
    }

    @Override
    public boolean shouldHandle(ItemStack stack) {
        return stack.getItem() instanceof ItemBucket;
    }

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        if (world.isAirBlock(pos)) {
            BlockPos place = new BlockPos(pos.getX(), direction.ordinal() < 2 ? pos.getY() : (pos.getY() - 1), pos.getZ());
            if (((ItemBucket) stack.getItem()).tryPlaceContainedLiquid(world, place)) {
                activator.sendItem(emptyBucket, direction.getOpposite());
                stack.stackSize--;
                if (stack.stackSize > 0) {
                    activator.sendItem(stack, direction.getOpposite());
                }

                return true;
            }
        }

        if (!FluidContainerRegistry.isEmptyContainer(stack)) {
            activator.sendItem(stack, direction.getOpposite());
            return true;
        }

        IBlockState targetBlock = world.getBlockState(pos);
        FluidStack fluidStack = BlockUtils.drainBlock(targetBlock, world, pos, true);

        if (fluidStack == null) {
            targetBlock = world.getBlockState(pos.down());
            fluidStack = BlockUtils.drainBlock(targetBlock, world, pos.down(), true);
        }

        ItemStack filledBucket = getFilledBucket(fluidStack, targetBlock.getBlock());

        if (fluidStack == null || filledBucket == null) {
            activator.sendItem(stack, direction.getOpposite());
            return true;
        }

        activator.sendItem(filledBucket, direction.getOpposite());
        stack.stackSize--;
        if (stack.stackSize > 0) {
            activator.sendItem(stack, direction.getOpposite());
        }

        return true;
    }

}
