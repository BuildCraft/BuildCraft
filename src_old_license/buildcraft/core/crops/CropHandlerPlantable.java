package buildcraft.core.crops;

import java.util.List;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.IPlantable;

import buildcraft.api.crops.ICropHandler;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.lib.misc.SoundUtil;

public class CropHandlerPlantable implements ICropHandler {
    public static final CropHandlerPlantable INSTANCE = new CropHandlerPlantable();

    protected CropHandlerPlantable() {}

    @Override
    public boolean isSeed(ItemStack stack) {
        if (stack.getItem() instanceof IPlantable) {
            return true;
        }

        if (stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) stack.getItem()).block;
            if (block instanceof IPlantable && block != Blocks.REEDS) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canSustainPlant(World world, ItemStack seed, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (seed.getItem() instanceof IPlantable) {
            Block block = state.getBlock();
            return block.canSustainPlant(state, world, pos, EnumFacing.UP, (IPlantable) seed.getItem()) && world.isAirBlock(pos.up());
        } else {
            Block block = state.getBlock();
            IPlantable plantable = (IPlantable) ((ItemBlock) seed.getItem()).block;
            return block.canSustainPlant(state, world, pos, EnumFacing.UP, plantable) && block != ((ItemBlock) seed.getItem()).block && world.isAirBlock(pos.up());
        }
    }

    @Override
    public boolean plantCrop(World world, EntityPlayer player, ItemStack seed, BlockPos pos) {
        return BlockUtils.useItemOnBlock(world, player, seed, pos, EnumFacing.UP);
    }

    @Override
    public boolean isMature(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof BlockFlower || block instanceof BlockTallGrass || block instanceof BlockMelon || block instanceof BlockMushroom || block instanceof BlockDoublePlant || block == Blocks.PUMPKIN) {
            return true;
        } else if (block instanceof BlockCrops) {
            return state.getValue(BlockCrops.AGE) == 7;
        } else if (block instanceof BlockNetherWart) {
            return state.getValue(BlockNetherWart.AGE) == 3;
        } else if (block instanceof IPlantable) {
            if (blockAccess.getBlockState(pos.down()).getBlock() == block) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean harvestCrop(World world, BlockPos pos, List<ItemStack> drops) {
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            if (BlockUtils.breakBlock((WorldServer) world, pos, drops, pos)) {
                SoundUtil.playBlockBreak(world, pos, state);
                return true;
            }
        }
        return false;
    }
}
