/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import org.apache.logging.log4j.Level;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
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
import buildcraft.api.core.Position;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipePluggable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.DefaultProps;
import buildcraft.core.IDropControlInventory;
import buildcraft.core.ITileBufferHolder;
import buildcraft.core.TileBuffer;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.IClientState;
import buildcraft.core.network.IGuiReturnHandler;
import buildcraft.core.network.ISyncedTile;
import buildcraft.core.network.PacketTileState;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.utils.ColorUtils;
import buildcraft.core.utils.Utils;
import buildcraft.transport.ItemFacade.FacadeState;
import buildcraft.transport.gates.GateFactory;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.utils.RobotStationState;

public class TileGenericPipe extends TileEntity implements IPowerReceptor, IFluidHandler,
		IPipeTile, ITileBufferHolder, IEnergyHandler, IDropControlInventory,
		ISyncedTile, ISolidSideTile, IGuiReturnHandler {

	public boolean initialized = false;
	public final PipeRenderState renderState = new PipeRenderState();
	public final CoreState coreState = new CoreState();
	public boolean[] pipeConnectionsBuffer = new boolean[6];

	public Pipe pipe;
	public int redstoneInput;
	public int[] redstoneInputSide = new int[ForgeDirection.VALID_DIRECTIONS.length];
	public int glassColor = -1;
	
	protected boolean deletePipe = false;
	protected boolean sendClientUpdate = false;
	protected boolean blockNeighborChange = false;
	protected boolean refreshRenderState = false;
	protected boolean pipeBound = false;
	protected boolean resyncGateExpansions = false;
	protected boolean attachPluggables = false;

	private TileBuffer[] tileBuffer;
	
	public static class CoreState implements IClientState {
		public int pipeId = -1;
		public ItemGate.GatePluggable[] gates = new ItemGate.GatePluggable[ForgeDirection.VALID_DIRECTIONS.length];

		@Override
		public void writeData(ByteBuf data) {
			data.writeInt(pipeId);
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				ItemGate.GatePluggable gate = gates[i];

				boolean gateValid = gate != null;
				data.writeBoolean(gateValid);
				if (gateValid) {
					gate.writeToByteByf(data);
				}
			}
		}

		@Override
		public void readData(ByteBuf data) {
			pipeId = data.readInt();
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				if (data.readBoolean()) {
					ItemGate.GatePluggable gate = gates[i];
					if (gate == null) {
						gates[i] = gate = new ItemGate.GatePluggable();
					}
					gate.readFromByteBuf(data);
				} else {
					gates[i] = null;
				}
			}
		}
	}

	public static class SideProperties {
		IPipePluggable[] pluggables = new IPipePluggable[ForgeDirection.VALID_DIRECTIONS.length];

		public void writeToNBT(NBTTagCompound nbt) {
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				IPipePluggable pluggable = pluggables[i];
				final String key = "pluggable[" + i + "]";
				if (pluggable == null) {
					nbt.removeTag(key);
				} else {
					NBTTagCompound pluggableData = new NBTTagCompound();
					pluggableData.setString("pluggableClass", pluggable.getClass().getName());
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
					Class<?> pluggableClass = Class.forName(pluggableData.getString("pluggableClass"));
					if (!IPipePluggable.class.isAssignableFrom(pluggableClass)) {
						BCLog.logger.warn("Wrong pluggable class: " + pluggableClass);
						continue;
					}
					IPipePluggable pluggable = (IPipePluggable) pluggableClass.newInstance();
					pluggable.readFromNBT(pluggableData);
					pluggables[i] = pluggable;
				} catch (Exception e) {
					BCLog.logger.warn("Failed to load side state");
					e.printStackTrace();
				}
			}

			// Migration code
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				IPipePluggable pluggable = null;
				if (nbt.hasKey("facadeState[" + i + "]")) {
					pluggable = new ItemFacade.FacadePluggable(FacadeState.readArray(nbt.getTagList("facadeState[" + i + "]", Constants.NBT.TAG_COMPOUND)));
				} else {
					// Migration support for 5.0.x and 6.0.x
					if (nbt.hasKey("facadeBlocks[" + i + "]")) {
						// 5.0.x
						Block block = (Block) Block.blockRegistry.getObjectById(nbt.getInteger("facadeBlocks[" + i + "]"));
						int blockId = nbt.getInteger("facadeBlocks[" + i + "]");

						if (blockId != 0) {
							int metadata = nbt.getInteger("facadeMeta[" + i + "]");
							pluggable = new ItemFacade.FacadePluggable(new FacadeState[]{FacadeState.create(block, metadata)});
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
							pluggable = new ItemFacade.FacadePluggable(new FacadeState[]{mainState, phasedState});
						} else {
							pluggable = new ItemFacade.FacadePluggable(new FacadeState[]{mainState});
						}
					}
				}

				if (nbt.getBoolean("plug[" + i + "]")) {
					pluggable = new ItemPlug.PlugPluggable();
				}
				if (nbt.getBoolean("robotStation[" + i + "]")) {
					pluggable = new ItemRobotStation.RobotStationPluggable();
				}

				if (pluggable != null) {
					pluggables[i] = pluggable;
				}
			}
		}

		public void rotateLeft() {
			IPipePluggable[] newPluggables = new IPipePluggable[ForgeDirection.VALID_DIRECTIONS.length];
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				newPluggables[dir.getRotation(ForgeDirection.UP).ordinal()] = pluggables[dir.ordinal()];
			}
			pluggables = newPluggables;
		}

		public boolean dropItem(TileGenericPipe pipe, ForgeDirection direction) {
			boolean result = false;
			IPipePluggable pluggable = pluggables[direction.ordinal()];
			if (pluggable != null) {
				pluggable.onDetachedPipe(pipe, direction);
				ItemStack[] stacks = pluggable.getDropItems(pipe);
				if (stacks != null) {
					for (ItemStack stack : stacks) {
						InvUtils.dropItems(pipe.worldObj, stack, pipe.xCoord, pipe.yCoord, pipe.zCoord);
					}
				}
				result = true;
			}
			pluggables[direction.ordinal()] = null;
			pipe.notifyBlockChanged();
			return result;
		}

		public void invalidate() {
			for (IPipePluggable p : pluggables) {
				if (p != null) {
					p.invalidate();
				}
			}
		}

		public void validate(TileGenericPipe pipe) {
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
				IPipePluggable p = pluggables[d.ordinal()];

				if (p != null) {
					p.validate(pipe, d);
				}
			}
		}
	}

	private SideProperties sideProperties = new SideProperties();

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
			BCLog.logger.log(Level.WARN, "Pipe failed to load from NBT at {0},{1},{2}", new Object[]{xCoord, yCoord, zCoord});
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
		BlockGenericPipe.updateNeighbourSignalState(pipe);
	}

	@Override
	public void updateEntity() {
		if (attachPluggables) {
			attachPluggables = false;
			// Attach callback
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				if (sideProperties.pluggables[i] != null) {
					sideProperties.pluggables[i].onAttachedPipe(this, ForgeDirection.getOrientation(i));
				}
			}
		}

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

		if (!BlockGenericPipe.isValid(pipe)) {
			return;
		}

		pipe.updateEntity();

		if (worldObj.isRemote) {
			if (resyncGateExpansions) {
				syncGateExpansions();
			}

			return;
		}

		if (blockNeighborChange) {
			computeConnections();
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
				BuildCraftPacket updatePacket = getBCDescriptionPacket();

				for (Object o : world.playerEntities) {
					EntityPlayerMP player = (EntityPlayerMP) o;

					if (world.getPlayerManager().isPlayerWatchingChunk (player, xCoord >> 4, zCoord >> 4)) {
						BuildCraftCore.instance.sendToPlayer(player, updatePacket);
					}
				}
			}
		}
	}

	public int getItemMetadata() {
		return 1 + (worldObj.isRemote ? renderState.glassColor : glassColor);
	}
	
	public int getStainedColorMultiplier() {
		int color;
		
		if (worldObj.isRemote) {
			color = renderState.glassColor;
		} else {
			color = this.glassColor;
		}
		
		return color >= 0 ? ColorUtils.getRGBColor(color) : -1;
	}
	
	/**
	 *  PRECONDITION: worldObj must not be null
	 */
	protected void refreshRenderState() {
		renderState.glassColor = (byte) glassColor;
		
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
				renderState.wireMatrix.setWireConnected(color, direction, pipe.isWireConnectedTo(this.getTile(direction), color));
			}

			boolean lit = pipe.signalStrength[color.ordinal()] > 0;

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

		// Gate Textures and movement
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			final Gate gate = pipe.gates[direction.ordinal()];
			renderState.gateMatrix.setIsGateExists(gate != null, direction);
			renderState.gateMatrix.setIsGateLit(gate != null && gate.isGateActive(), direction);
			renderState.gateMatrix.setIsGatePulsing(gate != null && gate.isGatePulsing(), direction);
		}

		// Facades
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			IPipePluggable pluggable = sideProperties.pluggables[direction.ordinal()];
			if (!(pluggable instanceof ItemFacade.FacadePluggable)) {
				renderState.facadeMatrix.setFacade(direction, null, 0, true);
				continue;
			}
			FacadeState[] states = ((ItemFacade.FacadePluggable) pluggable).states;
			if (states == null) {
				renderState.facadeMatrix.setFacade(direction, null, 0, true);
				continue;
			}
			// Iterate over all states and activate first proper
			FacadeState defaultState = null, activeState = null;
			for (FacadeState state : states) {
				if (state.wire == null) {
					defaultState = state;
					continue;
				}
				if (isWireActive(state.wire)) {
					activeState = state;
					break;
				}
			}
			if (activeState == null) {
				activeState = defaultState;
			}
			Block block = activeState != null ? activeState.block : null;
			int metadata = activeState != null ? activeState.metadata : 0;
			boolean transparent = activeState == null || block == null;
			renderState.facadeMatrix.setFacade(direction, block, metadata, transparent);
		}

		//Plugs
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			IPipePluggable pluggable = sideProperties.pluggables[direction.ordinal()];
			renderState.plugMatrix.setConnected(direction, pluggable instanceof ItemPlug.PlugPluggable);

			if (pluggable instanceof ItemRobotStation.RobotStationPluggable) {
				DockingStation station = ((ItemRobotStation.RobotStationPluggable) pluggable).getStation();

				if (station.isTaken()) {
					if (station.isMainStation()) {
						renderState.robotStationMatrix.setState(direction,
							RobotStationState.Linked);
					} else {
						renderState.robotStationMatrix.setState(direction,
								RobotStationState.Reserved);
					}
				} else {
					renderState.robotStationMatrix.setState(direction,
							RobotStationState.Available);
				}
			} else {
				renderState.robotStationMatrix.setState(direction, RobotStationState.None);
			}

		}

		if (renderState.isDirty()) {
			renderState.clean();
			sendUpdateToClient();
		}
	}

	public void initialize(Pipe<?> pipe) {
		this.blockType = getBlockType();

		if (pipe == null) {
			BCLog.logger.log(Level.WARN, "Pipe failed to initialize at {0},{1},{2}, deleting", new Object[]{xCoord, yCoord, zCoord});
			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			return;
		}

		this.pipe = pipe;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = getTile(o);

			if (tile instanceof ITileBufferHolder) {
				((ITileBufferHolder) tile).blockCreated(o, BuildCraftTransport.genericPipeBlock, this);
			}
			if (tile instanceof TileGenericPipe) {
				((TileGenericPipe) tile).scheduleNeighborChange();
			}
		}

		bindPipe();

		computeConnections();
		scheduleNeighborChange();
		scheduleRenderUpdate();

		if (pipe.needsInit()) {
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
	}

	/* IPIPEENTRY */
	@Override
	public int injectItem(ItemStack payload, boolean doAdd, ForgeDirection from, EnumColor color) {
		if (!pipe.inputOpen(from)) {
			return 0;
		}

		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof PipeTransportItems && isPipeConnected(from)) {
			if (doAdd) {
				Position itemPos = new Position(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, from.getOpposite());
				itemPos.moveBackwards(0.4);

				TravelingItem pipedItem = TravelingItem.make(itemPos.x, itemPos.y, itemPos.z, payload);
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

	/* SMP */

	public BuildCraftPacket getBCDescriptionPacket() {
		bindPipe();
		updateCoreState();

		PacketTileState packet = new PacketTileState(this.xCoord, this.yCoord, this.zCoord);

		if (pipe != null && pipe.transport != null) {
			pipe.transport.sendDescriptionPacket();
		}

		packet.addStateForSerialization((byte) 0, coreState);
		packet.addStateForSerialization((byte) 1, renderState);

		if (pipe instanceof IClientState) {
			packet.addStateForSerialization((byte) 2, (IClientState) pipe);
		}

		return packet;
	}

	@Override
	public Packet getDescriptionPacket() {
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
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			return cache[to.ordinal()].getTile();
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

		if (with instanceof TileGenericPipe) {
			TileGenericPipe other = (TileGenericPipe) with;
			
			if (other.hasBlockingPluggable(side.getOpposite())) {
				return false;
			}
			
			if (other.glassColor >= 0 && glassColor >= 0 && other.glassColor != glassColor) {
				return false;
			}
			
			Pipe<?> otherPipe = ((TileGenericPipe) with).pipe;

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

	protected boolean hasBlockingPluggable(ForgeDirection side) {
		IPipePluggable pluggable = sideProperties.pluggables[side.ordinal()];
		return pluggable != null && pluggable.blocking(this, side);
	}

	private void computeConnections() {
		TileBuffer[] cache = getTileCache();
		if (cache == null) {
			return;
		}

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			TileBuffer t = cache[side.ordinal()];
			t.refresh();

			pipeConnectionsBuffer[side.ordinal()] = canPipeConnect(t.getTile(), side);
		}
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with) {
		if (worldObj.isRemote) {
			return renderState.pipeConnectionMatrix.isConnected(with);
		}
		return pipeConnectionsBuffer[with.ordinal()];
	}

	@Override
	public boolean isWireActive(PipeWire wire) {
		if (pipe == null) {
			return false;
		}
		return pipe.signalStrength[wire.ordinal()] > 0;
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
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !hasPlug(from) && !hasRobotStation(from)) {
			return ((IFluidHandler) pipe.transport).fill(from, resource, doFill);
		} else {
			return 0;
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !hasPlug(from) && !hasRobotStation(from)) {
			return ((IFluidHandler) pipe.transport).drain(from, maxDrain, doDrain);
		} else {
			return null;
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !hasPlug(from) && !hasRobotStation(from)) {
			return ((IFluidHandler) pipe.transport).drain(from, resource, doDrain);
		} else {
			return null;
		}
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !hasPlug(from) && !hasRobotStation(from)) {
			return ((IFluidHandler) pipe.transport).canFill(from, fluid);
		} else {
			return false;
		}
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !hasPlug(from) && !hasRobotStation(from)) {
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

	public boolean addFacade(ForgeDirection direction, FacadeState[] states) {
		return setPluggable(direction, new ItemFacade.FacadePluggable(states));
	}

	public boolean addPlug(ForgeDirection direction) {
		return setPluggable(direction, new ItemPlug.PlugPluggable());
	}

	public boolean addRobotStation(ForgeDirection direction) {
		return setPluggable(direction, new ItemRobotStation.RobotStationPluggable());
	}

	public boolean addGate(ForgeDirection direction, Gate gate) {
		gate.setDirection(direction);
		pipe.gates[direction.ordinal()] = gate;
		return setPluggable(direction, new ItemGate.GatePluggable(gate));
	}

	public boolean hasFacade(ForgeDirection direction) {
		if (direction == null || direction == ForgeDirection.UNKNOWN) {
			return false;
		} else if (this.getWorldObj().isRemote) {
			return renderState.facadeMatrix.getFacadeBlock(direction) != null;
		} else {
			return sideProperties.pluggables[direction.ordinal()] instanceof ItemFacade.FacadePluggable;
		}
	}

	public boolean hasGate(ForgeDirection direction) {
		if (direction == null || direction == ForgeDirection.UNKNOWN) {
			return false;
		} else if (this.getWorldObj().isRemote) {
			return renderState.gateMatrix.isGateExists(direction);
		} else {
			return sideProperties.pluggables[direction.ordinal()] instanceof ItemGate.GatePluggable;
		}
	}

	public boolean setPluggable(ForgeDirection direction, IPipePluggable pluggable) {
		if (worldObj != null && worldObj.isRemote || pluggable == null) {
			return false;
		}
		sideProperties.dropItem(this, direction);
		sideProperties.pluggables[direction.ordinal()] = pluggable;
		pluggable.onAttachedPipe(this, direction);
		notifyBlockChanged();
		return true;
	}

	private void updateCoreState() {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			IPipePluggable pluggable = sideProperties.pluggables[i];
			coreState.gates[i] = pluggable instanceof ItemGate.GatePluggable ? (ItemGate.GatePluggable) pluggable : null;
		}
	}

	public boolean hasEnabledFacade(ForgeDirection direction) {
		return hasFacade(direction) && !renderState.facadeMatrix.getFacadeTransparent(direction);
	}

	public ItemStack getFacade(ForgeDirection direction) {
		IPipePluggable pluggable = sideProperties.pluggables[direction.ordinal()];
		return pluggable instanceof ItemFacade.FacadePluggable ?
				ItemFacade.getFacade(((ItemFacade.FacadePluggable) pluggable).states) : null;
	}

	public Gate getGate(ForgeDirection direction) {
		return pipe.gates[direction.ordinal()];
	}

	public DockingStation getStation(ForgeDirection direction) {
		IPipePluggable pluggable = sideProperties.pluggables[direction.ordinal()];
		return pluggable instanceof ItemRobotStation.RobotStationPluggable ?
				((ItemRobotStation.RobotStationPluggable) pluggable).getStation() : null;
	}

	// Legacy
	public void setGate(Gate gate, int direction) {
		if (sideProperties.pluggables[direction] == null) {
			gate.setDirection(ForgeDirection.getOrientation(direction));
			pipe.gates[direction] = gate;
			sideProperties.pluggables[direction] = new ItemGate.GatePluggable(gate);
		}
	}

	public boolean dropSideItems(ForgeDirection direction) {
		return sideProperties.dropItem(this, direction);
	}

	@SideOnly(Side.CLIENT)
	public IIconProvider getPipeIcons() {
		if (pipe == null) {
			return null;
		}
		return pipe.getIconProvider();
	}

	@Override
	public IClientState getStateInstance(byte stateId) {
		switch (stateId) {
			case 0:
				return coreState;
			case 1:
				return renderState;
			case 2:
				return (IClientState) pipe;
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
				if (pipe == null && coreState.pipeId != 0) {
					initialize(BlockGenericPipe.createPipe((Item) Item.itemRegistry.getObjectById(coreState.pipeId)));
				}

				if (pipe == null) {
					break;
				}

				for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
					final ItemGate.GatePluggable gatePluggable = coreState.gates[i];
					if (gatePluggable == null) {
						pipe.gates[i] = null;
						continue;
					}
					Gate gate = pipe.gates[i];
					if (gate == null || gate.logic != gatePluggable.logic || gate.material != gatePluggable.material) {
						pipe.gates[i] = GateFactory.makeGate(pipe, gatePluggable.material, gatePluggable.logic, ForgeDirection.getOrientation(i));
					}
				}

				syncGateExpansions();

				worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
				break;

			case 1: {
				if (renderState.needsRenderUpdate()) {
					worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
					renderState.clean();
				}
				break;
			}
		}
	}

	private void syncGateExpansions() {
		resyncGateExpansions = false;
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			Gate gate = pipe.gates[i];
			ItemGate.GatePluggable gatePluggable = coreState.gates[i];
			if (gate != null && gatePluggable.expansions.length > 0) {
				for (IGateExpansion expansion : gatePluggable.expansions) {
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
		if (hasFacade(side)) {
			return true;
		}

		if (BlockGenericPipe.isValid(pipe) && pipe instanceof ISolidSideTile) {
			if (((ISolidSideTile) pipe).isSolidOnSide(side)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasPlug(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN) {
			return false;
		}

		if (this.worldObj.isRemote) {
			return renderState.plugMatrix.isConnected(side);
		}

		return sideProperties.pluggables[side.ordinal()] instanceof ItemPlug.PlugPluggable;
	}

	public boolean hasRobotStation(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN) {
			return false;
		}

		if (this.worldObj.isRemote) {
			return renderState.robotStationMatrix.isConnected(side);
		}

		return sideProperties.pluggables[side.ordinal()] instanceof ItemRobotStation.RobotStationPluggable;
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

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor) {
			return ((IPowerReceptor) pipe).getPowerReceiver(null);
		} else {
			return null;
		}
	}

	@Override
	public void doWork(PowerHandler workProvider) {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).doWork(workProvider);
		}
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		if (pipe instanceof IEnergyHandler) {
			return ((IEnergyHandler) pipe).canConnectEnergy(from);
		} else {
			return false;
		}
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
			boolean simulate) {
		if (pipe instanceof IEnergyHandler) {
			return ((IEnergyHandler) pipe).receiveEnergy(from, maxReceive, simulate);
		} else {
			return 0;
		}
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
			boolean simulate) {
		if (pipe instanceof IEnergyHandler) {
			return ((IEnergyHandler) pipe).extractEnergy(from, maxExtract, simulate);
		} else {
			return 0;
		}
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		if (pipe instanceof IEnergyHandler) {
			return ((IEnergyHandler) pipe).getEnergyStored(from);
		} else {
			return 0;
		}
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		if (pipe instanceof IEnergyHandler) {
			return ((IEnergyHandler) pipe).getMaxEnergyStored(from);
		} else {
			return 0;
		}
	}

	@Override
	public TileEntity getAdjacentTile(ForgeDirection dir) {
		return getTile(dir);
	}

	@Override
	public IPipe getPipe() {
		return pipe;
	}
}
