package buildcraft.api.crops;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class CropManager {

	private static List<ICropHandler> handlers = new ArrayList<ICropHandler>();

	public static void registerHandler(ICropHandler cropHandler) {
		handlers.add(cropHandler);
	}

	public static boolean isSeed(ItemStack stack) {
		for (ICropHandler cropHandler : handlers) {
			if (cropHandler.isSeed(stack)) {
				return true;
			}
		}
		return false;
	}

	public static boolean canSustainPlant(World world, ItemStack seed, int x, int y, int z) {
		for (ICropHandler cropHandler : handlers) {
			if (cropHandler.isSeed(seed) && cropHandler.canSustainPlant(world, seed, x, y, z))
				return true;
		}
		return false;
	}

	public static boolean isMature(IBlockAccess blockAccess, Block block, int meta, int x, int y,
			int z) {
		for (ICropHandler cropHandler : handlers) {
			if (cropHandler.isMature(blockAccess, block, meta, x, y, z)) {
				return true;
			}
		}
		return false;
	}

	public static boolean harvestCrop(World world, int x, int y, int z, List<ItemStack> drops) {
		for (ICropHandler cropHandler : handlers) {
			Block block = world.getBlock(x, y, z);
			int meta = world.getBlockMetadata(x, y, z);
			if (cropHandler.isMature(world, block, meta, x, y, z)) {
				return cropHandler.harvestCrop(world, x, y, z, drops);
			}
		}
		return false;
	}

}
