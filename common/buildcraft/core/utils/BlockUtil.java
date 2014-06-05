/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.core.proxy.CoreProxy;

public final class BlockUtil {

	/**
	 * Deactivate constructor
	 */
	private BlockUtil() {
	}

	public static List<ItemStack> getItemStackFromBlock(WorldServer world, int i, int j, int k) {
		Block block = world.getBlock(i, j, k);

		if (block == null) {
			return null;
		}

		if (block.isAir(world, i, j, k)) {
			return null;
		}

		int meta = world.getBlockMetadata(i, j, k);

		ArrayList<ItemStack> dropsList = block.getDrops(world, i, j, k, meta, 0);
		float dropChance = ForgeEventFactory.fireBlockHarvesting(dropsList, world, block, i, j, k, meta, 0, 1.0F,
				false, CoreProxy.proxy.getBuildCraftPlayer(world).get());

		ArrayList<ItemStack> returnList = new ArrayList<ItemStack>();
		for (ItemStack s : dropsList) {
			if (world.rand.nextFloat() <= dropChance) {
				returnList.add(s);
			}
		}

		return returnList;
	}

	public static void breakBlock(WorldServer world, int x, int y, int z) {
		breakBlock(world, x, y, z, BuildCraftCore.itemLifespan);
	}

	public static void breakBlock(WorldServer world, int x, int y, int z, int forcedLifespan) {
		if (!world.isAirBlock(x, y, z) && BuildCraftCore.dropBrokenBlocks && !world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
			List<ItemStack> items = getItemStackFromBlock(world, x, y, z);

			for (ItemStack item : items) {
				float var = 0.7F;
				double dx = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
				double dy = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
				double dz = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
				EntityItem entityitem = new EntityItem(world, x + dx, y + dy, z + dz, item);

				entityitem.lifespan = forcedLifespan;
				entityitem.delayBeforeCanPickup = 10;

				world.spawnEntityInWorld(entityitem);
			}
		}

		world.setBlockToAir(x, y, z);
	}

	public static boolean isAnObstructingBlock(Block block, World world, int x, int y, int z) {
		if (block == null || block.isAir(world, x, y, z)) {
			return false;
		}
		return true;
	}

	public static boolean canChangeBlock(World world, int x, int y, int z) {
		return canChangeBlock(world.getBlock(x, y, z), world, x, y, z);
	}

	public static boolean canChangeBlock(Block block, World world, int x, int y, int z) {
		if (block == null || block.isAir(world, x, y, z)) {
			return true;
		}

		if (block.getBlockHardness(world, x, y, z) < 0) {
			return false;
		}

		if (block == BuildCraftEnergy.blockOil) {
			return false;
		}

		if (block == Blocks.lava || block == Blocks.flowing_lava) {
			return false;
		}

		return true;
	}

	public static boolean isUnbreakableBlock(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);

		return b != null && b.getBlockHardness(world, x, y, z) < 0;
	}

	/**
	 * Returns true if a block cannot be harvested without a tool.
	 */
	public static boolean isToughBlock(World world, int x, int y, int z) {
		return !world.getBlock(x, y, z).getMaterial().isToolNotRequired();
	}

	public static boolean isFullFluidBlock(World world, int x, int y, int z) {
		return isFullFluidBlock(world.getBlock(x, y, z), world, x, y, z);
	}

	public static boolean isFullFluidBlock(Block block, World world, int x, int y, int z) {
		if (block instanceof IFluidBlock || block instanceof BlockStaticLiquid) {
			return world.getBlockMetadata(x, y, z) == 0;
		}
		return false;
	}

	public static Fluid getFluid(Block block) {
		return FluidRegistry.lookupFluidForBlock (block);
	}

	public static FluidStack drainBlock(World world, int x, int y, int z, boolean doDrain) {
		return drainBlock(world.getBlock(x, y, z), world, x, y, z, doDrain);
	}

	public static FluidStack drainBlock(Block block, World world, int x, int y, int z, boolean doDrain) {
		Fluid fluid = FluidRegistry.lookupFluidForBlock(block);

		if (fluid != null && FluidRegistry.isFluidRegistered(fluid)) {
			int meta = world.getBlockMetadata(x, y, z);

			if (meta != 0) {
				return null;
			}

			if (doDrain) {
				world.setBlockToAir(x, y, z);
			}

			return new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
		} else {
			return null;
		}
	}

	/**
	 * Create an explosion which only affects a single block.
	 */
	@SuppressWarnings("unchecked")
	public static void explodeBlock(World world, int x, int y, int z) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}

		Explosion explosion = new Explosion(world, null, x + .5, y + .5, z + .5, 3f);
		explosion.affectedBlockPositions.add(new ChunkPosition(x, y, z));
		explosion.doExplosionB(true);

		for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
			if (!(player instanceof EntityPlayerMP)) {
				continue;
			}

			if (player.getDistanceSq(x, y, z) < 4096) {
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S27PacketExplosion(x + .5, y + .5, z + .5, 3f, explosion.affectedBlockPositions, null));
			}
		}
	}
}
