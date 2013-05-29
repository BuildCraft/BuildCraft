/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import java.util.Arrays;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquid;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.BlockIndex;
import buildcraft.core.EntityBlock;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.IDropControlInventory;
import buildcraft.core.IFramePipeConnection;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.network.ISynchronizedTile;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.energy.TileEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.NBTTagString;

public class Utils {

	public static final Random RANDOM = new Random();
	public static final float pipeMinPos = 0.25F;
	public static final float pipeMaxPos = 0.75F;
	public static float pipeNormalSpeed = 0.01F;
	private static final List<ForgeDirection> directions = new ArrayList<ForgeDirection>(Arrays.asList(ForgeDirection.VALID_DIRECTIONS));

	/**
	 * Tries to add the passed stack to any valid inventories around the given
	 * coordinates.
	 *
	 * @param stack
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return ItemStack representing what was added.
	 */
	public static ItemStack addToRandomInventory(ItemStack stack, World world, int x, int y, int z) {
		Collections.shuffle(directions);
		for (ForgeDirection orientation : directions) {
			Position pos = new Position(x, y, z, orientation);
			pos.moveForwards(1.0);

			TileEntity tileInventory = world.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
			ITransactor transactor = Transactor.getTransactorFor(tileInventory);
			if (transactor != null && !(tileInventory instanceof TileEngine) && transactor.add(stack, orientation.getOpposite(), false).stackSize > 0) {
				return transactor.add(stack, orientation.getOpposite(), true);
			}
		}

		ItemStack added = stack.copy();
		added.stackSize = 0;
		return added;

	}

	/**
	 * Depending on the kind of item in the pipe, set the floor at a different
	 * level to optimize graphical aspect.
	 */
	public static float getPipeFloorOf(ItemStack item) {
		return pipeMinPos;
	}

	public static ForgeDirection get2dOrientation(Position pos1, Position pos2) {
		double Dx = pos1.x - pos2.x;
		double Dz = pos1.z - pos2.z;
		double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;

		if (angle < 45 || angle > 315) {
			return ForgeDirection.EAST;
		} else if (angle < 135) {
			return ForgeDirection.SOUTH;
		} else if (angle < 225) {
			return ForgeDirection.WEST;
		} else {
			return ForgeDirection.NORTH;
		}
	}

	public static ForgeDirection get3dOrientation(Position pos1, Position pos2) {
		double Dx = pos1.x - pos2.x;
		double Dy = pos1.y - pos2.y;
		double angle = Math.atan2(Dy, Dx) / Math.PI * 180 + 180;

		if (angle > 45 && angle < 135) {
			return ForgeDirection.UP;
		} else if (angle > 225 && angle < 315) {
			return ForgeDirection.DOWN;
		} else {
			return get2dOrientation(pos1, pos2);
		}
	}

	/**
	 * Look around the tile given in parameter in all 6 position, tries to add
	 * the items to a random pipe entry around. Will make sure that the location
	 * from which the items are coming from (identified by the from parameter)
	 * isn't used again so that entities doesn't go backwards. Returns true if
	 * successful, false otherwise.
	 */
	public static boolean addToRandomPipeEntry(TileEntity tile, ForgeDirection from, ItemStack items) {
		World w = tile.worldObj;

		LinkedList<ForgeDirection> possiblePipes = new LinkedList<ForgeDirection>();

		for (int j = 0; j < 6; ++j) {
			if (from.getOpposite().ordinal() == j) {
				continue;
			}

			ForgeDirection o = ForgeDirection.values()[j];
			Position pos = new Position(tile.xCoord, tile.yCoord, tile.zCoord, o);

			pos.moveForwards(1.0);

			TileEntity pipeEntry = w.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

			if (pipeEntry instanceof IPipeEntry && ((IPipeEntry) pipeEntry).acceptItems()) {
				if (pipeEntry instanceof IPipeConnection) {
					if (!((IPipeConnection) pipeEntry).isPipeConnected(o.getOpposite())) {
						continue;
					}
				}
				possiblePipes.add(o);
			}
		}

		if (possiblePipes.size() > 0) {
			int choice = w.rand.nextInt(possiblePipes.size());

			Position entityPos = new Position(tile.xCoord, tile.yCoord, tile.zCoord, possiblePipes.get(choice));
			Position pipePos = new Position(tile.xCoord, tile.yCoord, tile.zCoord, possiblePipes.get(choice));

			entityPos.x += 0.5;
			entityPos.y += getPipeFloorOf(items);
			entityPos.z += 0.5;

			entityPos.moveForwards(0.5);

			pipePos.moveForwards(1.0);

			IPipeEntry pipeEntry = (IPipeEntry) w.getBlockTileEntity((int) pipePos.x, (int) pipePos.y, (int) pipePos.z);

			IPipedItem entity = new EntityPassiveItem(w, entityPos.x, entityPos.y, entityPos.z, items);

			pipeEntry.entityEntering(entity, entityPos.orientation);
			items.stackSize = 0;
			return true;
		} else {
			return false;
		}
	}

