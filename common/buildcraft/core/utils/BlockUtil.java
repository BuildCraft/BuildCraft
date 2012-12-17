/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.core.utils;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.api.core.BuildCraftAPI;

public class BlockUtil {

	public static List<ItemStack> getItemStackFromBlock(World world, int i, int j, int k) {
		Block block = Block.blocksList[world.getBlockId(i, j, k)];

		if (block == null)
			return null;

		int meta = world.getBlockMetadata(i, j, k);

		return block.getBlockDropped(world, i, j, k, meta, 0);
	}

	public static void breakBlock(World world, int x, int y, int z) {
		int blockId = world.getBlockId(x, y, z);

		if (blockId != 0 && BuildCraftCore.dropBrokenBlocks && !world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
			List<ItemStack> items = Block.blocksList[blockId].getBlockDropped(world, x, y, z, world.getBlockMetadata(x, y, z), 0);

			for (ItemStack item : items) {
				float var = 0.7F;
				double dx = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
				double dy = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
				double dz = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
				EntityItem entityitem = new EntityItem(world, x + dx, y + dy, z + dz, item);

				entityitem.lifespan = BuildCraftCore.itemLifespan;
				entityitem.delayBeforeCanPickup = 10;

				world.spawnEntityInWorld(entityitem);
			}
		}

		world.setBlockWithNotify(x, y, z, 0);
	}

	public static boolean canChangeBlock(World world, int x, int y, int z) {
		if (world.isAirBlock(x, y, z))
			return true;

		int blockID = world.getBlockId(x, y, z);
		if (Block.blocksList[blockID] == null)
			return true;
		Block block = Block.blocksList[blockID];

		if (block.getBlockHardness(world, x, y, z) < 0)
			return false;

		if (blockID == BuildCraftEnergy.oilMoving.blockID || blockID == BuildCraftEnergy.oilStill.blockID)
			return false;

		if (blockID == Block.lavaStill.blockID || blockID == Block.lavaMoving.blockID)
			return false;

		return true;
	}

	public static boolean isSoftBlock(World world, int x, int y, int z) {
		if (world.isAirBlock(x, y, z))
			return true;

		int blockId = world.getBlockId(x, y, z);

		return BuildCraftAPI.softBlocks[blockId] || Block.blocksList[blockId] == null;
	}
}
