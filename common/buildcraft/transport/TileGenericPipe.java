/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.List;

import org.apache.logging.log4j.Level;
import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import cofh.api.energy.IEnergyHandler;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.ISerializable;
import buildcraft.api.core.Position;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.power.IRedstoneEngineReceiver;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeManager;
import buildcraft.api.transport.PipeWire;
import buildcraft.api.transport.pluggable.IFacadePluggable;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.DefaultProps;
import buildcraft.core.internal.IDropControlInventory;
import buildcraft.core.lib.ITileBufferHolder;
import buildcraft.core.lib.TileBuffer;
import buildcraft.core.lib.network.IGuiReturnHandler;
import buildcraft.core.lib.network.ISyncedTile;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.PacketTileState;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.ItemFacade.FacadeState;
import buildcraft.transport.gates.GateFactory;
import buildcraft.transport.gates.GatePluggable;
import buildcraft.transport.pluggable.PlugPluggable;

public class TileGenericPipe extends TileEntity implements IFluidHandler,
		IPipeTile, ITileBufferHolder, IEnergyHandler, IDropControlInventory,
		ISyncedTile, ISolidSideTile, IGuiReturnHandler, IRedstoneEngineReceiver,
		IDebuggable, IPipeConnection {

	public boolean initialized = false;
	public final PipeRenderState renderState = new PipeRenderState();
	public final PipePluggableState pluggableState = new PipePluggableState();
	public final CoreState coreState = new CoreState();
	public boolean[] pipeConnectionsBuffer = new boolean[6];

	public Pipe pipe;
	public int redstoneInput;
	public int[] redstoneInputSide = new int[ForgeDirection.VALID_DIRECTIONS.length];

	protected boolean deletePipe = false;
	protected boolean sendClientUpdate = false;
	protected boolean blockNeighborChange = false;
	protected int blockNeighborChangedSides = 0;
	protected boolean refreshRenderState = false;
	protected boolean pipeBound = false;
	protected boolean resyncGateExpansions = false;
	protected boolean attachPluggables = false;
	protected SideProperties sideProperties = new SideProperties();

	private TileBuffer[] tileBuffer;
	private int glassColor = -1;

	public static class CoreState implements ISerializable {
		public int pipeId = -1;

		@Override
		public void writeData(ByteBuf data) {
			data.writeInt(pipeId);
		}

		@Override
		public void readData(ByteBuf data) {
			pipeId = data.readInt();
		}
	}

	public static class SideProperties {
		PipePluggable[] pluggables = new PipePluggable[ForgeDirection.VALID_DIRECTIONS.length];

		public void writeToNBT(NBTTagCompound nbt) {
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				PipePluggable pluggable = pluggables[i];
				final String key = "pluggable[" + i + "]";
				if (pluggable == null) {
					nbt.removeTag(key);
				} else {
					NBTTagCompound pluggableData = new NBTTagCompound();
					pluggableData.setString("pluggableName", PipeManager.getPluggableName(pluggable.getClass()));
					pluggable.writeToNBT(pluggableData);
					nbt.setTag(key, pluggableData);
				}
			}
		}

		public void readFromNBT(NBTTagCompound nbt) {
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				final String key = "pluggable[" + i + "]";
				if (!nbt.hasKey(key)) {
					continue;
				}
				try {
					NBTTagCompound pluggableData = nbt.getCompoundTag(key);
					Class<?> pluggableClass = null;
					// Migration support for 6.1.x/6.2.x
					if (pluggableData.hasKey("pluggableClass")) {
						String c = pluggableData.getString("pluggableClass");
						if ("buildcraft.transport.gates.ItemGate$GatePluggable".equals(c)) {
							pluggableClass = GatePluggable.class;
						} else if ("buildcraft.transport.ItemFacade$FacadePluggable".equals(c)) {
							pluggableClass = FacadePluggable.class;
						} else if ("buildcraft.transport.ItemPlug$PlugPluggable".equals(c)) {
							pluggableClass = PlugPluggable.class;
						} else if ("buildcraft.transport.gates.ItemRobotStation$RobotStationPluggable".equals(c)
								|| "buildcraft.transport.ItemRobotStation$RobotStationPluggable".equals(c)) {
							pluggableClass = PipeManager.getPluggableByName("robotStation");
						}
					} else {
						pluggableClass = PipeManager.getPluggableByName(pluggableData.getString("pluggableName"));
					}
					if (pluggableClass != null) {
						if (!PipePluggable.class.isAssignableFrom(pluggableClass)) {
							BCLog.logger.warn("Wrong pluggable class: " + pluggableClass);
							continue;
						}
						PipePluggable pluggable = (PipePluggable) pluggableClass.newInstance();
						pluggable.readFromNBT(pluggableData);
						pluggables[i] = pluggable;
					}
				} catch (Exception e) {
					BCLog.logger.warn("Failed to load side state");
					e.printStackTrace();
				}
			}

			// Migration code
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				PipePluggable pluggable = null;
				if (nbt.hasKey("facadeState[" + i + "]")) {
					pluggable = new FacadePluggable(FacadeState.readArray(nbt.getTagList("facadeState[" + i + "]", Constants.NBT.TAG_COMPOUND)));
				} else {
					// Migration support for 5.0.x and 6.0.x
					if (nbt.hasKey("facadeBlocks[" + i + "]")) {
						// 5.0.x
						Block block = (Block) Block.blockRegistry.getObjectById(nbt.getInteger("facadeBlocks[" + i + "]"));
						int blockId = nbt.getInteger("facadeBlocks[" + i + "]");

						if (blockId != 0) {
							int metadata = nbt.getInteger("facadeMeta[" + i + "]");
							pluggable = new FacadePluggable(new FacadeState[]{FacadeState.create(block, metadata)});
						}
					} else if (nbt.hasKey("facadeBlocksStr[" + i + "][0]")) {
						// 6.0.x
						FacadeState mainState = FacadeState.create(
								(Block) Block.blockRegistry.getObject(nbt.getString("facadeBlocksStr[" + i + "][0]")),
								nbt.getInteger("facadeMeta[" + i + "][0]")
						);
						if (nbt.hasKey("facadeBlocksStr[" + i + "][1]")) {
							FacadeState phasedState = FacadeState.create(
									(Block) Block.blockRegistry.getObject(nbt.getString("facadeBlocksStr[" + i + "][1]")),
									nbt.getInteger("facadeMeta[" + i + "][1]"),
									PipeWire.fromOrdinal(nbt.getInteger("facadeWires[" + i + "]"))
							);
							pluggable = new FacadePluggable(new FacadeState[]{mainState, phasedState});
						} else {
							pluggable = new FacadePluggable(new FacadeState[]{mainState});
						}
					}
				}

				if (nbt.getBoolean("plug[" + i + "]")) {
					pluggable = new PlugPluggable();
				}

				if (pluggable != null) {
					pluggables[i] = pluggable;
				}
			}
		}

		public void rotateLeft() {
			PipePluggable[] newPluggables = new PipePluggable[ForgeDirection.VALID_DIRECTIONS.length];
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				newPluggables[dir.getRotation(ForgeDirection.UP).ordinal()] = pluggables[dir.ordinal()];
			}
			pluggables = newPluggables;
		}

		public boolean dropItem(TileGenericPipe pipe, ForgeDirection direction, EntityPlayer player) {
			boolean result = false;
			PipePluggable pluggable = pluggables[direction.ordinal()];
			if (pluggable != null) {
				pluggable.onDetachedPipe(pipe, direction);
				if (!pipe.getWorld().isRemote) {
					ItemStack[] stacks = pluggable.getDropItems(pipe);
					if (stacks != null) {
						for (ItemStack stack : stacks) {
							Utils.dropTryIntoPlayerInventory(pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord,
									stack, player);
						}
					}
				}
				result = true;
			}
			return result;
		}

		public void invalidate() {
			for (PipePluggable p : pluggables) {
				if (p != null) {
					p.invalidate();
				}
			}
		}

		public void validate(TileGenericPipe pipe) {
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
				PipePluggable p = pluggables[d.ordinal()];

				if (p != null) {
					p.validate(pipe, d);
				}
			}
		}
	}

	public TileGenericPipe() {
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if (glassColor >= 0) {
			nbt.setByte("stainedColor", (byte) glassColor);
		}
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final String key = "redstoneInputSide[" + i + "]";
			nbt.setByte(key, (byte) redstoneInputSide[i]);
		}

		if (pipe != null) {
			nbt.setInteger("pipeId", Item.getIdFromItem(pipe.item));
			pipe.writeToNBT(nbt);
		} else {
			nbt.setInteger("pipeId", coreState.pipeId);
		}

		sideProperties.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		glassColor = nbt.hasKey("stainedColor") ? nbt.getByte("stainedColor") : -1;

		redstoneInput = 0;

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final String key = "redstoneInputSide[" + i + "]";
			if (nbt.hasKey(key)) {
				redstoneInputSide[i] = nbt.getByte(key);

				if (redstoneInputSide[i] > redstoneInput) {
					redstoneInput = redstoneInputSide[i];
				}
			} else {
				redstoneInputSide[i] = 0;
			}
		}

		coreState.pipeId = nbt.getInteger("pipeId");
		pipe = BlockGenericPipe.createPipe((Item) Item.getItemById(coreState.pipeId));
		bindPipe();

		if (pipe != null) {
			pipe.readFromNBT(nbt);
		} else {
			BCLog.logger.log(Level.WARN, "Pipe failed to load from NBT at {0},{1},{2}", xCoord, yCoord, zCoord);
			deletePipe = true;
		}

		sideProperties.readFromNBT(nbt);
		attachPluggables = true;
	}

	@Override
	public void invalidate() {
		initialized = false;
		tileBuffer = null;

		if (pipe != null) {
			pipe.invalidate();
		}

		sideProperties.invalidate();

		super.invalidate();
	}

	@Override
	public void validate() {
		super.validate();
		initialized = false;
		tileBuffer = null;
		bindPipe();

		if (pipe != null) {
			pipe.validate();
		}

		sideProperties.validate(this);
	}

	protected void notifyBlockChanged() {
		worldObj.notifyBlockOfNeighborChange(xCoord, yCoord, zCoord, getBlock());
		scheduleRenderUpdate();
		sendUpdateToClient();
		if (pipe != null) {
			pipe.scheduleWireUpdate();
		}
	}

	@Override
	public void updateEntity() {
		if (!worldObj.isRemote) {
			if (deletePipe) {
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			}

			if (pipe == null) {
				return;
			}

			if (!initialized) {
				initialize(pipe);
			}
		}

		if (attachPluggables) {
			attachPluggables = false;
			// Attach callback
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				if (sideProperties.pluggables[i] != null) {
					pipe.eventBus.registerHandler(sideProperties.pluggables[i]);
					sideProperties.pluggables[i].onAttachedPipe(this, ForgeDirection.getOrientation(i));
				}
			}
			notifyBlockChanged();
		}

		if (!BlockGenericPipe.isValid(pipe)) {
			return;
		}

		pipe.updateEntity();

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			PipePluggable p = getPipePluggable(direction);
			if (p != null) {
				p.update(this, direction);
			}
		}

		if (worldObj.isRemote) {
			if (resyncGateExpansions) {
				syncGateExpansions();
			}

			return;
		}

		if (blockNeighborChange) {
			for (int i = 0; i < 6; i++) {
				if ((blockNeighborChangedSides & (1 << i)) != 0) {
					blockNeighborChangedSides ^= 1 << i;
					computeConnection(ForgeDirection.getOrientation(i));
				}
			}
			pipe.onNeighborBlockChange(0);
			blockNeighborChange = false;
			refreshRenderState = true;
		}

		if (refreshRenderState) {
			refreshRenderState();
			refreshRenderState = false;
		}

		if (sendClientUpdate) {
			sendClientUpdate = false;

			if (worldObj instanceof WorldServer) {
				WorldServer world = (WorldServer) worldObj;
				Packet updatePacket = getBCDescriptionPacket();

				for (Object o : world.playerEntities) {
					EntityPlayerMP player = (EntityPlayerMP) o;

					if (world.getPlayerManager().isPlayerWatchingChunk(player, xCoord >> 4, zCoord >> 4)) {
						BuildCraftCore.instance.sendToPlayer(player, updatePacket);
					}
				}
			}
		}
	}

	public void initializeFromItemMetadata(int i) {
		if (i >= 1 && i <= 16) {
			setPipeColor((i - 1) & 15);
		} else {
			setPipeColor(-1);
		}
	}

	public int getItemMetadata() {
		return getPipeColor() >= 0 ? (1 + getPipeColor()) : 0;
	}

	public int getPipeColor() {
		return worldObj.isRemote ? renderState.getGlassColor() : this.glassColor;
	}

	public boolean setPipeColor(int color) {
		if (!worldObj.isRemote && color >= -1 && color < 16 && glassColor != color) {
			glassColor = color;
			notifyBlockChanged();
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, blockType);
			return true;
		}
		return false;
	}

	/**
	 *  PRECONDITION: worldObj must not be null
	 */
	protected void refreshRenderState() {
		renderState.setGlassColor((byte) glassColor);

		// Pipe connections;
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			renderState.pipeConnectionMatrix.setConnected(o, this.pipeConnectionsBuffer[o.ordinal()]);
		}

		// Pipe Textures
		for (int i = 0; i < 7; i++) {
			ForgeDirection o = ForgeDirection.getOrientation(i);
			renderState.textureMatrix.setIconIndex(o, pipe.getIconIndex(o));
		}

		// WireState
		for (PipeWire color : PipeWire.values()) {
			renderState.wireMatrix.setWire(color, pipe.wireSet[color.ordinal()]);

			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				renderState.wireMatrix.setWireConnected(color, direction, pipe.isWireConnectedTo(this.getTile(direction), color, direction));
			}

			boolean lit = pipe.wireSignalStrength[color.ordinal()] > 0;

			switch (color) {
				case RED:
					renderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Red_Lit : WireIconProvider.Texture_Red_Dark);
					break;
				case BLUE:
					renderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Blue_Lit : WireIconProvider.Texture_Blue_Dark);
					break;
				case GREEN:
					renderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Green_Lit : WireIconProvider.Texture_Green_Dark);
					break;
				case YELLOW:
					renderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Yellow_Lit : WireIconProvider.Texture_Yellow_Dark);
					break;
				default:
					break;

			}
		}

		/* TODO: Rewrite the requiresRenderUpdate API to run on the
		   server side instead of the client side to save network bandwidth */
		pluggableState.setPluggables(sideProperties.pluggables);

		if (renderState.isDirty()) {
			renderState.clean();
		}
		sendUpdateToClient();
	}

	public void initialize(Pipe<?> pipe) {
		initialized = false;

		this.blockType = getBlockType();

		if (pipe == null) {
			BCLog.logger.log(Level.WARN, "Pipe failed to initialize at {0},{1},{2}, deleting", xCoord, yCoord, zCoord);
			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			return;
		}

		this.pipe = pipe;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = getTile(o);

			if (tile instanceof ITileBufferHolder) {
				((ITileBufferHolder) tile).blockCreated(o, BuildCraftTransport.genericPipeBlock, this);
			}
			if (tile instanceof IPipeTile) {
				((IPipeTile) tile).scheduleNeighborChange();
			}
		}

		bindPipe();

		computeConnections();
		scheduleNeighborChange();
		scheduleRenderUpdate();

		if (!pipe.isInitialized()) {
			pipe.initialize();
		}

		initialized = true;
	}

	private void bindPipe() {
		if (!pipeBound && pipe != null) {
			pipe.setTile(this);
			coreState.pipeId = Item.getIdFromItem(pipe.item);
			pipeBound = true;
		}
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void scheduleNeighborChange() {
		blockNeighborChange = true;
		blockNeighborChangedSides = 0x3F;
	}

	public void scheduleNeighborChange(ForgeDirection direction) {
		blockNeighborChange = true;
		blockNeighborChangedSides |= direction == ForgeDirection.UNKNOWN ? 0x3F : (1 << direction.ordinal());
	}

	@Override
	public boolean canInjectItems(ForgeDirection from) {
		if (getPipeType() != IPipeTile.PipeType.ITEM) {
			return false;
		}
		return isPipeConnected(from);
	}

	@Override
	public int injectItem(ItemStack payload, boolean doAdd, ForgeDirection from, EnumColor color) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof PipeTransportItems && isPipeConnected(from)
				&& pipe.inputOpen(from)) {

			if (doAdd) {
				Position itemPos = new Position(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, from.getOpposite());
				itemPos.moveBackwards(0.4);

				TravelingItem pipedItem = TravelingItem.make(itemPos.x, itemPos.y, itemPos.z, payload);
				if (pipedItem.isCorrupted()) {
					return 0;
				}

				pipedItem.color = color;
				((PipeTransportItems) pipe.transport).injectItem(pipedItem, itemPos.orientation);
			}
			return payload.stackSize;
		}

		return 0;
	}

	@Override
	public int injectItem(ItemStack payload, boolean doAdd, ForgeDirection from) {
		return injectItem(payload, doAdd, from, null);
	}

	@Override
	public PipeType getPipeType() {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.transport.getPipeType();
		}
		return null;
	}

	@Override
	public int x() {
		return xCoord;
	}

	@Override
	public int y() {
		return yCoord;
	}

	@Override
	public int z() {
		return zCoord;
	}

	/* SMP */

	public Packet getBCDescriptionPacket() {
		bindPipe();
		updateCoreState();

		PacketTileState packet = new PacketTileState(this.xCoord, this.yCoord, this.zCoord);

		if (pipe != null && pipe.transport != null) {
			pipe.transport.sendDescriptionPacket();
		}

		packet.addStateForSerialization((byte) 0, coreState);
		packet.addStateForSerialization((byte) 1, renderState);
		packet.addStateForSerialization((byte) 2, pluggableState);

		if (pipe instanceof ISerializable) {
			packet.addStateForSerialization((byte) 3, (ISerializable) pipe);
		}

		return packet;
	}

	@Override
	public net.minecraft.network.Packet getDescriptionPacket() {
		return Utils.toPacket(getBCDescriptionPacket(), 1);
	}

	public void sendUpdateToClient() {
		sendClientUpdate = true;
	}

	@Override
	public void blockRemoved(ForgeDirection from) {

	}

	public TileBuffer[] getTileCache() {
		if (tileBuffer == null && pipe != null) {
			tileBuffer = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, pipe.transport.delveIntoUnloadedChunks());
		}
		return tileBuffer;
	}

	@Override
	public void blockCreated(ForgeDirection from, Block block, TileEntity tile) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			cache[from.getOpposite().ordinal()].set(block, tile);
		}
	}

	@Override
	public Block getBlock(ForgeDirection to) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			return cache[to.ordinal()].getBlock();
		} else {
			return null;
		}
	}

	@Override
	public TileEntity getTile(ForgeDirection to) {
		return getTile(to, false);
	}

	public TileEntity getTile(ForgeDirection to, boolean forceUpdate) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			return cache[to.ordinal()].getTile(forceUpdate);
		} else {
			return null;
		}
	}

	protected boolean canPipeConnect_internal(TileEntity with, ForgeDirection side) {
		if (!(pipe instanceof IPipeConnectionForced) || !((IPipeConnectionForced) pipe).ignoreConnectionOverrides(side)) {
			if (with instanceof IPipeConnection) {
				IPipeConnection.ConnectOverride override = ((IPipeConnection) with).overridePipeConnection(pipe.transport.getPipeType(), side.getOpposite());
				if (override != IPipeConnection.ConnectOverride.DEFAULT) {
					return override == IPipeConnection.ConnectOverride.CONNECT;
				}
			}
		}

		if (with instanceof IPipeTile) {
			IPipeTile other = (IPipeTile) with;

			if (other.hasBlockingPluggable(side.getOpposite())) {
				return false;
			}

			if (other.getPipeColor() >= 0 && glassColor >= 0 && other.getPipeColor() != glassColor) {
				return false;
			}

			Pipe<?> otherPipe = (Pipe<?>) other.getPipe();

			if (!BlockGenericPipe.isValid(otherPipe)) {
				return false;
			}

			if (!otherPipe.canPipeConnect(this, side.getOpposite())) {
				return false;
			}
		}

		return pipe.canPipeConnect(with, side);
	}

	/**
	 * Checks if this tile can connect to another tile
	 *
	 * @param with - The other Tile
	 * @param side - The orientation to get to the other tile ('with')
	 * @return true if pipes are considered connected
	 */
	protected boolean canPipeConnect(TileEntity with, ForgeDirection side) {
		if (with == null) {
			return false;
		}

		if (hasBlockingPluggable(side)) {
			return false;
		}

		if (!BlockGenericPipe.isValid(pipe)) {
			return false;
		}

		return canPipeConnect_internal(with, side);
	}

	public boolean hasBlockingPluggable(ForgeDirection side) {
		PipePluggable pluggable = getPipePluggable(side);
		if (pluggable == null) {
			return false;
		}

		if (pluggable instanceof IPipeConnection) {
			IPipe neighborPipe = getNeighborPipe(side);
			if (neighborPipe != null) {
				IPipeConnection.ConnectOverride override = ((IPipeConnection) pluggable).overridePipeConnection(neighborPipe.getTile().getPipeType(), side);
				if (override == IPipeConnection.ConnectOverride.CONNECT) {
					return true;
				} else if (override == IPipeConnection.ConnectOverride.DISCONNECT) {
					return false;
				}
			}
		}
		return pluggable.isBlocking(this, side);
	}

	protected void computeConnections() {
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			computeConnection(side);
		}
	}

	protected void computeConnection(ForgeDirection side) {
		TileBuffer[] cache = getTileCache();
		if (cache == null) {
			return;
		}

		TileBuffer t = cache[side.ordinal()];
		// For blocks which are not loaded, keep the old connection value.
		if (t.exists() || !initialized) {
			t.refresh();

			pipeConnectionsBuffer[side.ordinal()] = canPipeConnect(t.getTile(), side);
		}
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with) {
		if (worldObj.isRemote) {
			return renderState.pipeConnectionMatrix.isConnected(with);
		} else {
			return pipeConnectionsBuffer[with.ordinal()];
		}
	}

	@Override
	public boolean doDrop() {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.doDrop();
		} else {
			return false;
		}
	}

	@Override
	public void onChunkUnload() {
		if (pipe != null) {
			pipe.onChunkUnload();
		}
	}

	/**
	 * ITankContainer implementation *
	 */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !hasBlockingPluggable(from)) {
			return ((IFluidHandler) pipe.transport).fill(from, resource, doFill);
		} else {
			return 0;
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !hasBlockingPluggable(from)) {
			return ((IFluidHandler) pipe.transport).drain(from, maxDrain, doDrain);
		} else {
			return null;
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !hasBlockingPluggable(from)) {
			return ((IFluidHandler) pipe.transport).drain(from, resource, doDrain);
		} else {
			return null;
		}
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !hasBlockingPluggable(from)) {
			return ((IFluidHandler) pipe.transport).canFill(from, fluid);
		} else {
			return false;
		}
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !hasBlockingPluggable(from)) {
			return ((IFluidHandler) pipe.transport).canDrain(from, fluid);
		} else {
			return false;
		}
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return null;
	}

	public void scheduleRenderUpdate() {
		refreshRenderState = true;
	}

	public boolean hasFacade(ForgeDirection direction) {
		if (direction == null || direction == ForgeDirection.UNKNOWN) {
			return false;
		} else {
			return sideProperties.pluggables[direction.ordinal()] instanceof IFacadePluggable;
		}
	}

	public boolean hasGate(ForgeDirection direction) {
		if (direction == null || direction == ForgeDirection.UNKNOWN) {
			return false;
		} else {
			return sideProperties.pluggables[direction.ordinal()] instanceof GatePluggable;
		}
	}

	public boolean setPluggable(ForgeDirection direction, PipePluggable pluggable) {
		return setPluggable(direction, pluggable, null);
	}

	public boolean setPluggable(ForgeDirection direction, PipePluggable pluggable, EntityPlayer player) {
		if (worldObj != null && worldObj.isRemote) {
			return false;
		}

		if (direction == null || direction == ForgeDirection.UNKNOWN) {
			return false;
		}

		// Remove old pluggable
		if (sideProperties.pluggables[direction.ordinal()] != null) {
			sideProperties.dropItem(this, direction, player);
			pipe.eventBus.unregisterHandler(sideProperties.pluggables[direction.ordinal()]);
		}

		sideProperties.pluggables[direction.ordinal()] = pluggable;
		if (pluggable != null) {
			pipe.eventBus.registerHandler(pluggable);
			pluggable.onAttachedPipe(this, direction);
		}
		notifyBlockChanged();
		return true;
	}

	protected void updateCoreState() {
	}

	public boolean hasEnabledFacade(ForgeDirection direction) {
		return hasFacade(direction) && !((FacadePluggable) getPipePluggable(direction)).isTransparent();
	}

	// Legacy
	public void setGate(Gate gate, int direction) {
		if (sideProperties.pluggables[direction] == null) {
			gate.setDirection(ForgeDirection.getOrientation(direction));
			pipe.gates[direction] = gate;
			sideProperties.pluggables[direction] = new GatePluggable(gate);
		}
	}

	@SideOnly(Side.CLIENT)
	public IIconProvider getPipeIcons() {
		if (pipe == null) {
			return null;
		}
		return pipe.getIconProvider();
	}

	@Override
	public ISerializable getStateInstance(byte stateId) {
		switch (stateId) {
			case 0:
				return coreState;
			case 1:
				return renderState;
			case 2:
				return pluggableState;
			case 3:
				return (ISerializable) pipe;
		}
		throw new RuntimeException("Unknown state requested: " + stateId + " this is a bug!");
	}

	@Override
	public void afterStateUpdated(byte stateId) {
		if (!worldObj.isRemote) {
			return;
		}

		switch (stateId) {
			case 0:
				if (pipe != null) {
					break;
				}

				if (coreState.pipeId != 0) {
					initialize(BlockGenericPipe.createPipe((Item) Item.itemRegistry.getObjectById(coreState.pipeId)));
				}

				if (pipe == null) {
					break;
				}

				worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
				break;

			case 1: {
				if (renderState.needsRenderUpdate()) {
					worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
					renderState.clean();
				}
				break;
			}
			case 2: {
				PipePluggable[] newPluggables = pluggableState.getPluggables();

				// mark for render update if necessary
				for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
					PipePluggable old = sideProperties.pluggables[i];
					PipePluggable newer = newPluggables[i];
					if (old == null && newer == null) {
						continue;
					} else if (old != null && newer != null && old.getClass() == newer.getClass()) {
						if (newer.requiresRenderUpdate(old)) {
							worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
							break;
						}
					} else {
						// one of them is null but not the other, so update
						worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
						break;
					}
				}
				sideProperties.pluggables = newPluggables.clone();

				for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
					final PipePluggable pluggable = getPipePluggable(ForgeDirection.getOrientation(i));
					if (pluggable != null && pluggable instanceof GatePluggable) {
						final GatePluggable gatePluggable = (GatePluggable) pluggable;
						Gate gate = pipe.gates[i];
						if (gate == null || gate.logic != gatePluggable.getLogic() || gate.material != gatePluggable.getMaterial()) {
							pipe.gates[i] = GateFactory.makeGate(pipe, gatePluggable.getMaterial(), gatePluggable.getLogic(), ForgeDirection.getOrientation(i));
						}
					} else {
						pipe.gates[i] = null;
					}
				}

				syncGateExpansions();
				break;
			}
		}
	}

	private void syncGateExpansions() {
		resyncGateExpansions = false;
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			Gate gate = pipe.gates[i];
			if (gate == null) {
				continue;
			}
			GatePluggable gatePluggable = (GatePluggable) sideProperties.pluggables[i];
			if (gatePluggable.getExpansions().length > 0) {
				for (IGateExpansion expansion : gatePluggable.getExpansions()) {
					if (expansion != null) {
						if (!gate.expansions.containsKey(expansion)) {
							gate.addGateExpansion(expansion);
						}
					} else {
						resyncGateExpansions = true;
					}
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return DefaultProps.PIPE_CONTENTS_RENDER_DIST * DefaultProps.PIPE_CONTENTS_RENDER_DIST;
	}

	@Override
	public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
		return oldBlock != newBlock;
	}

	@Override
	public boolean isSolidOnSide(ForgeDirection side) {
		if (hasPipePluggable(side) && getPipePluggable(side).isSolidOnSide(this, side)) {
			return true;
		}

		if (BlockGenericPipe.isValid(pipe) && pipe instanceof ISolidSideTile) {
			if (((ISolidSideTile) pipe).isSolidOnSide(side)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public PipePluggable getPipePluggable(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN) {
			return null;
		}

		return sideProperties.pluggables[side.ordinal()];
	}

	@Override
	public boolean hasPipePluggable(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN) {
			return false;
		}

		return sideProperties.pluggables[side.ordinal()] != null;
	}

	public Block getBlock() {
		return getBlockType();
	}

	@Override
	public World getWorld() {
		return worldObj;
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void writeGuiData(ByteBuf data) {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IGuiReturnHandler) {
			((IGuiReturnHandler) pipe).writeGuiData(data);
		}
	}

	@Override
	public void readGuiData(ByteBuf data, EntityPlayer sender) {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IGuiReturnHandler) {
			((IGuiReturnHandler) pipe).readGuiData(data, sender);
		}
	}

	private IEnergyHandler internalGetEnergyHandler(ForgeDirection side) {
		if (hasPipePluggable(side)) {
			PipePluggable pluggable = getPipePluggable(side);
			if (pluggable instanceof IEnergyHandler) {
				return (IEnergyHandler) pluggable;
			} else if (pluggable.isBlocking(this, side)) {
				return null;
			}
		}
		if (pipe instanceof IEnergyHandler) {
			return (IEnergyHandler) pipe;
		}
		return null;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		IEnergyHandler handler = internalGetEnergyHandler(from);
		if (handler != null) {
			return handler.canConnectEnergy(from);
		} else {
			return false;
		}
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
							 boolean simulate) {
		IEnergyHandler handler = internalGetEnergyHandler(from);
		if (handler != null) {
			return handler.receiveEnergy(from, maxReceive, simulate);
		} else {
			return 0;
		}
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
							 boolean simulate) {
		IEnergyHandler handler = internalGetEnergyHandler(from);
		if (handler != null) {
			return handler.extractEnergy(from, maxExtract, simulate);
		} else {
			return 0;
		}
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		IEnergyHandler handler = internalGetEnergyHandler(from);
		if (handler != null) {
			return handler.getEnergyStored(from);
		} else {
			return 0;
		}
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		IEnergyHandler handler = internalGetEnergyHandler(from);
		if (handler != null) {
			return handler.getMaxEnergyStored(from);
		} else {
			return 0;
		}
	}

	@Override
	public Block getNeighborBlock(ForgeDirection dir) {
		return getBlock(dir);
	}

	@Override
	public TileEntity getNeighborTile(ForgeDirection dir) {
		return getTile(dir);
	}

	@Override
	public IPipe getNeighborPipe(ForgeDirection dir) {
		TileEntity neighborTile = getTile(dir);
		if (neighborTile instanceof IPipeTile) {
			return ((IPipeTile) neighborTile).getPipe();
		} else {
			return null;
		}
	}

	@Override
	public IPipe getPipe() {
		return pipe;
	}

	@Override
	public boolean canConnectRedstoneEngine(ForgeDirection side) {
		if (pipe instanceof IRedstoneEngineReceiver) {
			return ((IRedstoneEngineReceiver) pipe).canConnectRedstoneEngine(side);
		} else {
			return (getPipeType() != PipeType.POWER) && (getPipeType() != PipeType.STRUCTURE);
		}
	}

	@Override
	public void getDebugInfo(List<String> info, ForgeDirection side, ItemStack debugger, EntityPlayer player) {
		if (pipe instanceof IDebuggable) {
			((IDebuggable) pipe).getDebugInfo(info, side, debugger, player);
		}
		if (pipe.transport instanceof IDebuggable) {
			((IDebuggable) pipe.transport).getDebugInfo(info, side, debugger, player);
		}
		if (getPipePluggable(side) != null && getPipePluggable(side) instanceof IDebuggable) {
			((IDebuggable) getPipePluggable(side)).getDebugInfo(info, side, debugger, player);
		}
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with) {
		if (type == PipeType.POWER && hasPipePluggable(with) && getPipePluggable(with) instanceof IEnergyHandler) {
			return ConnectOverride.CONNECT;
		}
		return ConnectOverride.DEFAULT;
	}
}
