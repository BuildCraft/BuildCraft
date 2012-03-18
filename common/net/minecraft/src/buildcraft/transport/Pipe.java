/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Action;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.IActionReceptor;
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.api.TriggerParameter;
import net.minecraft.src.buildcraft.core.ActionRedstoneOutput;
import net.minecraft.src.buildcraft.core.IDropControlInventory;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.PersistentTile;
import net.minecraft.src.buildcraft.core.TilePacketWrapper;
import net.minecraft.src.buildcraft.core.Utils;

public class Pipe extends PersistentTile implements IPipe, IDropControlInventory {
		
	public int [] signalStrength = new int [] {0, 0, 0, 0};
	
	public int xCoord;
	public int yCoord;
	public int zCoord;
	public World worldObj;
	public TileGenericPipe container;
	
	public final PipeTransport transport;
	public final PipeLogic logic;
	public final int itemID;
	
	private TilePacketWrapper networkPacket;
	
	private boolean internalUpdateScheduled = false;
	
	@TileNetworkData (intKind = TileNetworkData.UNSIGNED_BYTE)
	public boolean [] wireSet = new boolean [] {false, false, false, false};
	
	public static enum GateKind {None, Single, AND_2, OR_2, AND_3, OR_3, AND_4, OR_4};
	public GateKind gateKind = GateKind.None;
	
	@SuppressWarnings("rawtypes")
	private static Map<Class, TilePacketWrapper> networkWrappers = new HashMap<Class, TilePacketWrapper>();
	
	Trigger [] activatedTriggers = new Trigger [8];
	TriggerParameter [] triggerParameters = new TriggerParameter [8];
	Action[] activatedActions = new Action [8];

	public boolean broadcastSignal[] = new boolean[] {false, false, false, false};
	public boolean broadcastRedstone = false;
	
	public SafeTimeTracker actionTracker = new SafeTimeTracker();
	
	public Pipe(PipeTransport transport, PipeLogic logic, int itemID) {
		this.transport = transport;
		this.logic = logic;
		this.itemID = itemID;

		if (!networkWrappers.containsKey(this.getClass())) {
			networkWrappers.put(
					this.getClass(),
					new TilePacketWrapper(new Class[] {
							TileGenericPipe.class,
							this.transport.getClass(),
							this.logic.getClass() },
							PacketIds.TileUpdate));
		}

		this.networkPacket = networkWrappers.get(this.getClass());
		
		
	}

	
	public void setPosition (int xCoord, int yCoord, int zCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		
		transport.setPosition(xCoord, yCoord, zCoord);
		logic.setPosition(xCoord, yCoord, zCoord);
	}
	
	public void setWorld (World worldObj) {		
		if (worldObj != null && this.worldObj == null) {
			this.worldObj = worldObj;
			transport.setWorld(worldObj);
			logic.setWorld(worldObj);			
		}
	}
	
