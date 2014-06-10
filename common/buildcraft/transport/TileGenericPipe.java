/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.transport.IPipeConnection;
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
import buildcraft.core.utils.Utils;
import buildcraft.robots.DockingStation;
import buildcraft.robots.DockingStationRegistry;
import buildcraft.transport.gates.GateDefinition;
import buildcraft.transport.gates.GateFactory;

public class TileGenericPipe extends TileEntity implements IPowerReceptor, IFluidHandler,
		IPipeTile, IOverrideDefaultTriggers, ITileBufferHolder,
		IDropControlInventory, ISyncedTile, ISolidSideTile, IGuiReturnHandler {

	public boolean initialized = false;
	public final PipeRenderState renderState = new PipeRenderState();
	public final CoreState coreState = new CoreState();
	public boolean[] pipeConnectionsBuffer = new boolean[6];
	public SafeTimeTracker networkSyncTracker = new SafeTimeTracker();

	@MjBattery
	public Pipe pipe;
	public int redstoneInput = 0;

	private boolean deletePipe = false;
	private TileBuffer[] tileBuffer;
	private boolean sendClientUpdate = false;
	private boolean blockNeighborChange = false;
	private boolean refreshRenderState = false;
	private boolean pipeBound = false;
	private boolean resyncGateExpansions = false;

	public class CoreState implements IClientState {

		public int pipeId = -1;
		public int gateMaterial = -1;
		public int gateLogic = -1;
		public final Set<Byte> expansions = new HashSet<Byte>();

		@Override
		public void writeData(ByteBuf data) {
			data.writeInt(pipeId);
			data.writeByte(gateMaterial);
			data.writeByte(gateLogic);
			data.writeByte(expansions.size());
			for (Byte expansion : expansions) {
				data.writeByte(expansion);
			}
		}

		@Override
		public void readData(ByteBuf data) {
			pipeId = data.readInt();
			gateMaterial = data.readByte();
			gateLogic = data.readByte();
			expansions.clear();
			int numExp = data.readByte();
			for (int i = 0; i < numExp; i++) {
				expansions.add(data.readByte());
			}
		}
	}

	public static class SideProperties {
		ItemFacade.FacadeState[][] facadeStates = new ItemFacade.FacadeState[ForgeDirection.VALID_DIRECTIONS.length][];

		boolean[] plugs = new boolean[ForgeDirection.VALID_DIRECTIONS.length];
		boolean[] robotStations = new boolean[ForgeDirection.VALID_DIRECTIONS.length];

		public void writeToNBT (NBTTagCompound nbt) {
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				NBTTagList list = ItemFacade.FacadeState.writeArray(facadeStates[i]);
				if (list != null) {
					nbt.setTag("facadeState[" + i + "]", list);
				} else {
					nbt.removeTag("facadeState[" + i + "]");
				}
				nbt.setBoolean("plug[" + i + "]", plugs[i]);
				nbt.setBoolean("robotStation[" + i + "]", robotStations[i]);
			}
		}

		public void readFromNBT (NBTTagCompound nbt) {
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				if (nbt.hasKey("facadeState[" + i + "]")) {
					facadeStates[i] = ItemFacade.FacadeState.readArray(nbt.getTagList("facadeState[" + i + "]", Constants.NBT.TAG_COMPOUND));
				} else {
					// Migration support for 5.0.x and 6.0.x
					if (nbt.hasKey("facadeBlocks[" + i + "]")) {
						// 5.0.x
						Block block = (Block) Block.blockRegistry.getObjectById(nbt.getInteger("facadeBlocks[" + i + "]"));
						int metadata = nbt.getInteger("facadeMeta[" + i + "]");
						facadeStates[i] = new ItemFacade.FacadeState[] {ItemFacade.FacadeState.create(block, metadata)};
					} else if (nbt.hasKey("facadeBlocksStr[" + i + "][0]")) {
						// 6.0.x
						ItemFacade.FacadeState mainState = ItemFacade.FacadeState.create(
								(Block) Block.blockRegistry.getObject(nbt.getString("facadeBlocksStr[" + i + "][0]")),
								nbt.getInteger("facadeMeta[" + i + "][0]")
						);
						if (nbt.hasKey("facadeBlocksStr[" + i + "][1]")) {
							ItemFacade.FacadeState phasedState = ItemFacade.FacadeState.create(
									(Block) Block.blockRegistry.getObject(nbt.getString("facadeBlocksStr[" + i + "][1]")),
									nbt.getInteger("facadeMeta[" + i + "][1]"),
									PipeWire.fromOrdinal(nbt.getInteger("facadeWires[" + i + "]"))
							);
							facadeStates[i] = new ItemFacade.FacadeState[] {mainState, phasedState};
						} else {
							facadeStates[i] = new ItemFacade.FacadeState[] {mainState};
						}
					}
				}

				plugs[i] = nbt.getBoolean("plug[" + i + "]");
				robotStations[i] = nbt.getBoolean("robotStation[" + i + "]");
			}
		}

		public void rotateLeft() {
			ItemFacade.FacadeState[][] newFacadeStates = new ItemFacade.FacadeState[ForgeDirection.VALID_DIRECTIONS.length][];
			boolean[] newPlugs = new boolean[ForgeDirection.VALID_DIRECTIONS.length];
			boolean[] newRobotStations = new boolean[ForgeDirection.VALID_DIRECTIONS.length];

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				ForgeDirection r = dir.getRotation(ForgeDirection.UP);

				newFacadeStates[r.ordinal()] = facadeStates[dir.ordinal()];
				newPlugs[r.ordinal()] = plugs[dir.ordinal()];
				newRobotStations[r.ordinal()] = robotStations[dir.ordinal()];
			}

			facadeStates = newFacadeStates;
			plugs = newPlugs;
			robotStations = newRobotStations;
		}
	}

	private SideProperties sideProperties = new SideProperties();

	public TileGenericPipe() {
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setByte("redstoneInput", (byte) redstoneInput);

		if (pipe != null) {
			nbt.setInteger("pipeId", Item.itemRegistry.getIDForObject(pipe.item));
			pipe.writeToNBT(nbt);
		} else {
			nbt.setInteger("pipeId", coreState.pipeId);
		}

		sideProperties.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		redstoneInput = nbt.getByte("redstoneInput");

		coreState.pipeId = nbt.getInteger("pipeId");
		pipe = BlockGenericPipe.createPipe((Item) Item.itemRegistry.getObjectById(coreState.pipeId));
		bindPipe();

		if (pipe != null) {
			pipe.readFromNBT(nbt);
		} else {
			BCLog.logger.log(Level.WARNING, "Pipe failed to load from NBT at {0},{1},{2}", new Object[]{xCoord, yCoord, zCoord});
			deletePipe = true;
		}

		sideProperties.readFromNBT(nbt);

		for (int i = 0; i < 6; ++i) {
			if (sideProperties.robotStations[i]) {
				DockingStationRegistry.registerStation(new DockingStation(this, ForgeDirection.VALID_DIRECTIONS[i]));
			}
		}
	}

	@Override
	public void invalidate() {
		initialized = false;
		tileBuffer = null;
		if (pipe != null) {
			pipe.invalidate();
		}
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

	// PRECONDITION: worldObj must not be null
	private void refreshRenderState() {
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
		renderState.setIsGateLit(pipe.gate != null ? pipe.gate.isGateActive() : false);
		renderState.setIsGatePulsing(pipe.gate != null ? pipe.gate.isGatePulsing() : false);

		// Facades
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			ItemFacade.FacadeState[] states = sideProperties.facadeStates[direction.ordinal()];
			if (states == null) {
				renderState.facadeMatrix.setFacade(direction, null, 0, true);
				continue;
			}
			// Iterate over all states and activate first proper
			ItemFacade.FacadeState defaultState = null, activeState = null;
			for (ItemFacade.FacadeState state : states) {
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
			renderState.plugMatrix.setConnected(direction, sideProperties.plugs[direction.ordinal()]);
		}

		//RobotStations
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			renderState.robotStationMatrix.setConnected(direction, sideProperties.robotStations[direction.ordinal()]);
		}

		if (renderState.isDirty()) {
			renderState.clean();
			sendUpdateToClient();
		}
	}

	public void initialize(Pipe pipe) {
		this.blockType = getBlockType();

		if (pipe == null) {
			BCLog.logger.log(Level.WARNING, "Pipe failed to initialize at {0},{1},{2}, deleting", new Object[]{xCoord, yCoord, zCoord});
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
	public int injectItem(ItemStack payload, boolean doAdd, ForgeDirection from) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof PipeTransportItems && isPipeConnected(from)) {
			if (doAdd) {
				Position itemPos = new Position(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, from.getOpposite());
				itemPos.moveBackwards(0.4);

				TravelingItem pipedItem = TravelingItem.make(itemPos.x, itemPos.y, itemPos.z, payload);
				((PipeTransportItems) pipe.transport).injectItem(pipedItem, itemPos.orientation);
			}
			return payload.stackSize;
		}
		return 0;
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

		PacketTileState packet = new PacketTileState(this.xCoord, this.yCoord, this.zCoord);
		coreState.expansions.clear();

		if (pipe != null && pipe.gate != null) {
			coreState.gateMaterial = pipe.gate.material.ordinal();
			coreState.gateLogic = pipe.gate.logic.ordinal();
			for (IGateExpansion ex : pipe.gate.expansions.keySet()) {
				coreState.expansions.add(GateExpansions.getServerExpansionID(ex.getUniqueIdentifier()));
			}
		} else {
			coreState.gateMaterial = -1;
			coreState.gateLogic = -1;
		}

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
	public LinkedList<ITrigger> getTriggers() {
		LinkedList<ITrigger> result = new LinkedList<ITrigger>();

		if (BlockGenericPipe.isFullyDefined(pipe) && pipe.hasGate()) {
			result.add(BuildCraftCore.triggerRedstoneActive);
			result.add(BuildCraftCore.triggerRedstoneInactive);
		}

		return result;
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

		if (hasPlug(side) || hasRobotStation(side)) {
			return false;
		}

		if (!BlockGenericPipe.isValid(pipe)) {
			return false;
		}

		if (!(pipe instanceof IPipeConnectionForced) || !((IPipeConnectionForced) pipe).ignoreConnectionOverrides(side)) {
			if (with instanceof IPipeConnection) {
				IPipeConnection.ConnectOverride override = ((IPipeConnection) with).overridePipeConnection(pipe.transport.getPipeType(), side.getOpposite());
				if (override != IPipeConnection.ConnectOverride.DEFAULT) {
					return override == IPipeConnection.ConnectOverride.CONNECT ? true : false;
				}
			}
		}

		if (with instanceof TileGenericPipe) {
			if (((TileGenericPipe) with).hasPlug(side.getOpposite())) {
				return false;
			}
			Pipe otherPipe = ((TileGenericPipe) with).pipe;

			if (!BlockGenericPipe.isValid(otherPipe)) {
				return false;
			}

			if (!otherPipe.canPipeConnect(this, side.getOpposite())) {
				return false;
			}
		}

		return pipe.canPipeConnect(with, side);
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

	public boolean addFacade(ForgeDirection direction, ItemFacade.FacadeState[] states) {
		if (this.getWorldObj().isRemote) {
			return false;
		}

		if (hasFacade(direction)) {
			dropFacadeItem(direction);
		}

		sideProperties.facadeStates[direction.ordinal()] = states;

		worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, getBlock());

		scheduleRenderUpdate();

		return true;
	}

	public boolean hasFacade(ForgeDirection direction) {
		if (direction == null || direction == ForgeDirection.UNKNOWN) {
			return false;
		} else if (this.getWorldObj().isRemote) {
			return renderState.facadeMatrix.getFacadeBlock(direction) != null;
		} else {
			return sideProperties.facadeStates[direction.ordinal()] != null;
		}
	}

	public boolean hasEnabledFacade(ForgeDirection direction) {
		return hasFacade(direction) && !renderState.facadeMatrix.getFacadeTransparent(direction);
	}

	private void dropFacadeItem(ForgeDirection direction) {
		InvUtils.dropItems(worldObj, getFacade(direction), this.xCoord, this.yCoord, this.zCoord);
	}

	public ItemStack getFacade(ForgeDirection direction) {
		return ItemFacade.getFacade(sideProperties.facadeStates[direction.ordinal()]);
	}

	public boolean dropFacade(ForgeDirection direction) {
		if (!hasFacade(direction)) {
			return false;
		}

		if (!worldObj.isRemote) {
			dropFacadeItem(direction);
			sideProperties.facadeStates[direction.ordinal()] = null;
			worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, getBlock());
			scheduleRenderUpdate();
		}

		return true;
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

				if (coreState.gateMaterial == -1) {
					pipe.gate = null;
				} else if (pipe.gate == null) {
					pipe.gate = GateFactory.makeGate(pipe, GateDefinition.GateMaterial.fromOrdinal(coreState.gateMaterial), GateDefinition.GateLogic.fromOrdinal(coreState.gateLogic));
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
		if (pipe.gate != null && !coreState.expansions.isEmpty()) {
			for (byte id : coreState.expansions) {
				IGateExpansion ex = GateExpansions.getExpansionClient(id);
				if (ex != null) {
					if (!pipe.gate.expansions.containsKey(ex)) {
						pipe.gate.addGateExpansion(ex);
					}
				} else {
					resyncGateExpansions = true;
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

		return sideProperties.plugs[side.ordinal()];
	}

	public boolean hasRobotStation(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN) {
			return false;
		}

		if (this.worldObj.isRemote) {
			return renderState.robotStationMatrix.isConnected(side);
		}

		return sideProperties.robotStations[side.ordinal()];
	}

	public boolean removeAndDropPlug(ForgeDirection side) {
		if (!hasPlug(side)) {
			return false;
		}

		if (!worldObj.isRemote) {
			sideProperties.plugs[side.ordinal()] = false;
			InvUtils.dropItems(worldObj, new ItemStack(BuildCraftTransport.plugItem), this.xCoord, this.yCoord, this.zCoord);
			worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, getBlock());
			scheduleNeighborChange(); //To force recalculation of connections
			scheduleRenderUpdate();
		}

		return true;
	}

	public boolean removeAndDropRobotStation(ForgeDirection side) {
		if (!hasRobotStation(side)) {
			return false;
		}

		if (!worldObj.isRemote) {
			sideProperties.robotStations[side.ordinal()] = false;
			InvUtils.dropItems(worldObj, new ItemStack(BuildCraftTransport.robotStationItem), this.xCoord, this.yCoord, this.zCoord);
			worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, getBlock());
			scheduleNeighborChange(); //To force recalculation of connections
			scheduleRenderUpdate();
		}

		return true;
	}

	public boolean addPlug(ForgeDirection forgeDirection) {
		if (hasPlug(forgeDirection)) {
			return false;
		}

		sideProperties.plugs[forgeDirection.ordinal()] = true;
		worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, getBlock());
		scheduleNeighborChange(); //To force recalculation of connections
		scheduleRenderUpdate();
		return true;
	}

	public boolean addRobotStation(ForgeDirection forgeDirection) {
		if (hasRobotStation(forgeDirection)) {
			return false;
		}

		sideProperties.robotStations[forgeDirection.ordinal()] = true;
		worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, getBlock());
		scheduleNeighborChange(); //To force recalculation of connections
		scheduleRenderUpdate();

		if (!worldObj.isRemote) {
			DockingStationRegistry.registerStation(new DockingStation(this, forgeDirection));
		}

		return true;
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
}
