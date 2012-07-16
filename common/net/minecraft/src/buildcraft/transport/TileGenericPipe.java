/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftCore;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.IOverrideDefaultTriggers;
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.IPipe.WireColor;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.IPipeTile;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.LiquidSlot;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.IDropControlInventory;
import net.minecraft.src.buildcraft.core.ITileBufferHolder;
import net.minecraft.src.buildcraft.core.TileBuffer;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.core.network.IndexInPayload;
import net.minecraft.src.buildcraft.core.network.PacketPayload;
import net.minecraft.src.buildcraft.core.network.PacketPipeDescription;
import net.minecraft.src.buildcraft.core.network.PacketTileUpdate;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;
import net.minecraft.src.buildcraft.transport.network.PipeRenderStatePacket;

public class TileGenericPipe extends TileEntity implements IPowerReceptor, ILiquidContainer, IPipeEntry,
		IPipeTile, IOverrideDefaultTriggers, ITileBufferHolder, IPipeConnection, IDropControlInventory, IPipeRenderState {
	
	private PipeRenderState renderState = new PipeRenderState();

	public TileBuffer[] tileBuffer;
	public boolean[] pipeConnectionsBuffer = new boolean[6];

	public SafeTimeTracker networkSyncTracker = new SafeTimeTracker();

	public Pipe pipe;
	private boolean blockNeighborChange = false;
	private boolean refreshRenderState = false;
	private boolean pipeBound = false;
	
	private int[] facadeBlocks = new int[Orientations.dirs().length];
	private int[] facadeMeta = new int[Orientations.dirs().length];
	
	//Store the pipe key to prevent losing pipes when a user forgets to include an addon
	int key; 

	@TileNetworkData
	public int pipeId = -1;

	public TileGenericPipe() {

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (pipe != null) {
			nbttagcompound.setInteger("pipeId", pipe.itemID);
			pipe.writeToNBT(nbttagcompound);
		} else
			nbttagcompound.setInteger("pipeId", key);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		

		key = nbttagcompound.getInteger("pipeId");
		pipe = BlockGenericPipe.createPipe(key);

		if (pipe != null) {
			pipe.readFromNBT(nbttagcompound);
		}
	}

	public void synchronizeIfDelay(int delay) {
		if (APIProxy.isServerSide())
			if (networkSyncTracker.markTimeIfDelay(worldObj, delay))
				CoreProxy.sendToPlayers(getUpdatePacket(), worldObj, xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE,
						mod_BuildCraftCore.instance);
	}

	@Override
	public void invalidate() {
		initialized = false;
		super.invalidate();

//		if (BlockGenericPipe.isValid(pipe))
//			BlockGenericPipe.removePipe(pipe);
	}

	@Override
	public void validate() {
		super.validate();
		bindPipe();
	}

	public boolean initialized = false;

	@Override
	public void updateEntity() {
		
		if (pipe == null)
			return;
		
		if (!initialized)
			initialize(pipe);

		if (!BlockGenericPipe.isValid(pipe))
			return;

		if (blockNeighborChange) {
			
			computeConnections();
			pipe.onNeighborBlockChange(0);
			blockNeighborChange = false;
			refreshRenderState = true;
		}
		
		if (refreshRenderState){
			refreshRenderState();
			refreshRenderState = false;
		}

		PowerProvider provider = getPowerProvider();

		if (provider != null)
			provider.update(this);

		if (pipe != null)
			pipe.updateEntity();
	}

	//PRECONDITION: worldObj must not be null
	private void refreshRenderState() {
		
		//Only done on server/SSP
		if (worldObj.isRemote) return;
		
		// Pipe connections;
		for(Orientations o : Orientations.dirs()){
			renderState.pipeConnectionMatrix.setConnected(o, this.pipeConnectionsBuffer[o.ordinal()]);
		}
		
		// Pipe Textures
		for(Orientations o: Orientations.values()){
			renderState.textureMatrix.setTextureIndex(o, pipe.getTextureIndex(o));
		}
		
		// WireState
		for (IPipe.WireColor color : IPipe.WireColor.values()){
			renderState.wireMatrix.setWire(color, pipe.wireSet[color.ordinal()]);
			for (Orientations direction : Orientations.dirs()){
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
		renderState.setGateTexture(!pipe.hasGate()?0:pipe.gate.getTexture(pipe.isGateActive()));

		// Facades
		for (Orientations direction:Orientations.dirs()){
			int blockId = this.facadeBlocks[direction.ordinal()];
			renderState.facadeMatrix.setConnected(direction, blockId != 0 && Block.blocksList[blockId] != null);
			if (Block.blocksList[blockId] != null){
				Block block = Block.blocksList[blockId];
				renderState.facadeMatrix.setTextureFile(direction, block.getTextureFile());
				renderState.facadeMatrix.setTextureIndex(direction, block.getBlockTextureFromSideAndMetadata(direction.ordinal(), this.facadeMeta[direction.ordinal()]));
			}
		}
		
		
		if (renderState.isDirty()){
			//worldObj.markBlockAsNeedsUpdate(this.xCoord, this.yCoord, this.zCoord);
			worldObj.markBlockNeedsUpdate(this.xCoord, this.yCoord, this.zCoord);
			renderState.clean();
		}
		
	}

	public void initialize(Pipe pipe) {
		
		this.pipe = pipe;
		
		tileBuffer = new TileBuffer[6];

		for (Orientations o : Orientations.dirs()) {
			Position pos = new Position(xCoord, yCoord, zCoord, o);
			pos.moveForwards(1.0);

			tileBuffer[o.ordinal()] = new TileBuffer();
			tileBuffer[o.ordinal()].initialize(worldObj, (int) pos.x, (int) pos.y, (int) pos.z);
		}

		for (Orientations o : Orientations.dirs()) {
			TileEntity tile = getTile(o);

			if (tile instanceof ITileBufferHolder)
				((ITileBufferHolder) tile).blockCreated(o, BuildCraftTransport.genericPipeBlock.blockID, this);
		}

		bindPipe();

		computeConnections();
		scheduleRenderUpdate();

		if (pipe != null)
			pipe.initialize();
		
		initialized = true;
	}

	private void bindPipe() {
		
		if (!pipeBound && pipe != null) {
			
			pipe.setTile(this);
			
			pipeId = pipe.itemID;
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
	public void setPowerProvider(PowerProvider provider) {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor)
			((IPowerReceptor) pipe).setPowerProvider(provider);

	}

	@Override
	public PowerProvider getPowerProvider() {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor)
			return ((IPowerReceptor) pipe).getPowerProvider();
		else
			return null;
	}

	@Override
	public void doWork() {
		if (BlockGenericPipe.isValid(pipe) && pipe instanceof IPowerReceptor)
			((IPowerReceptor) pipe).doWork();
	}

	@Override
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof ILiquidContainer)
			return ((ILiquidContainer) pipe.transport).fill(from, quantity, id, doFill);
		else
			return 0;
	}

	@Override
	public int empty(int quantityMax, boolean doEmpty) {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof ILiquidContainer)
			return ((ILiquidContainer) pipe.transport).empty(quantityMax, doEmpty);
		else
			return 0;
	}

	@Override
	public int getLiquidQuantity() {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof ILiquidContainer)
			return ((ILiquidContainer) pipe.transport).getLiquidQuantity();
		else
			return 0;
	}

	@Override
	public int getLiquidId() {
		if (BlockGenericPipe.isValid(pipe) && pipe.transport instanceof ILiquidContainer)
			return ((ILiquidContainer) pipe.transport).getLiquidId();
		else
			return 0;
	}

	public void scheduleNeighborChange() {
		blockNeighborChange = true;
	}

	@Override
	public void entityEntering(EntityPassiveItem item, Orientations orientation) {
		if (BlockGenericPipe.isValid(pipe))
			pipe.transport.entityEntering(item, orientation);
	}

	@Override
	public boolean acceptItems() {
		if (BlockGenericPipe.isValid(pipe))
			return pipe.transport.acceptItems();
		else
			return false;
	}

	public void handleDescriptionPacket(PipeRenderStatePacket packet) {
		if (worldObj.isRemote){
			if (pipe == null && packet.getPipeId() != 0){
				initialize(BlockGenericPipe.createPipe(packet.getPipeId()));
			}
			renderState = packet.getRenderState();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		}
		
		
		
		return;	
		
	
//		if (pipe == null && packet.payload.intPayload[0] != 0) {
//			
//			initialize(BlockGenericPipe.createPipe(packet.payload.intPayload[0]));
//
//			// Check for wire information
//			pipe.handleWirePayload(packet.payload, new IndexInPayload(1, 0, 0));
//			// Check for gate information
//			if (packet.payload.intPayload.length > 5)
//				pipe.handleGatePayload(packet.payload, new IndexInPayload(5, 0, 0));
//		}
	}

	//@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		if (BlockGenericPipe.isValid(pipe))
			pipe.handlePacket(packet);
	}

	//@Override
	public Packet getUpdatePacket() {
		return null;
		//return new PacketTileUpdate(this).getPacket();
	}

	public Packet getDescriptionPacket() {
		bindPipe();
		PipeRenderStatePacket packet = new PipeRenderStatePacket(this.renderState, this.pipeId, xCoord, yCoord, zCoord);
		return packet.getPacket();
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().maxEnergyReceived;
	}

	@Override
	public LinkedList<Trigger> getTriggers() {
		LinkedList<Trigger> result = new LinkedList<Trigger>();

		if (BlockGenericPipe.isFullyDefined(pipe) && pipe.hasGate()) {
			result.add(BuildCraftCore.triggerRedstoneActive);
			result.add(BuildCraftCore.triggerRedstoneInactive);
		}

		return result;
	}

	@Override
	public LiquidSlot[] getLiquidSlots() {
		return new LiquidSlot[0];
	}

	@Override
	public void blockRemoved(Orientations from) {
		// TODO Auto-generated method stub

	}

	@Override
	public void blockCreated(Orientations from, int blockID, TileEntity tile) {
		if (tileBuffer != null)
			tileBuffer[from.reverse().ordinal()].set(blockID, tile);
	}

	@Override
	public int getBlockId(Orientations to) {
		if (tileBuffer != null)
			return tileBuffer[to.ordinal()].getBlockID();
		else
			return 0;
	}

	@Override
	public TileEntity getTile(Orientations to) {
		if (tileBuffer != null)
			return tileBuffer[to.ordinal()].getTile();
		else
			return null;
	}

	public boolean isPipeConnected(TileEntity with) {
		Pipe pipe1 = pipe;
		Pipe pipe2 = null;

		if (with instanceof TileGenericPipe)
			pipe2 = ((TileGenericPipe) with).pipe;

		if (!BlockGenericPipe.isValid(pipe1))
			return false;

		if (BlockGenericPipe.isValid(pipe2) && !pipe1.transport.getClass().isAssignableFrom(pipe2.transport.getClass())
				&& !pipe1.transport.allowsConnect(pipe2.transport))
			return false;

		if (pipe2 != null && !( pipe2.isPipeConnected(this)))
			return false;

		return pipe1 != null ? pipe1.isPipeConnected(with) : false;
	}

	private void computeConnections() {
		if (tileBuffer != null) {
			boolean[] oldConnections = pipeConnectionsBuffer;
			pipeConnectionsBuffer = new boolean[6];

			for (int i = 0; i < tileBuffer.length; ++i) {
				TileBuffer t = tileBuffer[i];
				t.refresh();

				if (t.getTile() != null) {
					pipeConnectionsBuffer[i] = isPipeConnected(t.getTile());

					if (t.getTile() instanceof TileGenericPipe) {
						TileGenericPipe pipe = (TileGenericPipe) t.getTile();
						pipe.pipeConnectionsBuffer[Orientations.values()[i].reverse().ordinal()] = pipeConnectionsBuffer[i];
					}
				}
			}

			for (int i = 0; i < tileBuffer.length; ++i)
				if (oldConnections[i] != pipeConnectionsBuffer[i]) {
					Position pos = new Position(xCoord, yCoord, zCoord, Orientations.values()[i]);
					pos.moveForwards(1.0);
					scheduleRenderUpdate();
					//worldObj.markBlockAsNeedsUpdate((int) pos.x, (int) pos.y, (int) pos.z);
				}
		}
	}

	@Override
	public boolean isPipeConnected(Orientations with) {
		return pipeConnectionsBuffer[with.ordinal()];
	}

	@Override
	public boolean doDrop() {
		if (BlockGenericPipe.isValid(pipe))
			return pipe.doDrop();
		else
			return false;
	}
	
	
	public void scheduleRenderUpdate(){
		refreshRenderState = true;
	}
	
	public boolean addFacade(Orientations direction, int blockid, int meta){
		if (this.worldObj.isRemote) return false;
		if (this.facadeBlocks[direction.ordinal()] == blockid) return false;
		
		if (hasFacade(direction)){
			dropFacade(direction);
		}
		
		this.facadeBlocks[direction.ordinal()] = blockid;
		this.facadeMeta[direction.ordinal()] = meta;
		scheduleRenderUpdate();
		//refreshRenderState();
		return true;
	}
	
	public boolean hasFacade(Orientations direction){
		if (this.worldObj.isRemote) return false;
		return (this.facadeBlocks[direction.ordinal()] != 0);
	}
	
	public void dropFacade(Orientations direction){
		if (this.worldObj.isRemote) return;
		if (!hasFacade(direction)) return;
		Utils.dropItems(worldObj, new ItemStack(BuildCraftTransport.facadeItem, 1, ItemFacade.encode(this.facadeBlocks[direction.ordinal()], this.facadeMeta[direction.ordinal()])), this.xCoord, this.yCoord, this.zCoord);
		this.facadeBlocks[direction.ordinal()] = 0;
		this.facadeMeta[direction.ordinal()] = 0;
		scheduleRenderUpdate();
		//refreshRenderState();
	}
	
	/** IPipeRenderState implementation **/

	@Override
	public PipeRenderState getRenderState() {
		return renderState;
	}
}