	@Override
	public void setTile (TileEntity tile) {
		super.setTile(tile);
		
		this.container = (TileGenericPipe) tile;
		
		transport.setTile ((TileGenericPipe) tile);
		logic.setTile ((TileGenericPipe) tile);
		
		setPosition (tile.xCoord, tile.yCoord, tile.zCoord);
	}

	
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		return logic.blockActivated(entityplayer);
	}
	
	public void onBlockPlaced() {
		logic.onBlockPlaced();
		transport.onBlockPlaced ();
	}
		
	public void onNeighborBlockChange(int blockId) {		
		logic.onNeighborBlockChange (blockId);
		transport.onNeighborBlockChange (blockId);
		
		updateSignalState();
	}
	
	public boolean isPipeConnected(TileEntity tile) {	   
    	return logic.isPipeConnected(tile) && transport.isPipeConnected (tile);
	}
	
	public final int getPipeTexture() {
		if (drawingState == DrawingState.DrawingPipe) {
			return getMainBlockTexture();
		} else if (drawingState == DrawingState.DrawingRedWire) {
			if (signalStrength [IPipe.WireColor.Red.ordinal()] > 0) {
				return 6;
			} else {
				return 5;
			}
		} else if (drawingState == DrawingState.DrawingBlueWire) {
			if (signalStrength [IPipe.WireColor.Blue.ordinal()] > 0) {
				return 8;
			} else {
				return 7;
			}
		} else if (drawingState == DrawingState.DrawingGreenWire) {
			if (signalStrength [IPipe.WireColor.Green.ordinal()] > 0) {
				return 10;
			} else {
				return 9;
			}
		} else if (drawingState == DrawingState.DrawingYellowWire) {
			if (signalStrength [IPipe.WireColor.Yellow.ordinal()] > 0) {
				return 12;
			} else {
				return 11;
			}
		} else if (drawingState == DrawingState.DrawingGate) {
			boolean activeSignal = false;
			
			for (boolean b : broadcastSignal) {
				if (b) {
					activeSignal = true;
				}
			}
			
			switch (gateKind) {
			case None:
				break;
			case Single:
				if (!activeSignal && !broadcastRedstone) {
					return 8 * 16 + 12;
				} else {
					return 8 * 16 + 13;
				}
			case AND_2:
				if (!activeSignal && !broadcastRedstone) {
					return 6 * 16 + 7;
				} else {
					return 6 * 16 + 8;
				}
			case OR_2:
				if (!activeSignal && !broadcastRedstone) {
					return 6 * 16 + 9;
				} else {
					return 6 * 16 + 10;
				}
			case AND_3:
				if (!activeSignal && !broadcastRedstone) {
					return 8 * 16 + 4;
				} else {
					return 8 * 16 + 5;
				}
			case OR_3:
				if (!activeSignal && !broadcastRedstone) {
					return 8 * 16 + 6;
				} else {
					return 8 * 16 + 7;
				}
			case AND_4:
				if (!activeSignal && !broadcastRedstone) {
					return 8 * 16 + 8;
				} else {
					return 8 * 16 + 9;
				}
			case OR_4:
				if (!activeSignal && !broadcastRedstone) {
					return 8 * 16 + 10;
				} else {
					return 8 * 16 + 11;
				}
			}
		}
		
		return getPipeTexture();
	}
	
	public int getMainBlockTexture () {
		return 1 * 16 + 0;
	}

	@Override
	public void prepareTextureFor(Orientations connection) {

	}

	public void updateEntity() {
		transport.updateEntity ();		
		logic.updateEntity ();
		
		if (internalUpdateScheduled) {
			internalUpdate ();
			internalUpdateScheduled = false;
		}
		
		if (actionTracker.markTimeIfDelay(worldObj, 10)) {
			resolveActions();
		}
	}
	
	private void internalUpdate() {
		updateSignalState();
	}


	public void writeToNBT(NBTTagCompound nbttagcompound) {
		transport.writeToNBT(nbttagcompound);
		logic.writeToNBT(nbttagcompound);
		
		nbttagcompound.setInteger("gateKind", gateKind.ordinal());
		
		for (int i = 0; i < 4; ++i) {
			nbttagcompound.setBoolean("wireSet[" + i + "]", wireSet [i]);
		}
		
		for (int i = 0; i < 8; ++i) {
			nbttagcompound.setInteger("action[" + i + "]", activatedActions [i] != null ? activatedActions [i].id : 0);
			nbttagcompound.setInteger("trigger[" + i + "]", activatedTriggers [i] != null ? activatedTriggers [i].id : 0);
		}
		
		for (int i = 0; i < 8; ++i) {
			if (triggerParameters [i] != null) {
				NBTTagCompound cpt = new NBTTagCompound();
				triggerParameters [i].writeToNBT(cpt);
				nbttagcompound.setTag("triggerParameters[" + i + "]", cpt);
			}
		}
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		transport.readFromNBT(nbttagcompound);
		logic.readFromNBT(nbttagcompound);
		
		gateKind = GateKind.values() [nbttagcompound.getInteger("gateKind")];
		
		for (int i = 0; i < 4; ++i) {
			wireSet [i] = nbttagcompound.getBoolean("wireSet[" + i + "]");
		}
		
		for (int i = 0; i < 8; ++i) {
			activatedActions [i] = BuildCraftAPI.actions [nbttagcompound.getInteger("action[" + i + "]")];
			activatedTriggers [i] = BuildCraftAPI.triggers [nbttagcompound.getInteger("trigger[" + i + "]")];
		}
		
		for (int i = 0; i < 8; ++i) {
			if (nbttagcompound.hasKey("triggerParameters[" + i + "]")) {
				triggerParameters[i] = new TriggerParameter();
				triggerParameters[i].readFromNBT(nbttagcompound
						.getCompoundTag("triggerParameters[" + i + "]"));
			}
		}
	}
	
	private boolean initialized = false;

	public void initialize () {
		if (!initialized) {
			transport.initialize ();
			logic.initialize ();
			initialized = true;
			updateSignalState ();
		}
	}

	private void readNearbyPipesSignal (WireColor color) {
		boolean foundBiggerSignal = false;
		
		for (Orientations o : Orientations.dirs()) {
			TileEntity tile = container.getTile(o);
			
			if (tile instanceof TileGenericPipe) {
				TileGenericPipe tilePipe = (TileGenericPipe) tile;
				
				if (BlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
					if (isWireConnectedTo(tile, color)) {
						foundBiggerSignal |= receiveSignal(
								tilePipe.pipe.signalStrength[color.ordinal()] - 1,
								color);
					}
				}
			}
		}
		
		if (!foundBiggerSignal && signalStrength [color.ordinal()] != 0) {
			signalStrength [color.ordinal()]= 0;
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
			
			for (Orientations o : Orientations.dirs()) {
				TileEntity tile = container.getTile(o);
				
				if (tile instanceof TileGenericPipe) {
					TileGenericPipe tilePipe = (TileGenericPipe) tile;
					
					if (BlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
						tilePipe.pipe.internalUpdateScheduled = true;
					}
				}
			}
		}
	}
	
	private void updateSignalState() {
		for (IPipe.WireColor c : IPipe.WireColor.values()) {
			updateSignalStateForColor(c);
		}
	}
	
	private void updateSignalStateForColor(IPipe.WireColor color) {
		if (!wireSet [color.ordinal()]) {
			return;
		}
		
		// STEP 1: compute internal signal strength
		
		if (broadcastSignal [color.ordinal()]) {
			receiveSignal(255, color);
		} else {
			readNearbyPipesSignal(color);
		}
		
		// STEP 2: transmit signal in nearby blocks
		
		if (signalStrength [color.ordinal()] > 1) {
			for (Orientations o : Orientations.dirs()) {
				TileEntity tile = container.getTile(o);
				
				if (tile instanceof TileGenericPipe) {
					TileGenericPipe tilePipe = (TileGenericPipe) tile;

					if (BlockGenericPipe.isFullyDefined(tilePipe.pipe)
							&& tilePipe.pipe.wireSet [color.ordinal()]) {
						if (isWireConnectedTo(tile, color)) {
							tilePipe.pipe.receiveSignal(
									signalStrength[color.ordinal()] - 1, color);
						}
					}
				}
			}
		}
	}
	
	private boolean receiveSignal (int signal, IPipe.WireColor color) {
		if (worldObj == null) {
			return false;
		}
		
		int oldSignal = signalStrength [color.ordinal()];
		
		if (signal >= signalStrength [color.ordinal()] && signal != 0) {
			signalStrength [color.ordinal()] = signal;			
			internalUpdateScheduled = true;			
		
			if (oldSignal == 0) {
				worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);								
			}
			
			return true;
		} else {		
			return false;
		}
	}

	public boolean inputOpen(Orientations from) {
		return transport.inputOpen (from) && logic.inputOpen (from);
	}

	public boolean outputOpen(Orientations to) {
		return transport.outputOpen (to) && logic.outputOpen (to);
	}

	public void onEntityCollidedWithBlock(Entity entity) {
				
	}
	
	public Packet230ModLoader getNetworkPacket() {
		return networkPacket.toPacket(xCoord, yCoord, zCoord, new Object[] {
				container, transport, logic });
	}
	
	public void handlePacket (Packet230ModLoader packet) {
		networkPacket.updateFromPacket(new Object [] {container, transport, logic}, packet);
	}


	public boolean isPoweringTo(int l) {		
		if (!broadcastRedstone) {
			return false;
		}
		
		Orientations o = Orientations.values() [l].reverse();
		TileEntity tile = container.getTile(o);
		
		if (tile instanceof TileGenericPipe
				&& Utils.checkPipesConnections(this.container, tile)) {
			return false;
		}
		
		return true;
	}


	public boolean isIndirectlyPoweringTo(int l) {
		return isPoweringTo(l);
	}


	public void randomDisplayTick(Random random) {
	}
	
	private DrawingState drawingState = DrawingState.DrawingPipe;
	
	@Override
	public void setDrawingState (DrawingState state) {
		drawingState = state;
	}
	
	@Override
	public boolean isWired (WireColor color) {
		return wireSet [color.ordinal()];
	}
	
	@Override
	public boolean hasInterface () {
		return gateKind != GateKind.None;
	}


	public void onBlockRemoval() {
		if (wireSet [IPipe.WireColor.Red.ordinal()]) {
			Utils.dropItems(worldObj, new ItemStack(BuildCraftTransport.redPipeWire), xCoord,
					yCoord, zCoord);
		}
		
		if (wireSet [IPipe.WireColor.Blue.ordinal()]) {
			Utils.dropItems(worldObj, new ItemStack(BuildCraftTransport.bluePipeWire), xCoord,
					yCoord, zCoord);
		}
		
		if (wireSet [IPipe.WireColor.Green.ordinal()]) {
			Utils.dropItems(worldObj, new ItemStack(BuildCraftTransport.greenPipeWire), xCoord,
					yCoord, zCoord);
		}

		if (wireSet [IPipe.WireColor.Yellow.ordinal()]) {
			Utils.dropItems(worldObj, new ItemStack(BuildCraftTransport.yellowPipeWire), xCoord,
					yCoord, zCoord);		
		}

		if (gateKind != GateKind.None) {
			Utils.dropItems(worldObj, new ItemStack(
					BuildCraftTransport.pipeGate, 1, gateKind.ordinal() - 1),
					xCoord, yCoord, zCoord);
		}
	}
	
	public void setTrigger (int position, Trigger trigger) {
		activatedTriggers [position] = trigger;
	}
	
	public Trigger getTrigger (int position) {
		return activatedTriggers [position];
	}
	
	public void setTriggerParameter (int position, TriggerParameter p) {
		triggerParameters [position] = p;
	}
	
	public TriggerParameter getTriggerParameter (int position) {
		return triggerParameters [position];
	}
	
	public boolean isNearbyTriggerActive(Trigger trigger, TriggerParameter parameter) {
		if (trigger instanceof ITriggerPipe) {
			return ((ITriggerPipe) trigger).isTriggerActive(this, parameter);
		} else if (trigger != null) {
			for (Orientations o : Orientations.dirs()) {
				TileEntity tile = container.getTile(o);

				if (tile != null && !(tile instanceof TileGenericPipe)
						&& trigger.isTriggerActive(tile, parameter)) {
					return true;
				}
			}
		}

		return false;
	}
	
	public boolean isTriggerActive (Trigger trigger) {
		return false;
	}
	
	public LinkedList<Action> getActions () {
		LinkedList<Action> result = new LinkedList<Action>();
		
		if (wireSet [IPipe.WireColor.Red.ordinal()] && gateKind.ordinal() >= GateKind.AND_2.ordinal()) {
			result.add (BuildCraftTransport.actionRedSignal);
		}
		
		if (wireSet [IPipe.WireColor.Blue.ordinal()] && gateKind.ordinal() >= GateKind.AND_3.ordinal()) {
			result.add (BuildCraftTransport.actionBlueSignal);
		}
		
		if (wireSet [IPipe.WireColor.Green.ordinal()] && gateKind.ordinal() >= GateKind.AND_4.ordinal()) {
			result.add (BuildCraftTransport.actionGreenSignal);
		}

		if (wireSet [IPipe.WireColor.Yellow.ordinal()] && gateKind.ordinal() >= GateKind.AND_4.ordinal()) {
			result.add (BuildCraftTransport.actionYellowSignal);
		}
		
		return result;
	}


	public Action getAction(int position) {
		return activatedActions [position];
	}


	public void setAction(int position, Action action) {
		activatedActions [position] = action;		
	}
	
	private void resolveActions () {
		if (gateKind == GateKind.None) {
			return;
		}
		
		boolean oldBroadcastRedstone = broadcastRedstone;
		boolean [] oldBroadcastSignal = broadcastSignal;
		
		broadcastRedstone = false;
		broadcastSignal = new boolean [] {false, false, false, false};
		
		HashMap<Integer, Boolean> actions = new HashMap<Integer, Boolean> ();
		
		// Computes the actions depending on the triggers
		
		for (int it = 0; it < 8; ++it) {
			Trigger trigger = activatedTriggers [it];
			Action action = activatedActions [it];
			TriggerParameter parameter = triggerParameters [it];
			
			if (trigger != null && action != null) {
				if (!actions.containsKey(action.id)) {
					actions.put(action.id, isNearbyTriggerActive(trigger, parameter));
				} else {
					if (gateKind == GateKind.AND_2
							|| gateKind == GateKind.AND_3
							|| gateKind == GateKind.AND_4) {
						actions.put(action.id, actions.get(action.id)
								&& isNearbyTriggerActive(trigger, parameter));
					} else {
						actions.put(action.id, actions.get(action.id)
								|| isNearbyTriggerActive(trigger, parameter));
					}
				}
			}
		}
		
		// Activate the actions
		
		for (Integer i : actions.keySet()) {
			if (actions.get(i)) {
				if (BuildCraftAPI.actions [i] instanceof ActionRedstoneOutput) {
					broadcastRedstone = true;
				} else if (BuildCraftAPI.actions [i] instanceof ActionSignalOutput) {
					broadcastSignal[((ActionSignalOutput) BuildCraftAPI.actions[i]).color
							.ordinal()] = true;
				} else {
					for (int a = 0; a < container.tileBuffer.length; ++a) {
						if (container.tileBuffer[a].getTile() instanceof IActionReceptor) {
							IActionReceptor recept = (IActionReceptor) container.tileBuffer[a].getTile();
							recept.actionActivated(BuildCraftAPI.actions[i]);
						}
					}
				}
			}
		}		
		
		actionsActivated (actions);
		
		if (oldBroadcastRedstone != broadcastRedstone) {		
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord,
					BuildCraftTransport.genericPipeBlock.blockID);
		}
		
		for (int i = 0; i < oldBroadcastSignal.length; ++i) {
			if (oldBroadcastSignal [i] != broadcastSignal [i]) {
				worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
				updateSignalState();
				break;
			}	
		}		
	}

	protected void actionsActivated(HashMap<Integer, Boolean> actions) {
		
	}

	@Override
	public TileGenericPipe getContainer() {
		return container;
	}

	@Override
	public boolean isWireConnectedTo(TileEntity tile, WireColor color) {
		if (!(tile instanceof TileGenericPipe)) {
			return false;
		}
		
		TileGenericPipe tilePipe = (TileGenericPipe) tile;
		
		if (!BlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
			return false;
		}
		
		if (!tilePipe.pipe.wireSet [color.ordinal()]) {
			return false;
		}
		
		return 
		(tilePipe.pipe.transport instanceof PipeTransportStructure 
				|| transport instanceof PipeTransportStructure
				|| Utils.checkPipesConnections(container, tile));
	}


	public void dropContents() {
		transport.dropContents ();
	}

	public void onDropped(EntityItem item) {
		
	}
	
	/**
	 * If this pipe is open on one side, return it.
	 */
	public Orientations getOpenOrientation() {	
		int Connections_num = 0;
		
		Orientations target_orientation = Orientations.Unknown;
		
		for (Orientations o : Orientations.dirs()) {						
			if (Utils.checkPipesConnections(container.getTile(o), container)) {

				Connections_num++;

				if (Connections_num == 1) {
					target_orientation = o;
				}
			}
		}
		
		if(Connections_num > 1 || Connections_num == 0) {
			return Orientations.Unknown;
		}
			
		return target_orientation.reverse();
	}


	@Override
	public boolean doDrop() {
		return logic.doDrop ();
	}
}
