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
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.FMLCommonHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.core.proxy.CoreProxy;

public final class BlockUtils {

	/**
	 * Deactivate constructor
	 */
	private BlockUtils() {
	}

	public static List<ItemStack> getItemStackFromBlock(WorldServer world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock().isAir(world, pos)) {
			return null;
		}

		List<ItemStack> dropsList = state.getBlock().getDrops(world, pos, state, 0);
		float dropChance = ForgeEventFactory.fireBlockHarvesting(dropsList, world, pos, state, 0, 1.0F,
				false, CoreProxy.proxy.getBuildCraftPlayer(world).get());

		ArrayList<ItemStack> returnList = new ArrayList<ItemStack>();
		for (ItemStack s : dropsList) {
			if (world.rand.nextFloat() <= dropChance) {
				returnList.add(s);
			}
		}

		return returnList;
	}

	public static boolean breakBlock(WorldServer world, BlockPos pos) {
		return breakBlock(world, pos, BuildCraftCore.itemLifespan);
	}

	public static boolean breakBlock(WorldServer world, BlockPos pos, int forcedLifespan) {
		BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos),
				CoreProxy.proxy.getBuildCraftPlayer(world).get());
		MinecraftForge.EVENT_BUS.post(breakEvent);

		if (breakEvent.isCanceled()) {
			return false;
		}

		if (!world.isAirBlock(pos) && BuildCraftCore.dropBrokenBlocks && !world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
			List<ItemStack> items = getItemStackFromBlock(world, pos);

			for (ItemStack item : items) {
				dropItem(world, pos, forcedLifespan, item);
			}
		}

		world.setBlockToAir(pos);

		return true;
	}

	public static void dropItem(WorldServer world, BlockPos pos, int forcedLifespan, ItemStack stack) {
		float var = 0.7F;
		double dx = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
		double dy = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
		double dz = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
		EntityItem entityitem = new EntityItem(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack);

		entityitem.lifespan = forcedLifespan;
		entityitem.setDefaultPickupDelay();

		world.spawnEntityInWorld(entityitem);
	}

	public static boolean isAnObstructingBlock(Block block, World world, BlockPos pos) {
		if (block == null || block.isAir(world, pos)) {
			return false;
		}
		return true;
	}

	public static boolean canChangeBlock(World world, BlockPos pos) {
		return canChangeBlock(world.getBlockState(pos).getBlock(), world, pos);
	}

	public static boolean canChangeBlock(Block block, World world, BlockPos pos) {
		if (block == null || block.isAir(world, pos)) {
			return true;
		}

		if (block.getBlockHardness(world, pos) < 0) {
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

	public static boolean isUnbreakableBlock(World world, BlockPos pos) {
		Block b = world.getBlockState(pos).getBlock();

		return b != null && b.getBlockHardness(world, pos) < 0;
	}

	/**
	 * Returns true if a block cannot be harvested without a tool.
	 */
	public static boolean isToughBlock(World world, BlockPos pos) {
		return !world.getBlockState(pos).getBlock().getMaterial().isToolNotRequired();
	}

	public static boolean isFullFluidBlock(World world, BlockPos pos) {
		return isFullFluidBlock(world.getBlockState(pos).getBlock(), world, pos);
	}

	public static boolean isFullFluidBlock(Block block, World world, BlockPos pos) {
		if (block instanceof IFluidBlock || block instanceof BlockStaticLiquid) {
			return ((Integer) world.getBlockState(pos).getValue(BlockFluidBase.LEVEL)).intValue() == 0;
		}
		return false;
	}

	public static Fluid getFluid(Block block) {
		return FluidRegistry.lookupFluidForBlock (block);
	}

	public static FluidStack drainBlock(World world, BlockPos pos, boolean doDrain) {
		return drainBlock(world.getBlockState(pos).getBlock(), world, pos, doDrain);
	}

	public static FluidStack drainBlock(Block block, World world, BlockPos pos, boolean doDrain) {
		Fluid fluid = FluidRegistry.lookupFluidForBlock(block);

		if (fluid != null && FluidRegistry.isFluidRegistered(fluid)) {
			int meta = ((Integer) world.getBlockState(pos).getValue(BlockFluidBase.LEVEL)).intValue();

			if (meta != 0) {
				return null;
			}

			if (doDrain) {
				world.setBlockToAir(pos);
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
	public static void explodeBlock(World world, BlockPos pos) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}

		List<BlockPos> positions = new ArrayList<BlockPos>();
		positions.add(pos);

		Explosion explosion = new Explosion(world, null, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 3f, positions);
		explosion.doExplosionB(true);

		for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
			if (!(player instanceof EntityPlayerMP)) {
				continue;
			}

			if (player.getDistanceSq(pos) < 4096) {
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S27PacketExplosion(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 3f, positions, null));
			}
		}
	}

	public static int computeBlockBreakEnergy(World world, BlockPos pos) {
		return (int) Math.floor(BuilderAPI.BREAK_ENERGY * BuildCraftFactory.miningMultiplier * ((world.getBlockState(pos).getBlock().getBlockHardness(world, pos) + 1) * 2));
	}
}
