/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.BlockIndex;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityBlock;
import buildcraft.core.IDropControlInventory;
import buildcraft.core.IFramePipeConnection;
import buildcraft.core.LaserData;
import buildcraft.core.LaserKind;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.ISynchronizedTile;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.energy.TileEngine;

public final class Utils {

	public static final Random RANDOM = new Random();
	private static final List<ForgeDirection> directions = new ArrayList<ForgeDirection>(Arrays.asList(ForgeDirection.VALID_DIRECTIONS));

	/**
	 * Deactivate constructor
	 */
	private Utils() {
	}

	/* IINVENTORY HELPERS */
	/**
	 * Tries to add the passed stack to any valid inventories around the given
	 * coordinates.
	 *
	 * @param stack
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return amount used
	 */
	public static int addToRandomInventoryAround(World world, int x, int y, int z, ItemStack stack) {
		Collections.shuffle(directions);
		for (ForgeDirection orientation : directions) {
			Position pos = new Position(x, y, z, orientation);
			pos.moveForwards(1.0);

			TileEntity tileInventory = world.getTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
			ITransactor transactor = Transactor.getTransactorFor(tileInventory);
			if (transactor != null && !(tileInventory instanceof TileEngine) && transactor.add(stack, orientation.getOpposite(), false).stackSize > 0) {
				return transactor.add(stack, orientation.getOpposite(), true).stackSize;
			}
		}
		return 0;

	}

	/**
	 * Returns the cardinal direction of the entity depending on its
	 * rotationYaw
	 */
	public static ForgeDirection get2dOrientation(EntityLivingBase entityliving) {
		ForgeDirection[] orientationTable = { ForgeDirection.SOUTH,
				ForgeDirection.WEST, ForgeDirection.NORTH, ForgeDirection.EAST };
		int orientationIndex = MathHelper.floor_double((entityliving.rotationYaw + 45.0) / 90.0) & 3;
		return orientationTable[orientationIndex];
	}

	/*
	 * FIXME This is only kept here for the purpose of get3dOrientation, which
	 * should probably be removed following the same principles
	 */
	@Deprecated
	private static ForgeDirection get2dOrientation(Position pos1, Position pos2) {
		double dX = pos1.x - pos2.x;
		double dZ = pos1.z - pos2.z;
		double angle = Math.atan2(dZ, dX) / Math.PI * 180 + 180;

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
		double dX = pos1.x - pos2.x;
		double dY = pos1.y - pos2.y;
		double angle = Math.atan2(dY, dX) / Math.PI * 180 + 180;

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
	public static int addToRandomPipeAround(World world, int x, int y, int z, ForgeDirection from, ItemStack stack) {
		List<IPipeTile> possiblePipes = new ArrayList<IPipeTile>();
		List<ForgeDirection> pipeDirections = new ArrayList<ForgeDirection>();

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (from.getOpposite() == side) {
				continue;
			}

			Position pos = new Position(x, y, z, side);

			pos.moveForwards(1.0);

			TileEntity tile = world.getTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

			if (tile instanceof IPipeTile) {
				IPipeTile pipe = (IPipeTile) tile;
				if (pipe.getPipeType() != PipeType.ITEM) {
					continue;
				}
				if (!pipe.isPipeConnected(side.getOpposite())) {
					continue;
				}

				possiblePipes.add(pipe);
				pipeDirections.add(side.getOpposite());
			}
		}

		if (possiblePipes.size() > 0) {
			int choice = RANDOM.nextInt(possiblePipes.size());

			IPipeTile pipeEntry = possiblePipes.get(choice);

			return pipeEntry.injectItem(stack, true, pipeDirections.get(choice));
		}
		return 0;
	}

	public static TileEntity getTile(World world, Position pos, ForgeDirection step) {
		Position tmp = new Position(pos);
		tmp.orientation = step;
		tmp.moveForwards(1.0);

		return world.getTileEntity((int) tmp.x, (int) tmp.y, (int) tmp.z);
	}

	public static IAreaProvider getNearbyAreaProvider(World world, int i, int j, int k) {
		TileEntity a1 = world.getTileEntity(i + 1, j, k);
		TileEntity a2 = world.getTileEntity(i - 1, j, k);
		TileEntity a3 = world.getTileEntity(i, j, k + 1);
		TileEntity a4 = world.getTileEntity(i, j, k - 1);
		TileEntity a5 = world.getTileEntity(i, j + 1, k);
		TileEntity a6 = world.getTileEntity(i, j - 1, k);

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
		block.setBrightness(210);

		world.spawnEntityInWorld(block);

		return block;
	}

