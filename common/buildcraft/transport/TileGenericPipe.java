/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipe.WireColor;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.IDropControlInventory;
import buildcraft.core.ITileBufferHolder;
import buildcraft.core.TileBuffer;
import buildcraft.core.network.IClientState;
import buildcraft.core.network.ISyncedTile;
import buildcraft.core.network.PacketTileState;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Gate.GateKind;
import buildcraft.transport.network.PipeRenderStatePacket;

public class TileGenericPipe extends TileEntity implements IPowerReceptor, ITankContainer, IPipeEntry, IPipeTile, IOverrideDefaultTriggers, ITileBufferHolder,
		IPipeConnection, IDropControlInventory, IPipeRenderState, ISyncedTile {

	private class CoreState implements IClientState {

		public int pipeId = -1;
		public int gateKind = 0;

		@Override
		public void writeData(DataOutputStream data) throws IOException {
			data.writeInt(pipeId);
			data.writeInt(gateKind);
		}

		@Override
		public void readData(DataInputStream data) throws IOException {
			pipeId = data.readInt();
			gateKind = data.readInt();
		}

	}

	private PipeRenderState renderState = new PipeRenderState();
	private CoreState coreState = new CoreState();

	public TileBuffer[] tileBuffer;
	public boolean[] pipeConnectionsBuffer = new boolean[6];

	public SafeTimeTracker networkSyncTracker = new SafeTimeTracker();

	public Pipe pipe;
	private boolean blockNeighborChange = false;
	private boolean refreshRenderState = false;
	private boolean pipeBound = false;

	private int[] facadeBlocks = new int[ForgeDirection.VALID_DIRECTIONS.length];
	private int[] facadeMeta = new int[ForgeDirection.VALID_DIRECTIONS.length];

	public TileGenericPipe() {

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (pipe != null) {
			nbttagcompound.setInteger("pipeId", pipe.itemID);
			pipe.writeToNBT(nbttagcompound);
		} else {
			nbttagcompound.setInteger("pipeId", coreState.pipeId);
		}

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			nbttagcompound.setInteger("facadeBlocks[" + i + "]", facadeBlocks[i]);
			nbttagcompound.setInteger("facadeMeta[" + i + "]", facadeMeta[i]);
		}

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		coreState.pipeId = nbttagcompound.getInteger("pipeId");
		pipe = BlockGenericPipe.createPipe(coreState.pipeId);

		if (pipe != null) {
			pipe.readFromNBT(nbttagcompound);
		} else {
			BuildCraftCore.bcLog.warning("Pipe failed to load from NBT at "+xCoord+","+yCoord+","+zCoord);
        }

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			facadeBlocks[i] = nbttagcompound.getInteger("facadeBlocks[" + i + "]");
			facadeMeta[i] = nbttagcompound.getInteger("facadeMeta[" + i + "]");
		}

	}

	@Override
	public void invalidate() {
		initialized = false;
		if (pipe != null) {
			pipe.invalidate();
		}
		super.invalidate();
	}

	@Override
	public void validate() {
		super.validate();
		bindPipe();
		if (pipe != null) {
			pipe.validate();
		}
	}

	public boolean initialized = false;

	@Override
	public void updateEntity() {

		if (pipe == null)
			return;

		if (!initialized) {
			initialize(pipe);
		}

		if (!BlockGenericPipe.isValid(pipe))
			return;

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

		IPowerProvider provider = getPowerProvider();

		if (provider != null) {
			provider.update(this);
		}

		if (pipe != null) {
			pipe.updateEntity();
		}
	}

	// PRECONDITION: worldObj must not be null
	private void refreshRenderState() {

		// Only done on server/SSP
		if (worldObj.isRemote)
			return;

		// Pipe connections;
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			renderState.pipeConnectionMatrix.setConnected(o, this.pipeConnectionsBuffer[o.ordinal()]);
		}

		// Pipe Textures
		renderState.setTextureFile(pipe.getTextureFile());
		for (ForgeDirection o : ForgeDirection.values()) {
			renderState.textureMatrix.setTextureIndex(o, pipe.getTextureIndex(o));
		}

		// WireState
		for (IPipe.WireColor color : IPipe.WireColor.values()) {
			renderState.wireMatrix.setWire(color, pipe.wireSet[color.ordinal()]);
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				renderState.wireMatrix.setWireConnected(color, direction, pipe.isWireConnectedTo(this.getTile(direction), color));
			}
		}

		// Wire Textures

		if (pipe.wireSet[IPipe.WireColor.Red.ordinal()]) {
			renderState.wireMatrix.setTextureIndex(WireColor.Red, pipe.signalStrength[IPipe.WireColor.Red.ordinal()] > 0 ? 6 : 5);
		} else {
			renderState.wireMatrix.setTextureIndex(WireColor.Red, 0);
		}

		if (pipe.wireSet[IPipe.WireColor.Blue.ordinal()]) {
			renderState.wireMatrix.setTextureIndex(WireColor.Blue, pipe.signalStrength[IPipe.WireColor.Blue.ordinal()] > 0 ? 8 : 7);
		} else {
			renderState.wireMatrix.setTextureIndex(WireColor.Blue, 0);
		}

		if (pipe.wireSet[IPipe.WireColor.Green.ordinal()]) {
			renderState.wireMatrix.setTextureIndex(WireColor.Green, pipe.signalStrength[IPipe.WireColor.Green.ordinal()] > 0 ? 10 : 9);
		} else {
			renderState.wireMatrix.setTextureIndex(WireColor.Green, 0);
		}

		if (pipe.wireSet[IPipe.WireColor.Yellow.ordinal()]) {
			renderState.wireMatrix.setTextureIndex(WireColor.Yellow, pipe.signalStrength[IPipe.WireColor.Yellow.ordinal()] > 0 ? 12 : 11);
		} else {
			renderState.wireMatrix.setTextureIndex(WireColor.Yellow, 0);
		}

		// Gate Textures
		renderState.setHasGate(pipe.hasGate());
		renderState.setGateTexture(!pipe.hasGate() ? 0 : pipe.gate.getTexture(pipe.isGateActive()));

		// Facades
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			int blockId = this.facadeBlocks[direction.ordinal()];
			renderState.facadeMatrix.setConnected(direction, blockId != 0 && Block.blocksList[blockId] != null);
			if (Block.blocksList[blockId] != null) {
				Block block = Block.blocksList[blockId];
				renderState.facadeMatrix.setTextureFile(direction, block.getTextureFile());
				renderState.facadeMatrix.setTextureIndex(direction,
						block.getBlockTextureFromSideAndMetadata(direction.ordinal(), this.facadeMeta[direction.ordinal()]));
			}
		}

		if (renderState.isDirty()) {
			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			renderState.clean();
		}

	}

	public void initialize(Pipe pipe) {

		this.pipe = pipe;

		tileBuffer = new TileBuffer[6];

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			Position pos = new Position(xCoord, yCoord, zCoord, o);
			pos.moveForwards(1.0);

			tileBuffer[o.ordinal()] = new TileBuffer();
			tileBuffer[o.ordinal()].initialize(worldObj, (int) pos.x, (int) pos.y, (int) pos.z);
		}

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = getTile(o);

			if (tile instanceof ITileBufferHolder) {
				((ITileBufferHolder) tile).blockCreated(o, BuildCraftTransport.genericPipeBlock.blockID, this);
			}
		}

		bindPipe();

		computeConnections();
		scheduleRenderUpdate();

		if (pipe != null) {
			pipe.initialize();
		} else {
			BuildCraftCore.bcLog.warning("Pipe failed to initialize pipe at "+xCoord+","+yCoord+","+zCoord);
		}

		initialized = true;
	}

	private void bindPipe() {

		if (!pipeBound && pipe != null) {

			pipe.setTile(this);

			coreState.pipeId = pipe.itemID;
			pipeBound = true;
		}
	}

	@Override
	public IPipe getPipe() {
		return pipe;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).setPowerProvider(provider);
		}

	}

	@Override
	public IPowerProvider getPowerProvider() {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor)
			return ((IPowerReceptor) pipe).getPowerProvider();
		else
			return null;
	}

	@Override
	public void doWork() {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).doWork();
		}
	}

	public void scheduleNeighborChange() {
		blockNeighborChange = true;
	}

	/* IPIPEENTRY */
	@Override
	public void entityEntering(IPipedItem item, ForgeDirection orientation) {
		if (BlockGenericPipe.isValid(pipe)) {
			pipe.transport.entityEntering(item, orientation);
		}
	}

	@Override
	public void entityEntering(ItemStack payload, ForgeDirection orientation) {

		/* FIXME: This is untested guesswork */
		Position itemPos = new Position(xCoord, yCoord, zCoord, orientation);
		itemPos.moveBackwards(1.0);

		itemPos.x += 0.5;
		itemPos.y += 0.25;
		itemPos.z += 0.5;
		itemPos.moveForwards(0.5);

		EntityPassiveItem pipedItem = new EntityPassiveItem(worldObj, itemPos.x, itemPos.y, itemPos.z, payload);
		entityEntering(pipedItem, orientation);
	}

	@Override
	public boolean acceptItems() {
		if (BlockGenericPipe.isValid(pipe))
			return pipe.transport.acceptItems();
		else
			return false;
	}

	/* SMP */
	public void handleDescriptionPacket(PipeRenderStatePacket packet) {
		if (worldObj.isRemote) {
			if (pipe == null && packet.getPipeId() != 0) {
				initialize(BlockGenericPipe.createPipe(packet.getPipeId()));
			}
			renderState = packet.getRenderState();
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		}
		return;
	}

	@Override
	public Packet getDescriptionPacket() {
		bindPipe();

		PacketTileState packet = new PacketTileState(this.xCoord, this.yCoord, this.zCoord);
		if (pipe != null && pipe.gate != null) {
			coreState.gateKind = pipe.gate.kind.ordinal();
		} else {
			coreState.gateKind = 0;
		}

		if (pipe != null && pipe.transport != null) {
			pipe.transport.sendDescriptionPacket();
		}

		packet.addStateForSerialization((byte) 0, coreState);
		packet.addStateForSerialization((byte) 1, renderState);
		if (pipe instanceof IClientState) {
			packet.addStateForSerialization((byte) 2, (IClientState) pipe);
		}
		return packet.getPacket();
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().getMaxEnergyReceived();
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
		// TODO Auto-generated method stub

	}

	@Override
	public void blockCreated(ForgeDirection from, int blockID, TileEntity tile) {
		if (tileBuffer != null) {
			tileBuffer[from.getOpposite().ordinal()].set(blockID, tile);
		}
	}

	@Override
	public int getBlockId(ForgeDirection to) {
		if (tileBuffer != null)
			return tileBuffer[to.ordinal()].getBlockID();
		else
			return 0;
	}

	@Override
	public TileEntity getTile(ForgeDirection to) {
		if (tileBuffer != null)
			return tileBuffer[to.ordinal()].getTile();
		else
			return null;
	}

	public boolean isPipeConnected(TileEntity with, ForgeDirection side) {
		Pipe pipe1 = pipe;
		Pipe pipe2 = null;

		if (with instanceof TileGenericPipe) {
			pipe2 = ((TileGenericPipe) with).pipe;
		}

		if (!BlockGenericPipe.isValid(pipe1))
			return false;

		if (BlockGenericPipe.isValid(pipe2) && !pipe1.transport.getClass().isAssignableFrom(pipe2.transport.getClass())
				&& !pipe1.transport.allowsConnect(pipe2.transport))
			return false;

		if (pipe2 != null && !(pipe2.isPipeConnected(this, side)))
			return false;

		return pipe1 != null ? pipe1.isPipeConnected(with, side) : false;
	}

	private void computeConnections() {
		if (tileBuffer != null) {
			boolean[] oldConnections = pipeConnectionsBuffer;
			pipeConnectionsBuffer = new boolean[6];

			for (int i = 0; i < tileBuffer.length; ++i) {
				TileBuffer t = tileBuffer[i];
				t.refresh();

				if (t.getTile() != null) {
					pipeConnectionsBuffer[i] = isPipeConnected(t.getTile(), ForgeDirection.VALID_DIRECTIONS[i].getOpposite());

					if (t.getTile() instanceof TileGenericPipe) {
						TileGenericPipe pipe = (TileGenericPipe) t.getTile();
						pipe.pipeConnectionsBuffer[ForgeDirection.VALID_DIRECTIONS[i].getOpposite().ordinal()] = pipeConnectionsBuffer[i];
					}
				}
			}
		}
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with) {
		return pipeConnectionsBuffer[with.ordinal()];
	}

	@Override
	public boolean doDrop() {
		if (BlockGenericPipe.isValid(pipe))
			return pipe.doDrop();
		else
			return false;
	}

	@Override
	public void onChunkUnload() {
		if (pipe != null) {
			pipe.onChunkUnload();
		}
	}

	/** ITankContainer implementation **/

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof ITankContainer)
			return ((ITankContainer) pipe.transport).fill(from, resource, doFill);
		else
			return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof ITankContainer)
			return ((ITankContainer) pipe.transport).fill(tankIndex, resource, doFill);
		else
			return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof ITankContainer)
			return ((ITankContainer) pipe.transport).drain(from, maxDrain, doDrain);
		else
			return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof ITankContainer)
			return ((ITankContainer) pipe.transport).drain(tankIndex, maxDrain, doDrain);
		else
			return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof ITankContainer)
			return ((ITankContainer) pipe.transport).getTanks(direction);
		else
			return null;
	}

	public void scheduleRenderUpdate() {
		refreshRenderState = true;
	}

	public boolean addFacade(ForgeDirection direction, int blockid, int meta) {
		if (this.worldObj.isRemote)
			return false;
		if (this.facadeBlocks[direction.ordinal()] == blockid)
			return false;

		if (hasFacade(direction)) {
			dropFacade(direction);
		}

		this.facadeBlocks[direction.ordinal()] = blockid;
		this.facadeMeta[direction.ordinal()] = meta;
		scheduleRenderUpdate();
		return true;
	}

	public boolean hasFacade(ForgeDirection direction) {
		if (this.worldObj.isRemote)
			return renderState.facadeMatrix.isConnected(direction);
		return (this.facadeBlocks[direction.ordinal()] != 0);
	}

	public void dropFacade(ForgeDirection direction) {
		if (this.worldObj.isRemote)
			return;
		if (!hasFacade(direction))
			return;
		Utils.dropItems(
				worldObj,
				new ItemStack(BuildCraftTransport.facadeItem, 1,
						ItemFacade.encode(this.facadeBlocks[direction.ordinal()], this.facadeMeta[direction.ordinal()])), this.xCoord, this.yCoord, this.zCoord);
		this.facadeBlocks[direction.ordinal()] = 0;
		this.facadeMeta[direction.ordinal()] = 0;
		scheduleRenderUpdate();
	}

	/** IPipeRenderState implementation **/

	@Override
	public PipeRenderState getRenderState() {
		return renderState;
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
		if (!worldObj.isRemote)
			return;

		switch (stateId) {
		case 0:
			if (pipe == null && coreState.pipeId != 0) {
				initialize(BlockGenericPipe.createPipe(coreState.pipeId));
			}
			if (pipe != null && coreState.gateKind != GateKind.None.ordinal()) {
				if (pipe.gate == null) {
					pipe.gate = new GateVanilla(pipe);
				}
				pipe.gate.kind = GateKind.values()[coreState.gateKind];
			}
			break;
		}
		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double func_82115_m() {
		return DefaultProps.PIPE_CONTENTS_RENDER_DIST * DefaultProps.PIPE_CONTENTS_RENDER_DIST;
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof ITankContainer)
			return ((ITankContainer) pipe.transport).getTank(direction, type);
		else
			return null;
	}

	@Override
	public boolean shouldRefresh(int oldID, int newID, int oldMeta, int newMeta, World world, int x, int y, int z) {
		return oldID != newID;
	}
}
