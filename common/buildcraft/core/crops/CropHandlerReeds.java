package buildcraft.core.crops;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.crops.CropManager;
import buildcraft.api.crops.ICropHandler;

public class CropHandlerReeds implements ICropHandler {

    @Override
    public boolean isSeed(ItemStack stack) {
        return stack.getItem() == Items.reeds;
    }

    @Override
    public boolean canSustainPlant(World world, ItemStack seed, BlockPos pos) {
        return CropManager.getDefaultHandler().canSustainPlant(world, seed, pos);
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
