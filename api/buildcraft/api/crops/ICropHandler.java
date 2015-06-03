package buildcraft.api.crops;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface ICropHandler {

	/**
	 * Check if an item is a seed.
	 *
	 * @param stack
	 * @return true if the item can be planted.
	 */
	boolean isSeed(ItemStack stack);

	/**
	 * Check if the item can be planted. You can assume canSustainPlant() will
	 * only be called if isSeed() returned true.
	 *
	 * @param world
	 * @param seed
	 * @param x
	 * @param y
	 * @param z
	 * @return true if the item can be planted at (x, y, z).
	 */
	boolean canSustainPlant(World world, ItemStack seed, int x, int y, int z);

	/**
	 * Plant the item in the block. You can assume plantCrop() will only be
	 * called if canSustainPlant() returned true.
	 *
	 * @param world
	 * @param player
	 * @param seed
	 * @param x
	 * @param y
	 * @param z
	 * @return true if the item was planted at (x, y, z).
	 */
	boolean plantCrop(World world, EntityPlayer player, ItemStack seed, int x, int y, int z);

	/**
	 * Check if a crop is mature and can be harvested.
	 *
	 * @param blockAccess
	 * @param block
	 * @param meta
	 * @param x
	 * @param y
	 * @param z
	 * @return true if the block at (x, y, z) is mature and can be harvested.
	 */
	boolean isMature(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z);

	/**
	 * Harvest the crop. You can assume harvestCrop() will only be called if
	 * isMature() returned true.
	 *
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param drops
	 *            a list to return the harvest's drops.
	 * @return true if the block was successfully harvested.
	 */
	boolean harvestCrop(World world, int x, int y, int z, List<ItemStack> drops);

}
