package buildcraft.core.crops;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockMelon;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.crops.ICropHandler;
import buildcraft.core.lib.utils.BlockUtils;

public class CropHandlerPlantable implements ICropHandler {

	private static final Set<Block> FORBIDDEN_BLOCKS = new HashSet<Block>();

	public static void forbidBlock(Block b) {
		FORBIDDEN_BLOCKS.add(b);
	}

	@Override
	public boolean isSeed(ItemStack stack) {
		if (stack.getItem() instanceof IPlantable) {
			return true;
		}

		if (stack.getItem() instanceof ItemBlock) {
			Block block = ((ItemBlock) stack.getItem()).field_150939_a;
			if (block instanceof IPlantable && !FORBIDDEN_BLOCKS.contains(block)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canSustainPlant(World world, ItemStack seed, int x, int y, int z) {
		if (seed.getItem() instanceof IPlantable) {
			return world.getBlock(x, y, z).canSustainPlant(world, x, y, z, ForgeDirection.UP,
					(IPlantable) seed.getItem())
					&& world.isAirBlock(x, y + 1, z);
		} else {
			Block block = world.getBlock(x, y, z);
			IPlantable plantable = (IPlantable) ((ItemBlock) seed.getItem()).field_150939_a;
			return block.canSustainPlant(world, x, y, z, ForgeDirection.UP, plantable)
					&& block != ((ItemBlock) seed.getItem()).field_150939_a
					&& world.isAirBlock(x, y + 1, z);
		}
	}

	@Override
	public boolean plantCrop(World world, EntityPlayer player, ItemStack seed, int x, int y, int z) {
		return BlockUtils.useItemOnBlock(world, player, seed, x, y, z, ForgeDirection.UP);
	}

	@Override
	public boolean isMature(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z) {
		if (block == null || FORBIDDEN_BLOCKS.contains(block)) {
			return false;
		} else if (block instanceof BlockTallGrass
				|| block instanceof BlockMelon
				|| block instanceof BlockMushroom
				|| block instanceof BlockDoublePlant
				|| block == Blocks.pumpkin) {
			return true;
		} else if (block instanceof BlockCrops) {
			return meta == 7;
		} else if (block instanceof BlockNetherWart) {
			return meta == 3;
		} else if (block instanceof IPlantable) {
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