	public static void dropItems(World world, ItemStack stack, int i, int j, int k) {
		if (stack.stackSize <= 0) {
			return;
		}

		float f1 = 0.7F;
		double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		EntityItem entityitem = new EntityItem(world, i + d, j + d1, k + d2, stack);
		entityitem.delayBeforeCanPickup = 10;

		world.spawnEntityInWorld(entityitem);
	}

	public static void dropItems(World world, IInventory inventory, int i, int j, int k) {
		for (int l = 0; l < inventory.getSizeInventory(); ++l) {
			ItemStack items = inventory.getStackInSlot(l);

			if (items != null && items.stackSize > 0) {
				dropItems(world, inventory.getStackInSlot(l).copy(), i, j, k);
			}
		}
	}

	public static TileEntity getTile(World world, Position pos, ForgeDirection step) {
		Position tmp = new Position(pos);
		tmp.orientation = step;
		tmp.moveForwards(1.0);

		return world.getBlockTileEntity((int) tmp.x, (int) tmp.y, (int) tmp.z);
	}

	/**
	 * Ensures that the given inventory is the full inventory, i.e. takes double
	 * chests into account.
	 *
	 * @param inv
	 * @return Modified inventory if double chest, unmodified otherwise.
	 */
	public static IInventory getInventory(IInventory inv) {
		if (inv instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) inv;
			Position pos = new Position(chest.xCoord, chest.yCoord, chest.zCoord);
			TileEntity tile;
			IInventory chest2 = null;
			tile = Utils.getTile(chest.worldObj, pos, ForgeDirection.WEST);
			if (tile instanceof TileEntityChest) {
				chest2 = (IInventory) tile;
			}
			tile = Utils.getTile(chest.worldObj, pos, ForgeDirection.EAST);
			if (tile instanceof TileEntityChest) {
				chest2 = (IInventory) tile;
			}
			tile = Utils.getTile(chest.worldObj, pos, ForgeDirection.NORTH);
			if (tile instanceof TileEntityChest) {
				chest2 = (IInventory) tile;
			}
			tile = Utils.getTile(chest.worldObj, pos, ForgeDirection.SOUTH);
			if (tile instanceof TileEntityChest) {
				chest2 = (IInventory) tile;
			}
			if (chest2 != null) {
				return new InventoryLargeChest("", inv, chest2);
			}
		}
		return inv;
	}

	public static IAreaProvider getNearbyAreaProvider(World world, int i, int j, int k) {
		TileEntity a1 = world.getBlockTileEntity(i + 1, j, k);
		TileEntity a2 = world.getBlockTileEntity(i - 1, j, k);
		TileEntity a3 = world.getBlockTileEntity(i, j, k + 1);
		TileEntity a4 = world.getBlockTileEntity(i, j, k - 1);
		TileEntity a5 = world.getBlockTileEntity(i, j + 1, k);
		TileEntity a6 = world.getBlockTileEntity(i, j - 1, k);

		if (a1 instanceof IAreaProvider) {
			return (IAreaProvider) a1;
		}

		if (a2 instanceof IAreaProvider) {
			return (IAreaProvider) a2;
		}

		if (a3 instanceof IAreaProvider) {
			return (IAreaProvider) a3;
		}

		if (a4 instanceof IAreaProvider) {
			return (IAreaProvider) a4;
		}

		if (a5 instanceof IAreaProvider) {
			return (IAreaProvider) a5;
		}

		if (a6 instanceof IAreaProvider) {
			return (IAreaProvider) a6;
		}

		return null;
	}

	public static EntityBlock createLaser(World world, Position p1, Position p2, LaserKind kind) {
		if (p1.equals(p2)) {
			return null;
		}

		double iSize = p2.x - p1.x;
		double jSize = p2.y - p1.y;
		double kSize = p2.z - p1.z;

		double i = p1.x;
		double j = p1.y;
		double k = p1.z;

		if (iSize != 0) {
			i += 0.5;
			j += 0.45;
			k += 0.45;

			jSize = 0.10;
			kSize = 0.10;
		} else if (jSize != 0) {
			i += 0.45;
			j += 0.5;
			k += 0.45;

			iSize = 0.10;
			kSize = 0.10;
		} else if (kSize != 0) {
			i += 0.45;
			j += 0.45;
			k += 0.5;

			iSize = 0.10;
			jSize = 0.10;
		}

		EntityBlock block = CoreProxy.proxy.newEntityBlock(world, i, j, k, iSize, jSize, kSize, kind);

		world.spawnEntityInWorld(block);

		return block;
	}

	public static EntityBlock[] createLaserBox(World world, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, LaserKind kind) {
		EntityBlock lasers[] = new EntityBlock[12];
		Position[] p = new Position[8];

		p[0] = new Position(xMin, yMin, zMin);
		p[1] = new Position(xMax, yMin, zMin);
		p[2] = new Position(xMin, yMax, zMin);
		p[3] = new Position(xMax, yMax, zMin);
		p[4] = new Position(xMin, yMin, zMax);
		p[5] = new Position(xMax, yMin, zMax);
		p[6] = new Position(xMin, yMax, zMax);
		p[7] = new Position(xMax, yMax, zMax);

		lasers[0] = Utils.createLaser(world, p[0], p[1], kind);
		lasers[1] = Utils.createLaser(world, p[0], p[2], kind);
		lasers[2] = Utils.createLaser(world, p[2], p[3], kind);
		lasers[3] = Utils.createLaser(world, p[1], p[3], kind);
		lasers[4] = Utils.createLaser(world, p[4], p[5], kind);
		lasers[5] = Utils.createLaser(world, p[4], p[6], kind);
		lasers[6] = Utils.createLaser(world, p[5], p[7], kind);
		lasers[7] = Utils.createLaser(world, p[6], p[7], kind);
		lasers[8] = Utils.createLaser(world, p[0], p[4], kind);
		lasers[9] = Utils.createLaser(world, p[1], p[5], kind);
		lasers[10] = Utils.createLaser(world, p[2], p[6], kind);
		lasers[11] = Utils.createLaser(world, p[3], p[7], kind);

		return lasers;
	}

	public static void handleBufferedDescription(ISynchronizedTile tileSynch) {
		TileEntity tile = (TileEntity) tileSynch;
		BlockIndex index = new BlockIndex(tile.xCoord, tile.yCoord, tile.zCoord);

		if (BuildCraftCore.bufferedDescriptions.containsKey(index)) {

			PacketUpdate payload = BuildCraftCore.bufferedDescriptions.get(index);
			BuildCraftCore.bufferedDescriptions.remove(index);

			tileSynch.handleDescriptionPacket(payload);
			tileSynch.postPacketHandling(payload);
		}
	}

	public static int liquidId(int blockId) {
		if (blockId == Block.waterStill.blockID || blockId == Block.waterMoving.blockID) {
			return Block.waterStill.blockID;
		} else if (blockId == Block.lavaStill.blockID || blockId == Block.lavaMoving.blockID) {
			return Block.lavaStill.blockID;
		} else if (Block.blocksList[blockId] instanceof ILiquid) {
			return ((ILiquid) Block.blocksList[blockId]).stillLiquidId();
		} else {
			return 0;
		}
	}

	public static LiquidStack liquidFromBlockId(int blockId) {
		if (blockId == Block.waterStill.blockID || blockId == Block.waterMoving.blockID) {
			return new LiquidStack(Block.waterStill.blockID, LiquidContainerRegistry.BUCKET_VOLUME, 0);
		} else if (blockId == Block.lavaStill.blockID || blockId == Block.lavaMoving.blockID) {
			return new LiquidStack(Block.lavaStill.blockID, LiquidContainerRegistry.BUCKET_VOLUME, 0);
		} else if (Block.blocksList[blockId] instanceof ILiquid) {
			ILiquid liquid = (ILiquid) Block.blocksList[blockId];
			if (liquid.isMetaSensitive()) {
				return new LiquidStack(liquid.stillLiquidId(), LiquidContainerRegistry.BUCKET_VOLUME, liquid.stillLiquidMeta());
			} else {
				return new LiquidStack(liquid.stillLiquidId(), LiquidContainerRegistry.BUCKET_VOLUME, 0);
			}
		} else {
			return null;
		}
	}

	public static void preDestroyBlock(World world, int i, int j, int k) {
		TileEntity tile = world.getBlockTileEntity(i, j, k);

		if (tile instanceof IInventory && !CoreProxy.proxy.isRenderWorld(world)) {
			if (!(tile instanceof IDropControlInventory) || ((IDropControlInventory) tile).doDrop()) {
				dropItems(world, (IInventory) tile, i, j, k);
			}
		}

		if (tile instanceof TileBuildCraft) {
			((TileBuildCraft) tile).destroy();
		}
	}

	public static boolean checkPipesConnections(TileEntity tile1, TileEntity tile2) {
		if (tile1 == null || tile2 == null) {
			return false;
		}

		if (!(tile1 instanceof IPipeConnection) && !(tile2 instanceof IPipeConnection)) {
			return false;
		}

		ForgeDirection o = ForgeDirection.UNKNOWN;

		if (tile1.xCoord - 1 == tile2.xCoord) {
			o = ForgeDirection.WEST;
		} else if (tile1.xCoord + 1 == tile2.xCoord) {
			o = ForgeDirection.EAST;
		} else if (tile1.yCoord - 1 == tile2.yCoord) {
			o = ForgeDirection.DOWN;
		} else if (tile1.yCoord + 1 == tile2.yCoord) {
			o = ForgeDirection.UP;
		} else if (tile1.zCoord - 1 == tile2.zCoord) {
			o = ForgeDirection.NORTH;
		} else if (tile1.zCoord + 1 == tile2.zCoord) {
			o = ForgeDirection.SOUTH;
		}

		if (tile1 instanceof IPipeConnection && !((IPipeConnection) tile1).isPipeConnected(o)) {
			return false;
		}

		if (tile2 instanceof IPipeConnection && !((IPipeConnection) tile2).isPipeConnected(o.getOpposite())) {
			return false;
		}

		return true;
	}

	public static boolean checkPipesConnections(IBlockAccess blockAccess, TileEntity tile1, int x2, int y2, int z2) {
		TileEntity tile2 = blockAccess.getBlockTileEntity(x2, y2, z2);

		return checkPipesConnections(tile1, tile2);
	}

	public static boolean checkLegacyPipesConnections(IBlockAccess blockAccess, int x1, int y1, int z1, int x2, int y2, int z2) {

		Block b1 = Block.blocksList[blockAccess.getBlockId(x1, y1, z1)];
		Block b2 = Block.blocksList[blockAccess.getBlockId(x2, y2, z2)];

		if (!(b1 instanceof IFramePipeConnection) && !(b2 instanceof IFramePipeConnection)) {
			return false;
		}

		if (b1 instanceof IFramePipeConnection && !((IFramePipeConnection) b1).isPipeConnected(blockAccess, x1, y1, z1, x2, y2, z2)) {
			return false;
		}

		if (b2 instanceof IFramePipeConnection && !((IFramePipeConnection) b2).isPipeConnected(blockAccess, x2, y2, z2, x1, y1, z1)) {
			return false;
		}

		return true;

	}

	public static NBTTagCompound getItemData(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound("tag");
			stack.setTagCompound(nbt);
		}
		return nbt;
	}

	public static void addItemToolTip(ItemStack stack, String tag, String msg) {
		NBTTagCompound nbt = getItemData(stack);
		NBTTagCompound display = nbt.getCompoundTag("display");
		nbt.setCompoundTag("display", display);
		NBTTagList lore = display.getTagList("Lore");
		display.setTag("Lore", lore);
		lore.appendTag(new NBTTagString(tag, msg));
	}

	public static void writeInvToNBT(IInventory inv, String tag, NBTTagCompound data) {
		NBTTagList list = new NBTTagList();
		for (byte slot = 0; slot < inv.getSizeInventory(); slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			if (stack != null) {
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", slot);
				stack.writeToNBT(itemTag);
				list.appendTag(itemTag);
			}
		}
		data.setTag(tag, list);
	}

	public static void readInvFromNBT(IInventory inv, String tag, NBTTagCompound data) {
		NBTTagList list = data.getTagList(tag);
		for (byte entry = 0; entry < list.tagCount(); entry++) {
			NBTTagCompound itemTag = (NBTTagCompound) list.tagAt(entry);
			int slot = itemTag.getByte("Slot");
			if (slot >= 0 && slot < inv.getSizeInventory()) {
				ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
				inv.setInventorySlotContents(slot, stack);
			}
		}
	}

	public static void readStacksFromNBT(NBTTagCompound nbt, String name, ItemStack[] stacks) {
		NBTTagList nbttaglist = nbt.getTagList(name);

		for (int i = 0; i < stacks.length; ++i) {
			if (i < nbttaglist.tagCount()) {
				NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist.tagAt(i);

				stacks[i] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
			} else {
				stacks[i] = null;
			}
		}
	}

	public static void writeStacksToNBT(NBTTagCompound nbt, String name, ItemStack[] stacks) {
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < stacks.length; ++i) {
			NBTTagCompound cpt = new NBTTagCompound();
			nbttaglist.appendTag(cpt);
			if (stacks[i] != null) {
				stacks[i].writeToNBT(cpt);
			}

		}

		nbt.setTag(name, nbttaglist);
	}

	public static ItemStack consumeItem(ItemStack stack) {
		if (stack.stackSize == 1) {
			if (stack.getItem().hasContainerItem()) {
				return stack.getItem().getContainerItemStack(stack);
			} else {
				return null;
			}
		} else {
			stack.splitStack(1);

			return stack;
		}
	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static int[] concat(int[] first, int[] second) {
		int[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static float[] concat(float[] first, float[] second) {
		float[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static int[] createSlotArray(int first, int count) {
		int[] slots = new int[count];
		for (int k = first; k < first + count; k++) {
			slots[k - first] = k;
		}
		return slots;
	}
}
