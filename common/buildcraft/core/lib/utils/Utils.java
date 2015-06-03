/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.Position;
import buildcraft.api.power.IEngine;
import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.LaserKind;
import buildcraft.core.internal.IDropControlInventory;
import buildcraft.core.internal.IFramePipeConnection;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.network.Packet;

public final class Utils {
	public static final boolean CAULDRON_DETECTED;
	public static final XorShift128Random RANDOM = new XorShift128Random();
	private static final List<EnumFacing> directions = new ArrayList<EnumFacing>(Arrays.asList(EnumFacing.VALUES));

	static {
		boolean cauldron = false;
		try {
			cauldron = Utils.class.getClassLoader().loadClass("org.spigotmc.SpigotConfig") != null;
		} catch (ClassNotFoundException e) {

		}
		CAULDRON_DETECTED = cauldron;
	}

	/** Deactivate constructor */
	private Utils() {}

	/** Tries to add the passed stack to any valid inventories around the given coordinates.
	 *
	 * @param stack
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return amount used */
	public static int addToRandomInventoryAround(World world, BlockPos pos, ItemStack stack) {
		Collections.shuffle(directions);
		for (EnumFacing orientation : directions) {
			BlockPos newpos = pos.offset(orientation);

			TileEntity tile = world.getTileEntity(newpos);
			ITransactor transactor = Transactor.getTransactorFor(tile);
			if (transactor != null && !(tile instanceof IEngine) && transactor.add(stack, orientation.getOpposite(), false).stackSize > 0) {
				return transactor.add(stack, orientation.getOpposite(), true).stackSize;
			}
		}
		return 0;

	}

	/** Returns the cardinal direction of the entity depending on its rotationYaw */
	public static EnumFacing get2dOrientation(EntityLivingBase entityliving) {
		EnumFacing[] orientationTable = { EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST };
		int orientationIndex = MathHelper.floor_double((entityliving.rotationYaw + 45.0) / 90.0) & 3;
		return orientationTable[orientationIndex];
	}

	/** Look around the tile given in parameter in all 6 position, tries to add the items to a random injectable tile
	 * around. Will make sure that the location from which the items are coming from (identified by the from parameter)
	 * isn't used again so that entities doesn't go backwards. Returns true if successful, false otherwise. */
	public static int addToRandomInjectableAround(World world, BlockPos pos, EnumFacing from, ItemStack stack) {
		List<IInjectable> possiblePipes = new ArrayList<IInjectable>();
		List<EnumFacing> pipeDirections = new ArrayList<EnumFacing>();

		for (EnumFacing side : EnumFacing.VALUES) {
			if (from.getOpposite() == side) {
				continue;
			}

			BlockPos newpos = pos.offset(side);

			TileEntity tile = world.getTileEntity(newpos);

			if (tile instanceof IInjectable) {
				if (!((IInjectable) tile).canInjectItems(side.getOpposite())) {
					continue;
				}

				possiblePipes.add((IInjectable) tile);
				pipeDirections.add(side.getOpposite());
			} else {
				IInjectable wrapper = CompatHooks.INSTANCE.getInjectableWrapper(tile, side);
				if (wrapper != null) {
					possiblePipes.add(wrapper);
					pipeDirections.add(side.getOpposite());
				}
			}
		}

		if (possiblePipes.size() > 0) {
			int choice = RANDOM.nextInt(possiblePipes.size());

			IInjectable pipeEntry = possiblePipes.get(choice);

			return pipeEntry.injectItem(stack, true, pipeDirections.get(choice), null);
		}
		return 0;
	}

