package buildcraft.core.crops;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
    public boolean canSustainPlant(World world, ItemStack seed, int x, int y, int z) {
        return CropManager.getDefaultHandler().canSustainPlant(world, seed, x, y, z);
    }

    @Override
    public boolean plantCrop(World world, EntityPlayer player, ItemStack seed, int x, int y, int z) {
        return CropManager.getDefaultHandler().plantCrop(world, player, seed, x, y, z);
    }

    @Override
    public boolean isMature(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z) {
        return false;
    }

    @Override
    public boolean harvestCrop(World world, int x, int y, int z, List<ItemStack> drops) {
        return false;
    }
}