	public static EntityBlock[] createLaserBox(World world, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, LaserKind kind) {
		EntityBlock[] lasers = new EntityBlock[12];
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

	public static LaserData[] createLaserDataBox(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax) {
		LaserData[] lasers = new LaserData[12];
		Position[] p = new Position[8];

		p[0] = new Position(xMin, yMin, zMin);
		p[1] = new Position(xMax, yMin, zMin);
		p[2] = new Position(xMin, yMax, zMin);
		p[3] = new Position(xMax, yMax, zMin);
		p[4] = new Position(xMin, yMin, zMax);
		p[5] = new Position(xMax, yMin, zMax);
		p[6] = new Position(xMin, yMax, zMax);
		p[7] = new Position(xMax, yMax, zMax);

		lasers[0] = new LaserData (p[0], p[1]);
		lasers[1] = new LaserData (p[0], p[2]);
		lasers[2] = new LaserData (p[2], p[3]);
		lasers[3] = new LaserData (p[1], p[3]);
		lasers[4] = new LaserData (p[4], p[5]);
		lasers[5] = new LaserData (p[4], p[6]);
		lasers[6] = new LaserData (p[5], p[7]);
		lasers[7] = new LaserData (p[6], p[7]);
		lasers[8] = new LaserData (p[0], p[4]);
		lasers[9] = new LaserData (p[1], p[5]);
		lasers[10] = new LaserData (p[2], p[6]);
		lasers[11] = new LaserData (p[3], p[7]);

		return lasers;
	}

	public static void handleBufferedDescription(ISynchronizedTile tileSynch) {
		TileEntity tile = (TileEntity) tileSynch;
		BlockIndex index = new BlockIndex(tile.xCoord, tile.yCoord, tile.zCoord);

		if (BuildCraftCore.bufferedDescriptions.containsKey(index)) {

			PacketUpdate payload = BuildCraftCore.bufferedDescriptions.get(index);
			BuildCraftCore.bufferedDescriptions.remove(index);

			try {
				tileSynch.handleDescriptionPacket(payload);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			tileSynch.postPacketHandling(payload);
		}
	}

	public static void preDestroyBlock(World world, int i, int j, int k) {
		TileEntity tile = world.getTileEntity(i, j, k);

		if (tile instanceof IInventory && !world.isRemote) {
			if (!(tile instanceof IDropControlInventory) || ((IDropControlInventory) tile).doDrop()) {
				InvUtils.dropItems(world, (IInventory) tile, i, j, k);
				InvUtils.wipeInventory((IInventory) tile);
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

		if (!(tile1 instanceof IPipeTile) && !(tile2 instanceof IPipeTile)) {
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

		if (tile1 instanceof IPipeTile && !((IPipeTile) tile1).isPipeConnected(o)) {
			return false;
		}

		if (tile2 instanceof IPipeTile && !((IPipeTile) tile2).isPipeConnected(o.getOpposite())) {
			return false;
		}

		return true;
	}

	public static boolean checkLegacyPipesConnections(IBlockAccess blockAccess, int x1, int y1, int z1, int x2, int y2, int z2) {

		Block b1 = blockAccess.getBlock(x1, y1, z1);
		Block b2 = blockAccess.getBlock(x2, y2, z2);

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

	public static int[] createSlotArray(int first, int count) {
		int[] slots = new int[count];
		for (int k = first; k < first + count; k++) {
			slots[k - first] = k;
		}
		return slots;
	}

	public static void writeUTF (ByteBuf data, String str) {
		try {
			byte [] b = str.getBytes("UTF-8");
			data.writeInt (b.length);
			data.writeBytes(b);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			data.writeInt (0);
		}
	}

	public static String readUTF (ByteBuf data) {
		try {
			int len = data.readInt();
			byte [] b = new byte [len];
			data.readBytes(b);
			return new String (b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeNBT (ByteBuf data, NBTTagCompound nbt) {
		try {
			byte[] compressed = CompressedStreamTools.compress(nbt);
			data.writeInt(compressed.length);
			data.writeBytes(compressed);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static NBTTagCompound readNBT(ByteBuf data) {
		try {
			int length = data.readInt();
			byte[] compressed = new byte[length];
			data.readBytes(compressed);
			return CompressedStreamTools.func_152457_a(compressed, NBTSizeTracker.field_152451_a);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeStack (ByteBuf data, ItemStack stack) {
		if (stack == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			NBTTagCompound nbt = new NBTTagCompound();
			stack.writeToNBT(nbt);
			Utils.writeNBT(data, nbt);
		}
	}


	public static ItemStack readStack(ByteBuf data) {
		if (!data.readBoolean()) {
			return null;
		} else {
			NBTTagCompound nbt = readNBT(data);
			return ItemStack.loadItemStackFromNBT(nbt);
		}
	}

	/**
	 * This subprogram transforms a packet into a FML packet to be send in the
	 * minecraft default packet mechanism. This always use BC-CORE as a
	 * channel, and as a result, should use discriminators declared there.
	 *
	 * WARNING! The implementation of this subprogram relies on the internal
	 * behavior of #FMLIndexedMessageToMessageCodec (in particular the encode
	 * member). It is probably opening a maintenance issue and should be
	 * replaced eventually by some more solid mechanism.
	 */
	public static FMLProxyPacket toPacket (BuildCraftPacket packet, int discriminator) {
		ByteBuf buf = Unpooled.buffer();

		buf.writeByte((byte) discriminator);
		packet.writeData(buf);

		return new FMLProxyPacket(buf, DefaultProps.NET_CHANNEL_NAME + "-CORE");
	}
}
