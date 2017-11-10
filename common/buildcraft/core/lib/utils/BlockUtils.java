/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.utils;

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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import buildcraft.BuildCraftCore;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.core.proxy.CoreProxy;

public final class BlockUtils {
	/**
	 * Deactivate constructor
	 */
	private BlockUtils() {
	}

	public static List<ItemStack> getItemStackFromBlock(WorldServer world, int i, int j, int k) {
		Block block = world.getBlock(i, j, k);

		if (block == null || block.isAir(world, i, j, k)) {
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

	public static boolean breakBlock(WorldServer world, int x, int y, int z) {
		return breakBlock(world, x, y, z, BuildCraftCore.itemLifespan * 20);
	}

	public static boolean breakBlock(WorldServer world, int x, int y, int z, int forcedLifespan) {
		List<ItemStack> items = new ArrayList<ItemStack>();

		if (breakBlock(world, x, y, z, items)) {
			for (ItemStack item : items) {
				dropItem(world, x, y, z, forcedLifespan, item);
			}
			return true;
		}
		return false;
	}

	public static EntityPlayer getFakePlayerWithTool(WorldServer world, int x, int y, int z, ItemStack tool) {
		EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer(world, x, y, z).get();
		int i = 0;

		while (player.getHeldItem() != tool && i < 9) {
			if (i > 0) {
				player.inventory.setInventorySlotContents(i - 1, null);
			}

			player.inventory.setInventorySlotContents(i, tool);
			i++;
		}

		return player;
	}

	public static boolean harvestBlock(WorldServer world, int x, int y, int z, ItemStack tool) {
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);

		EntityPlayer player = getFakePlayerWithTool(world, x, y, z, tool);

		BreakEvent breakEvent = new BreakEvent(x, y, z, world, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z), player);
		MinecraftForge.EVENT_BUS.post(breakEvent);

		if (breakEvent.isCanceled()) {
			return false;
		}

		if (!block.canHarvestBlock(player, meta)) {
			return false;
		}

		if (tool != null && tool.getItem() != null && tool.getItem().onBlockStartBreak(tool, x, y, z, player)) {
			return false;
		}

		block.onBlockHarvested(world, x, y, z, meta, player);
		block.harvestBlock(world, player, x, y, z, meta);
		world.setBlockToAir(x, y, z);

		return true;
	}

	public static boolean breakBlock(WorldServer world, int x, int y, int z, List<ItemStack> drops) {
		BreakEvent breakEvent = new BreakEvent(x, y, z, world, world.getBlock(x, y, z),
				world.getBlockMetadata(x, y, z), CoreProxy.proxy.getBuildCraftPlayer(world).get());
		MinecraftForge.EVENT_BUS.post(breakEvent);

		if (breakEvent.isCanceled()) {
			return false;
		}

		if (!world.isAirBlock(x, y, z) && !world.isRemote
				&& world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
			drops.addAll(getItemStackFromBlock(world, x, y, z));
		}
		world.setBlockToAir(x, y, z);

		return true;
	}

	public static void dropItem(WorldServer world, int x, int y, int z, int forcedLifespan, ItemStack stack) {
		float var = 0.7F;
		double dx = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
		double dy = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
		double dz = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
		EntityItem entityitem = new EntityItem(world, x + dx, y + dy, z + dz, stack);

		entityitem.lifespan = forcedLifespan;
		entityitem.delayBeforeCanPickup = 10;

		world.spawnEntityInWorld(entityitem);
	}

	public static boolean canChangeBlock(World world, int x, int y, int z) {
		return canChangeBlock(world.getBlock(x, y, z), world, x, y, z);
	}

	public static boolean canChangeBlock(Block block, World world, int x, int y, int z) {
		if (block == null || block.isAir(world, x, y, z)) {
			return true;
		}

		if (isUnbreakableBlock(world, x, y, z, block)) {
			return false;
		}

		if (block == Blocks.lava || block == Blocks.flowing_lava) {
			return false;
		} else if (block instanceof IFluidBlock && ((IFluidBlock) block).getFluid() != null) {
			Fluid f = ((IFluidBlock) block).getFluid();
			if (f.getDensity(world, x, y, z) >= 3000) {
				return false;
			}
		}

		return true;
	}

	public static float getBlockHardnessMining(World world, int x, int y, int z, Block b, ItemStack tool) {
		if (world instanceof WorldServer && !BuildCraftCore.miningAllowPlayerProtectedBlocks) {
			float relativeHardness = b.getPlayerRelativeBlockHardness(getFakePlayerWithTool((WorldServer) world, x, y, z, tool), world, x, y, z);

			if (relativeHardness <= 0.0F) { // Forge's getPlayerRelativeBlockHardness hook returns 0.0F if the hardness is < 0.0F.
				return -1.0F;
			}
		}

		return b.getBlockHardness(world, x, y, z);
	}

