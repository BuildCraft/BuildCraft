package buildcraft.core.crops;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.crops.ICropHandler;
import buildcraft.core.lib.utils.BlockUtils;

public class CropHandlerReeds implements ICropHandler {

	@Override
	public boolean isSeed(ItemStack stack) {
		return stack.getItem() == Items.reeds;
	}

	@Override
	public boolean canSustainPlant(World world, ItemStack seed, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		return block.canSustainPlant(world, x, y, z, ForgeDirection.UP, (IPlantable) Blocks.reeds)
				&& block != Blocks.reeds && world.isAirBlock(x, y + 1, z);
	}

	@Override
	public boolean isMature(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z) {
		if (block == null) {
			return false;
		} else if (block == Blocks.reeds) {
			if (y > 0 && blockAccess.getBlock(x, y - 1, z) == block) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean harvestCrop(World world, int x, int y, int z, List<ItemStack> drops) {
		if (!world.isRemote) {
			Block block = world.getBlock(x, y, z);
			int meta = world.getBlockMetadata(x, y, z);
			if (BlockUtils.breakBlock((WorldServer) world, x, y, z, drops)) {
				world.playAuxSFXAtEntity(null, 2001, x, y, z, Block.getIdFromBlock(block)
						+ (meta << 12));
				return true;
			}
		}
		return false;
	}

}
