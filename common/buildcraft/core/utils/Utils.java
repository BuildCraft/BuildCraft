/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
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
import buildcraft.core.proxy.CoreProxy;
import buildcraft.energy.TileEngine;

public final class Utils {

	public static final Random RANDOM = new Random();
	private static final List<EnumFacing> directions = new ArrayList<EnumFacing>(Arrays.asList(EnumFacing.values()));

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
	 * @param sourcePos
	 * @return amount used
	 */
	public static int addToRandomInventoryAround(World world, BlockPos sourcePos, ItemStack stack) {
		Collections.shuffle(directions);
		for (EnumFacing orientation : directions) {
			BlockPos pos = sourcePos.offset(orientation);

			TileEntity tileInventory = world.getTileEntity(pos);
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
	public static EnumFacing get2dOrientation(EntityLivingBase entityliving) {
		EnumFacing[] orientationTable = { EnumFacing.SOUTH,
				EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST };
		int orientationIndex = MathHelper.floor_double((entityliving.rotationYaw + 45.0) / 90.0) & 3;
		return orientationTable[orientationIndex];
	}

	/*
	 * FIXME This is only kept here for the purpose of get3dOrientation, which
	 * should probably be removed following the same principles
	 */
	@Deprecated
	private static EnumFacing get2dOrientation(Position pos1, Position pos2) {
		double dX = pos1.x - pos2.x;
		double dZ = pos1.z - pos2.z;
		double angle = Math.atan2(dZ, dX) / Math.PI * 180 + 180;

		if (angle < 45 || angle > 315) {
			return EnumFacing.EAST;
		} else if (angle < 135) {
			return EnumFacing.SOUTH;
		} else if (angle < 225) {
			return EnumFacing.WEST;
		} else {
			return EnumFacing.NORTH;
		}
	}

	public static EnumFacing get3dOrientation(Position pos1, Position pos2) {
		double dX = pos1.x - pos2.x;
		double dY = pos1.y - pos2.y;
		double angle = Math.atan2(dY, dX) / Math.PI * 180 + 180;

		if (angle > 45 && angle < 135) {
			return EnumFacing.UP;
		} else if (angle > 225 && angle < 315) {
			return EnumFacing.DOWN;
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
	public static int addToRandomPipeAround(World world, BlockPos sourcePos, EnumFacing from, ItemStack stack) {
		List<IPipeTile> possiblePipes = new ArrayList<IPipeTile>();
		List<EnumFacing> pipeDirections = new ArrayList<EnumFacing>();

		for (EnumFacing side : EnumFacing.values()) {
			if (from.getOpposite() == side) {
				continue;
			}

			BlockPos pos = sourcePos.offset(side);
			TileEntity tile = world.getTileEntity(pos);

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

	public static TileEntity getTile(World world, BlockPos sourcePos, EnumFacing step) {
		return world.getTileEntity(sourcePos.offset(step));
	}

	public static IAreaProvider getNearbyAreaProvider(World world, BlockPos pos) {
		TileEntity a1 = world.getTileEntity(pos.offset(EnumFacing.EAST));
		TileEntity a2 = world.getTileEntity(pos.offset(EnumFacing.WEST));
		TileEntity a3 = world.getTileEntity(pos.offset(EnumFacing.SOUTH));
		TileEntity a4 = world.getTileEntity(pos.offset(EnumFacing.NORTH));
		TileEntity a5 = world.getTileEntity(pos.offset(EnumFacing.UP));
		TileEntity a6 = world.getTileEntity(pos.offset(EnumFacing.DOWN));

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

	public static void preDestroyBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof IInventory && !world.isRemote) {
			if (!(tile instanceof IDropControlInventory) || ((IDropControlInventory) tile).doDrop()) {
				InvUtils.dropItems(world, (IInventory) tile, pos);
				InvUtils.wipeInventory((IInventory) tile);
			}
		}

		if (tile instanceof TileBuildCraft) {
			((TileBuildCraft) tile).destroy();
		}
	}

	public static boolean isFakePlayer(EntityPlayer player) {
		if (player instanceof FakePlayer) {
			return true;
		}

		// Tip donated by skyboy - addedToChunk must be set to false by a fake player
		// or it becomes a chunk-loading entity.
		if (!player.addedToChunk) {
			return true;
		}

		return false;
	}

	public static boolean checkPipesConnections(TileEntity tile1, TileEntity tile2) {
		if (tile1 == null || tile2 == null) {
			return false;
		}

		if (!(tile1 instanceof IPipeTile) && !(tile2 instanceof IPipeTile)) {
			return false;
		}

		BlockPos pos1 = tile1.getPos();
		BlockPos pos2 = tile2.getPos();

		EnumFacing o = null;

		if (pos1.getX() - 1 == pos2.getX()) {
			o = EnumFacing.WEST;
		} else if (pos1.getX() + 1 == pos2.getX()) {
			o = EnumFacing.EAST;
		} else if (pos1.getY() - 1 == pos2.getY()) {
			o = EnumFacing.DOWN;
		} else if (pos1.getY() + 1 == pos2.getY()) {
			o = EnumFacing.UP;
		} else if (pos1.getZ() - 1 == pos2.getZ()) {
			o = EnumFacing.NORTH;
		} else if (pos1.getZ() + 1 == pos2.getZ()) {
			o = EnumFacing.SOUTH;
		}

		if (tile1 instanceof IPipeTile && !((IPipeTile) tile1).isPipeConnected(o)) {
			return false;
		}

		if (tile2 instanceof IPipeTile && !((IPipeTile) tile2).isPipeConnected(o.getOpposite())) {
			return false;
		}

		return true;
	}

	public static boolean checkLegacyPipesConnections(IBlockAccess blockAccess, BlockPos pos1, BlockPos pos2) {

		Block b1 = blockAccess.getBlockState(pos1).getBlock();
		Block b2 = blockAccess.getBlockState(pos2).getBlock();

		if (!(b1 instanceof IFramePipeConnection) && !(b2 instanceof IFramePipeConnection)) {
			return false;
		}

		if (b1 instanceof IFramePipeConnection && !((IFramePipeConnection) b1).isPipeConnected(blockAccess, pos1, pos2)) {
			return false;
		}

		if (b2 instanceof IFramePipeConnection && !((IFramePipeConnection) b2).isPipeConnected(blockAccess, pos2, pos1)) {
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
			if (str == null) {
				data.writeInt(0);
				return;
			}
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
			if (len == 0) {
				return "";
			}
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
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			CompressedStreamTools.writeCompressed(nbt, out);
			out.flush();
			byte[] compressed = out.toByteArray();
			data.writeInt(compressed.length);
			data.writeBytes(compressed);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static NBTTagCompound readNBT(ByteBuf data) {
		try {
			int length = data.readInt();
			byte[] compressed = new byte[length];
			data.readBytes(compressed);
			return CompressedStreamTools.readCompressed(new ByteArrayInputStream(compressed));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeStack (ByteBuf data, ItemStack stack) {
		if (stack == null || stack.getItem() == null || stack.stackSize < 0) {
			data.writeByte(0);
		} else {
			// ItemStacks generally shouldn't have a stackSize above 64,
			// so we use this "trick" to save bandwidth by storing it in the first byte.
			data.writeByte((MathUtils.clamp(stack.stackSize + 1, 0, 64) & 0x7F) | (stack.hasTagCompound() ? 128 : 0));
			data.writeShort(Item.getIdFromItem(stack.getItem()));
			data.writeShort(stack.getItemDamage());
			if (stack.hasTagCompound()) {
				Utils.writeNBT(data, stack.getTagCompound());
			}
		}
	}

	public static ItemStack readStack(ByteBuf data) {
		int flags = data.readUnsignedByte();
		if (flags == 0) {
			return null;
		} else {
			boolean hasCompound = (flags & 0x80) != 0;
			int stackSize = (flags & 0x7F) - 1;
			int itemId = data.readUnsignedShort();
			int itemDamage = data.readShort();
			ItemStack stack = new ItemStack(Item.getItemById(itemId), stackSize, itemDamage);
			if (hasCompound) {
				stack.setTagCompound(Utils.readNBT(data));
			}
			return stack;
		}
	}

	public static void writeByteArray(ByteBuf stream, byte[] data) {
		stream.writeInt(data.length);
		stream.writeBytes(data);
	}

	public static byte[] readByteArray(ByteBuf stream) {
		byte[] data = new byte[stream.readInt()];
		stream.readBytes(data, 0, data.length);
		return data;
	}

	public static void writeBlockPos(ByteBuf stream, BlockPos pos) {
		stream.writeInt(pos.getX());
		stream.writeShort(pos.getY());
		stream.writeInt(pos.getZ());
	}

	public static BlockPos readBlockPos(ByteBuf stream) {
		return new BlockPos(stream.readInt(), stream.readShort(), stream.readInt());
	}

	public static void writeBlockPos(NBTTagCompound compound, BlockPos pos) {
		compound.setInteger("x", pos.getX());
		compound.setShort("y", (short) pos.getY());
		compound.setInteger("z", pos.getZ());
	}

	public static BlockPos readBlockPos(NBTTagCompound compound) {
		return new BlockPos(compound.getInteger("x"), compound.getShort("y"), compound.getInteger("z"));
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

		return new FMLProxyPacket(new PacketBuffer(buf), DefaultProps.NET_CHANNEL_NAME + "-CORE");
	}

	public static ItemStack getItemStack(IBlockState state, int quantity) {
		return new ItemStack(state.getBlock(), quantity, state.getBlock().damageDropped(state));
	}

	public static ItemStack getItemStack(IBlockState state) {
		return getItemStack(state, 1);
	}

	public static boolean nextTo(BlockPos pos, BlockPos pos1) {
		for (EnumFacing dir : EnumFacing.values()) {
			if (pos.offset(dir).equals(pos1)) {
				return true;
			}
		}
		return false;
	}

	public static String getBlockName(Block block) {
		ResourceLocation location = ((ResourceLocation) Block.blockRegistry.getNameForObject(block));
		if (location == null) {
			return null;
		}
		return location.getResourceDomain() + ":" + location.getResourcePath();
	}

	public static String getItemName(Item item) {
		ResourceLocation location = ((ResourceLocation) Item.itemRegistry.getNameForObject(item));
		if (location == null) {
			return null;
		}
		return location.getResourceDomain() + ":" + location.getResourcePath();
	}

	// WORLD HELPER
	
    /**
     * Checks between a min and max all the chunks inbetween actually exist. Args: world, minX, minY, minZ, maxX, maxY, maxZ
     */
    public static boolean checkChunksExist(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        if (maxY >= 0 && minY < 256)
        {
            minX >>= 4;
            minZ >>= 4;
            maxX >>= 4;
            maxZ >>= 4;

            for (int var7 = minX; var7 <= maxX; ++var7)
            {
                for (int var8 = minZ; var8 <= maxZ; ++var8)
                {
                    if (!world.getChunkProvider().chunkExists(var7, var8))
                    {
                        return false;
                    }
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }
}
