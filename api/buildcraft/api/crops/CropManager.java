package buildcraft.api.crops;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class CropManager {
	private static List<ICropHandler> handlers = new ArrayList<ICropHandler>();
	private static ICropHandler defaultHandler;

	private CropManager() {

	}

	public static void registerHandler(ICropHandler cropHandler) {
		handlers.add(cropHandler);
	}

	public static void setDefaultHandler(ICropHandler cropHandler) {
		defaultHandler = cropHandler;
	}

	public static ICropHandler getDefaultHandler() {
		return defaultHandler;
	}

	public static boolean isSeed(ItemStack stack) {
		for (ICropHandler cropHandler : handlers) {
			if (cropHandler.isSeed(stack)) {
				return true;
			}
		}
		return defaultHandler.isSeed(stack);
	}

	public static boolean canSustainPlant(World world, ItemStack seed, int x, int y, int z) {
		for (ICropHandler cropHandler : handlers) {
			if (cropHandler.isSeed(seed) && cropHandler.canSustainPlant(world, seed, x, y, z)) {
				return true;
			}
		}
		return defaultHandler.isSeed(seed) && defaultHandler.canSustainPlant(world, seed, x, y, z);
	}

	public static boolean plantCrop(World world, EntityPlayer player, ItemStack seed, int x, int y,
			int z) {
		for (ICropHandler cropHandler : handlers) {
			if (cropHandler.isSeed(seed) && cropHandler.canSustainPlant(world, seed, x, y, z)
					&& cropHandler.plantCrop(world, player, seed, x, y, z)) {
				return true;
			}
		}
		return defaultHandler.plantCrop(world, player, seed, x, y, z);
	}

	public static boolean isMature(IBlockAccess blockAccess, Block block, int meta, int x, int y,
			int z) {
		for (ICropHandler cropHandler : handlers) {
			if (cropHandler.isMature(blockAccess, block, meta, x, y, z)) {
				return true;
			}
		}
		return defaultHandler.isMature(blockAccess, block, meta, x, y, z);
	}

	public static boolean harvestCrop(World world, int x, int y, int z, List<ItemStack> drops) {
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		for (ICropHandler cropHandler : handlers) {
			if (cropHandler.isMature(world, block, meta, x, y, z)) {
				return cropHandler.harvestCrop(world, x, y, z, drops);
			}
		}
		return defaultHandler.isMature(world, block, meta, x, y, z)
				&& defaultHandler.harvestCrop(world, x, y, z, drops);
	}

}
