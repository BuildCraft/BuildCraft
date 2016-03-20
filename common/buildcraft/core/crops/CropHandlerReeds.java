package buildcraft.core.crops;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import buildcraft.api.crops.CropManager;
import buildcraft.api.crops.ICropHandler;

public class CropHandlerReeds implements ICropHandler {

    @Override
    public boolean isSeed(ItemStack stack) {
        return stack.getItem() == Items.reeds;
    }

    @Override
    public boolean canSustainPlant(World world, ItemStack seed, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block.canSustainPlant(world, pos, EnumFacing.UP, (IPlantable) Blocks.reeds) && block != Blocks.reeds && world.isAirBlock(pos.up());
    }

    @Override
    public boolean plantCrop(World world, EntityPlayer player, ItemStack seed, BlockPos pos) {
        return CropManager.getDefaultHandler().plantCrop(world, player, seed, pos);
    }

    @Override
    public boolean isMature(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
        return false;
    }

    @Override
    public boolean harvestCrop(World world, BlockPos pos, List<ItemStack> drops) {
        return false;
    }
}