	public static boolean isUnbreakableBlock(World world, int x, int y, int z, Block b) {
		if (b == null) {
			return false;
		}

		return getBlockHardnessMining(world, x, y, z, b, null) < 0;
	}

	public static boolean isUnbreakableBlock(World world, int x, int y, int z) {
		return isUnbreakableBlock(world, x, y, z, world.getBlock(x, y, z));
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
		if (block instanceof IFluidBlock) {
			return ((IFluidBlock) block).getFilledPercentage(world, x, y, z) == 1.0f;
		} else if (block instanceof BlockStaticLiquid) {
			return world.getBlockMetadata(x, y, z) == 0;
		}
		return false;
	}

	public static Fluid getFluid(Block block) {
		if (block instanceof IFluidBlock) {
			return ((IFluidBlock) block).getFluid();
		} else {
			return FluidRegistry.lookupFluidForBlock(block);
		}
	}

	public static FluidStack drainBlock(World world, int x, int y, int z, boolean doDrain) {
		return drainBlock(world.getBlock(x, y, z), world, x, y, z, doDrain);
	}

	public static FluidStack drainBlock(Block block, World world, int x, int y, int z, boolean doDrain) {
		if (block instanceof IFluidBlock) {
			IFluidBlock fluidBlock = (IFluidBlock) block;
			if (!fluidBlock.canDrain(world, x, y, z)) {
				return null;
			}
			return fluidBlock.drain(world, x, y, z, doDrain);
		} else {
			Fluid fluid = getFluid(block);

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

	public static int computeBlockBreakEnergy(World world, int x, int y, int z) {
		return (int) Math.floor(BuilderAPI.BREAK_ENERGY * BuildCraftCore.miningMultiplier * ((world.getBlock(x, y, z).getBlockHardness(world, x, y, z) + 1) * 2));
	}

	/**
	 * The following functions let you avoid unnecessary chunk loads, which is nice.
	 */
	public static TileEntity getTileEntity(World world, int x, int y, int z) {
		return getTileEntity(world, x, y, z, false);
	}

	public static TileEntity getTileEntity(World world, int x, int y, int z, boolean force) {
		if (!force) {
			if (y < 0 || y > 255) {
				return null;
			}
			Chunk chunk = ThreadSafeUtils.getChunk(world, x >> 4, z >> 4);
			return chunk != null ? chunk.getTileEntityUnsafe(x & 15, y, z & 15) : null;
		} else {
			return world.getTileEntity(x, y, z);
		}
	}

	public static Block getBlock(World world, int x, int y, int z) {
		return getBlock(world, x, y, z, false);
	}

	public static Block getBlock(World world, int x, int y, int z, boolean force) {
		if (!force) {
			if (y < 0 || y > 255) {
				return Blocks.air;
			}
			Chunk chunk = ThreadSafeUtils.getChunk(world, x >> 4, z >> 4);
			return chunk != null ? chunk.getBlock(x & 15, y, z & 15) : Blocks.air;
		} else {
			return world.getBlock(x, y, z);
		}
	}

	public static int getBlockMetadata(World world, int x, int y, int z) {
		return getBlockMetadata(world, x, y, z, false);

	}

	public static int getBlockMetadata(World world, int x, int y, int z, boolean force) {
		if (!force) {
			if (y < 0 || y > 255) {
				return 0;
			}
			Chunk chunk = ThreadSafeUtils.getChunk(world, x >> 4, z >> 4);
			return chunk != null ? chunk.getBlockMetadata(x & 15, y, z & 15) : 0;
		} else {
			return world.getBlockMetadata(x, y, z);
		}
	}

	public static boolean useItemOnBlock(World world, EntityPlayer player, ItemStack stack, int x,
										 int y, int z, ForgeDirection direction) {
		boolean done = stack.getItem().onItemUseFirst(stack, player, world, x, y, z,
				direction.ordinal(), 0.5F, 0.5F, 0.5F);

		if (!done) {
			done = stack.getItem().onItemUse(stack, player, world, x, y, z, direction.ordinal(),
					0.5F, 0.5F, 0.5F);
		}
		return done;
	}

	public static void onComparatorUpdate(World world, int x, int y, int z, Block block) {
		world.func_147453_f(x, y, z, block);
	}

	public static TileEntityChest getOtherDoubleChest(TileEntity inv) {
		if (inv instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) inv;

			TileEntityChest adjacent = null;

			if (chest.adjacentChestXNeg != null) {
				adjacent = chest.adjacentChestXNeg;
			}

			if (chest.adjacentChestXPos != null) {
				adjacent = chest.adjacentChestXPos;
			}

			if (chest.adjacentChestZNeg != null) {
				adjacent = chest.adjacentChestZNeg;
			}

			if (chest.adjacentChestZPos != null) {
				adjacent = chest.adjacentChestZPos;
			}

			return adjacent;
		}
		return null;
	}
}