	public static void dropTryIntoPlayerInventory(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
		if (player != null && player.inventory.addItemStackToInventory(stack)) {
			if (player instanceof EntityPlayerMP) {
				((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
			}
		}
		InvUtils.dropItems(world, stack, pos);
	}

	public static IAreaProvider getNearbyAreaProvider(World world, int i, int j, int k) {
		for (TileEntity t : (List<TileEntity>) world.loadedTileEntityList) {
			if (t instanceof ITileAreaProvider && ((ITileAreaProvider) t).isValidFromLocation(i, j, k)) {
				return (IAreaProvider) t;
			}
		}

		return null;
	}

	public static EntityLaser createLaser(World world, Position p1, Position p2, LaserKind kind) {
		if (p1.equals(p2)) {
			return null;
		}
		EntityLaser block = new EntityLaser(world, p1, p2, kind);
		world.spawnEntityInWorld(block);
		return block;
	}

	public static EntityLaser[] createLaserBox(World world, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax,
			LaserKind kind) {
		EntityLaser[] lasers = new EntityLaser[12];
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

		lasers[0] = new LaserData(p[0], p[1]);
		lasers[1] = new LaserData(p[0], p[2]);
		lasers[2] = new LaserData(p[2], p[3]);
		lasers[3] = new LaserData(p[1], p[3]);
		lasers[4] = new LaserData(p[4], p[5]);
		lasers[5] = new LaserData(p[4], p[6]);
		lasers[6] = new LaserData(p[5], p[7]);
		lasers[7] = new LaserData(p[6], p[7]);
		lasers[8] = new LaserData(p[0], p[4]);
		lasers[9] = new LaserData(p[1], p[5]);
		lasers[10] = new LaserData(p[2], p[6]);
		lasers[11] = new LaserData(p[3], p[7]);

		return lasers;
	}

	public static void preDestroyBlock(World world, BlockPos pos) {
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

		EnumFacing o = null;

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (tile1.getPos().offset(facing).equals(tile2.getPos())) {
				o = facing;
				break;
			}
		}
		
		if (o == null) {
			return false;
		}

		if (tile1 instanceof IPipeTile && !((IPipeTile) tile1).isPipeConnected(o)) {
			return false;
		}

		if (tile2 instanceof IPipeTile && !((IPipeTile) tile2).isPipeConnected(o.getOpposite())) {
			return false;
		}

		return true;
	}

	/** Not required? */
	// TODO (AlexIIL) CHECK IF THIS IS REQUIRED
	@Deprecated
	public static boolean checkLegacyPipesConnections(IBlockAccess blockAccess, BlockPos bp1, BlockPos bp2) {

		IBlockState b1 = blockAccess.getBlockState(bp1);
		IBlockState b2 = blockAccess.getBlockState(bp2);

		if (!(b1 instanceof IFramePipeConnection) && !(b2 instanceof IFramePipeConnection)) {
			return false;
		}

		if (b1 instanceof IFramePipeConnection && !((IFramePipeConnection) b1).isPipeConnected(blockAccess, bp1, bp2)) {
			return false;
		}

		if (b2 instanceof IFramePipeConnection && !((IFramePipeConnection) b2).isPipeConnected(blockAccess, bp2, bp1)) {
			return false;
		}

		return true;

	}

	public static boolean isPipeConnected(IBlockAccess access, BlockPos pos, EnumFacing dir, IPipeTile.PipeType type) {
		TileEntity tile = access.getTileEntity(pos.offset(dir));
		return tile instanceof IPipeTile && ((IPipeTile) tile).getPipeType() == type && ((IPipeTile) tile).isPipeConnected(dir.getOpposite());
	}

	public static int[] createSlotArray(int first, int count) {
		int[] slots = new int[count];
		for (int k = first; k < first + count; k++) {
			slots[k - first] = k;
		}
		return slots;
	}

	/** This subprogram transforms a packet into a FML packet to be send in the minecraft default packet mechanism. This
	 * always use BC-CORE as a channel, and as a result, should use discriminators declared there.
	 *
	 * WARNING! The implementation of this subprogram relies on the internal behavior of
	 * #FMLIndexedMessageToMessageCodec (in particular the encode member). It is probably opening a maintenance issue
	 * and should be replaced eventually by some more solid mechanism. */
	public static FMLProxyPacket toPacket(Packet packet, int discriminator) {
		ByteBuf buf = Unpooled.buffer();

		buf.writeByte((byte) discriminator);
		packet.writeData(buf);

		return new FMLProxyPacket(new PacketBuffer(buf), DefaultProps.NET_CHANNEL_NAME + "-CORE");
	}
	
	public static String getNameForItem(Item item) {
		Object obj = Item.itemRegistry.getNameForObject(item);
		if (obj == null) {
			return null;
		}
		return obj.toString();
	}
	
	public static String getNameForBlock(Block item) {
		Object obj = Block.blockRegistry.getNameForObject(item);
		if (obj == null) {
			return null;
		}
		return obj.toString();
	}
}
