/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.Action;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerDirectional;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import buildcraft.api.gates.TriggerParameter;
import buildcraft.api.transport.IPipe;
import buildcraft.core.IDropControlInventory;
import buildcraft.core.network.TilePacketWrapper;
import buildcraft.core.triggers.ActionRedstoneOutput;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Gate.GateConditional;
import buildcraft.transport.pipes.PipeLogic;
import buildcraft.transport.triggers.ActionSignalOutput;

public abstract class Pipe implements IPipe, IDropControlInventory {

	public int[] signalStrength = new int[] { 0, 0, 0, 0 };

	public int xCoord;
	public int yCoord;
	public int zCoord;
	public World worldObj;
	public TileGenericPipe container;

	public final PipeTransport transport;
	public final PipeLogic logic;
	public final int itemID;

	private boolean internalUpdateScheduled = false;

	public boolean[] wireSet = new boolean[] { false, false, false, false };

	public Gate gate;

	@SuppressWarnings("rawtypes")
	private static Map<Class, TilePacketWrapper> networkWrappers = new HashMap<Class, TilePacketWrapper>();

	public ITrigger[] activatedTriggers = new Trigger[8];
	public ITriggerParameter[] triggerParameters = new ITriggerParameter[8];
	public IAction[] activatedActions = new Action[8];

	public boolean broadcastSignal[] = new boolean[] { false, false, false, false };
	public boolean broadcastRedstone = false;

	public SafeTimeTracker actionTracker = new SafeTimeTracker();

	public Pipe(PipeTransport transport, PipeLogic logic, int itemID) {
		this.transport = transport;
		this.logic = logic;
		this.itemID = itemID;

		if (!networkWrappers.containsKey(this.getClass())) {
			networkWrappers
					.put(this.getClass(), new TilePacketWrapper(new Class[] { TileGenericPipe.class, this.transport.getClass(), this.logic.getClass() }));
		}

	}

	private void setPosition(int xCoord, int yCoord, int zCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;

		transport.setPosition(xCoord, yCoord, zCoord);
		logic.setPosition(xCoord, yCoord, zCoord);
	}

	private void setWorld(World worldObj) {
		if (worldObj != null && this.worldObj == null) {
			this.worldObj = worldObj;
			transport.setWorld(worldObj);
			logic.setWorld(worldObj);
		}
	}

	public void setTile(TileEntity tile) {

		this.container = (TileGenericPipe) tile;

		transport.setTile((TileGenericPipe) tile);
		logic.setTile((TileGenericPipe) tile);

		setPosition(tile.xCoord, tile.yCoord, tile.zCoord);
		setWorld(tile.worldObj);
	}

	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		return logic.blockActivated(entityplayer);
	}

	public void onBlockPlaced() {
		logic.onBlockPlaced();
		transport.onBlockPlaced();
	}
	
	public void onBlockPlacedBy(EntityLiving placer) {}

	public void onNeighborBlockChange(int blockId) {
		logic.onNeighborBlockChange(blockId);
		transport.onNeighborBlockChange(blockId);

		updateSignalState();
	}

	public boolean isPipeConnected(TileEntity tile, ForgeDirection side) {
		return logic.isPipeConnected(tile) && transport.isPipeConnected(tile, side);
	}

	/**
	 * Should return the texture file that is used to render this pipe
	 */
	public abstract String getTextureFile();

	/**
	 * Should return the textureindex in the file specified by getTextureFile()
	 * 
	 * @param direction
	 *            The orientation for the texture that is requested. Unknown for the center pipe center
	 * @return the index in the texture sheet
	 */
	public abstract int getTextureIndex(ForgeDirection direction);

	/**
	 * Should return the textureindex used by the Pipe Item Renderer, as this is done client-side the default implementation might not work if your
	 * getTextureIndex(Orienations.Unknown) has logic
	 * 
	 * @return
	 */
	public int getTextureIndexForItem() {
		return getTextureIndex(ForgeDirection.UNKNOWN);
	}

	public void updateEntity() {

		transport.updateEntity();
		logic.updateEntity();

		if (internalUpdateScheduled) {
			internalUpdate();
			internalUpdateScheduled = false;
		}

		// Do not try to update gates client side.
		if (worldObj.isRemote)
			return;

		if (actionTracker.markTimeIfDelay(worldObj, 10)) {
			resolveActions();
		}

		// Update the gate if we have any
		if (gate != null) {
			gate.update();
		}

	}

	private void internalUpdate() {
		updateSignalState();
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		transport.writeToNBT(nbttagcompound);
		logic.writeToNBT(nbttagcompound);

		// Save pulser if any
		if (gate != null) {
			NBTTagCompound nbttagcompoundC = new NBTTagCompound();
			gate.writeToNBT(nbttagcompoundC);
			nbttagcompound.setTag("Gate", nbttagcompoundC);
		}

		for (int i = 0; i < 4; ++i) {
			nbttagcompound.setBoolean("wireSet[" + i + "]", wireSet[i]);
		}

		for (int i = 0; i < 8; ++i) {
			nbttagcompound.setInteger("action[" + i + "]", activatedActions[i] != null ? activatedActions[i].getId() : 0);
			nbttagcompound.setInteger("trigger[" + i + "]", activatedTriggers[i] != null ? activatedTriggers[i].getId() : 0);
		}

		for (int i = 0; i < 8; ++i)
			if (triggerParameters[i] != null) {
				NBTTagCompound cpt = new NBTTagCompound();
				triggerParameters[i].writeToNBT(cpt);
				nbttagcompound.setTag("triggerParameters[" + i + "]", cpt);
			}
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		transport.readFromNBT(nbttagcompound);
		logic.readFromNBT(nbttagcompound);

		// Load pulser if any
		if (nbttagcompound.hasKey("Gate")) {
			NBTTagCompound nbttagcompoundP = nbttagcompound.getCompoundTag("Gate");
			gate = new GateVanilla(this);
			gate.readFromNBT(nbttagcompoundP);
		} else if (nbttagcompound.hasKey("gateKind")) {
			// Legacy implementation
			Gate.GateKind kind = Gate.GateKind.values()[nbttagcompound.getInteger("gateKind")];
			if (kind != Gate.GateKind.None) {
				gate = new GateVanilla(this);
				gate.kind = kind;
			}
		}

		for (int i = 0; i < 4; ++i) {
			wireSet[i] = nbttagcompound.getBoolean("wireSet[" + i + "]");
		}

		for (int i = 0; i < 8; ++i) {
			activatedActions[i] = ActionManager.actions[nbttagcompound.getInteger("action[" + i + "]")];
			activatedTriggers[i] = ActionManager.triggers[nbttagcompound.getInteger("trigger[" + i + "]")];
		}

		for (int i = 0; i < 8; ++i)
			if (nbttagcompound.hasKey("triggerParameters[" + i + "]")) {
				triggerParameters[i] = new TriggerParameter();
				triggerParameters[i].readFromNBT(nbttagcompound.getCompoundTag("triggerParameters[" + i + "]"));
			}
	}

	private boolean initialized = false;

	public void initialize() {
		if (!initialized) {
			transport.initialize();
			logic.initialize();
			initialized = true;
			updateSignalState();
		}
	}

	private void readNearbyPipesSignal(WireColor color) {
		boolean foundBiggerSignal = false;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = container.getTile(o);

			if (tile instanceof TileGenericPipe) {
				TileGenericPipe tilePipe = (TileGenericPipe) tile;

				if (BlockGenericPipe.isFullyDefined(tilePipe.pipe))
					if (isWireConnectedTo(tile, color)) {
						foundBiggerSignal |= receiveSignal(tilePipe.pipe.signalStrength[color.ordinal()] - 1, color);
					}
			}
		}

		if (!foundBiggerSignal && signalStrength[color.ordinal()] != 0) {
			signalStrength[color.ordinal()] = 0;
			// worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
			container.scheduleRenderUpdate();

			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
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
		if (!wireSet[color.ordinal()])
			return;

		// STEP 1: compute internal signal strength

		if (broadcastSignal[color.ordinal()]) {
			receiveSignal(255, color);
		} else {
			readNearbyPipesSignal(color);
		}

		// STEP 2: transmit signal in nearby blocks

		if (signalStrength[color.ordinal()] > 1) {
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = container.getTile(o);

				if (tile instanceof TileGenericPipe) {
					TileGenericPipe tilePipe = (TileGenericPipe) tile;

					if (BlockGenericPipe.isFullyDefined(tilePipe.pipe) && tilePipe.pipe.wireSet[color.ordinal()])
						if (isWireConnectedTo(tile, color)) {
							tilePipe.pipe.receiveSignal(signalStrength[color.ordinal()] - 1, color);
						}
				}
			}
		}
	}

	private boolean receiveSignal(int signal, IPipe.WireColor color) {
		if (worldObj == null)
			return false;

		int oldSignal = signalStrength[color.ordinal()];

		if (signal >= signalStrength[color.ordinal()] && signal != 0) {
			signalStrength[color.ordinal()] = signal;
			internalUpdateScheduled = true;

			if (oldSignal == 0) {
				// worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
				container.scheduleRenderUpdate();

			}

			return true;
		} else
			return false;
	}

	public boolean inputOpen(ForgeDirection from) {
		return transport.inputOpen(from) && logic.inputOpen(from);
	}

	public boolean outputOpen(ForgeDirection to) {
		return transport.outputOpen(to) && logic.outputOpen(to);
	}

	public void onEntityCollidedWithBlock(Entity entity) {

	}

	public boolean canConnectRedstone() {
		if (hasGate())
			return true;

		return false;
	}

	public boolean isPoweringTo(int l) {
		if (!broadcastRedstone)
			return false;

		ForgeDirection o = ForgeDirection.values()[l].getOpposite();
		TileEntity tile = container.getTile(o);

		if (tile instanceof TileGenericPipe && Utils.checkPipesConnections(this.container, tile))
			return false;

		return true;
	}

	public boolean isIndirectlyPoweringTo(int l) {
		return isPoweringTo(l);
	}

	public void randomDisplayTick(Random random) {
	}

	// / @Override TODO: should be in IPipe
	public boolean isWired() {
		for (WireColor color : WireColor.values())
			if (isWired(color))
				return true;

		return false;
	}

	@Override
	public boolean isWired(WireColor color) {
		return wireSet[color.ordinal()];
	}

	@Override
	public boolean hasInterface() {
		return hasGate();
	}

	public boolean hasGate() {
		return gate != null;
	}

	protected void updateNeighbors(boolean needSelf) {
		if (needSelf) {
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, BuildCraftTransport.genericPipeBlock.blockID);
		}
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord - 1, zCoord, BuildCraftTransport.genericPipeBlock.blockID);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord + 1, zCoord, BuildCraftTransport.genericPipeBlock.blockID);
		worldObj.notifyBlocksOfNeighborChange(xCoord - 1, yCoord, zCoord, BuildCraftTransport.genericPipeBlock.blockID);
		worldObj.notifyBlocksOfNeighborChange(xCoord + 1, yCoord, zCoord, BuildCraftTransport.genericPipeBlock.blockID);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord - 1, BuildCraftTransport.genericPipeBlock.blockID);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord + 1, BuildCraftTransport.genericPipeBlock.blockID);
	}

	public void onBlockRemoval() {
		if (wireSet[IPipe.WireColor.Red.ordinal()]) {
			Utils.dropItems(worldObj, new ItemStack(BuildCraftTransport.redPipeWire), xCoord, yCoord, zCoord);
		}

		if (wireSet[IPipe.WireColor.Blue.ordinal()]) {
			Utils.dropItems(worldObj, new ItemStack(BuildCraftTransport.bluePipeWire), xCoord, yCoord, zCoord);
		}

		if (wireSet[IPipe.WireColor.Green.ordinal()]) {
			Utils.dropItems(worldObj, new ItemStack(BuildCraftTransport.greenPipeWire), xCoord, yCoord, zCoord);
		}

		if (wireSet[IPipe.WireColor.Yellow.ordinal()]) {
			Utils.dropItems(worldObj, new ItemStack(BuildCraftTransport.yellowPipeWire), xCoord, yCoord, zCoord);
		}

		if (hasGate()) {
			gate.dropGate(worldObj, xCoord, yCoord, zCoord);
		}

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (container.hasFacade(direction)) {
				container.dropFacade(direction);
			}
		}

		if (broadcastRedstone) {
			updateNeighbors(false); // self will update due to block id changing
		}
	}

	public void setTrigger(int position, ITrigger trigger) {
		activatedTriggers[position] = trigger;
	}

	public ITrigger getTrigger(int position) {
		return activatedTriggers[position];
	}

	public void setTriggerParameter(int position, ITriggerParameter p) {
		triggerParameters[position] = p;
	}

	public ITriggerParameter getTriggerParameter(int position) {
		return triggerParameters[position];
	}

	public boolean isNearbyTriggerActive(ITrigger trigger, ITriggerParameter parameter) {
		if (trigger instanceof ITriggerPipe)
			return ((ITriggerPipe) trigger).isTriggerActive(this, parameter);
		else if (trigger != null) {
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = container.getTile(o);

				if (tile != null && !(tile instanceof TileGenericPipe)) {
					if (trigger instanceof ITriggerDirectional) {
						if (((ITriggerDirectional) trigger).isTriggerActive(o.getOpposite(), tile, parameter))
							return true;
					} else if (trigger.isTriggerActive(tile, parameter))
						return true;
				}
			}
		}

		return false;
	}

	public boolean isTriggerActive(ITrigger trigger) {
		return false;
	}

	public LinkedList<IAction> getActions() {
		LinkedList<IAction> result = new LinkedList<IAction>();

		if (hasGate()) {
			gate.addActions(result);
		}

		return result;
	}

	public IAction getAction(int position) {
		return activatedActions[position];
	}

	public void setAction(int position, IAction action) {
		activatedActions[position] = action;
	}

	public void resetGate() {
		gate = null;
		activatedTriggers = new Trigger[activatedTriggers.length];
		triggerParameters = new ITriggerParameter[triggerParameters.length];
		activatedActions = new Action[activatedActions.length];
		broadcastSignal = new boolean[] { false, false, false, false };
		if (broadcastRedstone) {
			updateNeighbors(true);
		}
		broadcastRedstone = false;
		// worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		container.scheduleRenderUpdate();
	}

	private void resolveActions() {
		if (!hasGate())
			return;

		boolean oldBroadcastRedstone = broadcastRedstone;
		boolean[] oldBroadcastSignal = broadcastSignal;

		broadcastRedstone = false;
		broadcastSignal = new boolean[] { false, false, false, false };

		// Tell the gate to prepare for resolving actions. (Disable pulser)
		gate.startResolution();

		HashMap<Integer, Boolean> actions = new HashMap<Integer, Boolean>();

		// Computes the actions depending on the triggers
		for (int it = 0; it < 8; ++it) {
			ITrigger trigger = activatedTriggers[it];
			IAction action = activatedActions[it];
			ITriggerParameter parameter = triggerParameters[it];

			if (trigger != null && action != null)
				if (!actions.containsKey(action.getId())) {
					actions.put(action.getId(), isNearbyTriggerActive(trigger, parameter));
				} else if (gate.getConditional() == GateConditional.AND) {
					actions.put(action.getId(), actions.get(action.getId()) && isNearbyTriggerActive(trigger, parameter));
				} else {
					actions.put(action.getId(), actions.get(action.getId()) || isNearbyTriggerActive(trigger, parameter));
				}
		}

		// Activate the actions
		for (Integer i : actions.keySet())
			if (actions.get(i)) {

				// Custom gate actions take precedence over defaults.
				if (gate.resolveAction(ActionManager.actions[i])) {
					continue;
				}

				if (ActionManager.actions[i] instanceof ActionRedstoneOutput) {
					broadcastRedstone = true;
				} else if (ActionManager.actions[i] instanceof ActionSignalOutput) {
					broadcastSignal[((ActionSignalOutput) ActionManager.actions[i]).color.ordinal()] = true;
				} else {
					for (int a = 0; a < container.tileBuffer.length; ++a)
						if (container.tileBuffer[a].getTile() instanceof IActionReceptor) {
							IActionReceptor recept = (IActionReceptor) container.tileBuffer[a].getTile();
							recept.actionActivated(ActionManager.actions[i]);
						}
				}
			}

		actionsActivated(actions);

		if (oldBroadcastRedstone != broadcastRedstone) {
			container.scheduleRenderUpdate();
			updateNeighbors(true);
		}

		for (int i = 0; i < oldBroadcastSignal.length; ++i)
			if (oldBroadcastSignal[i] != broadcastSignal[i]) {
				// worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
				container.scheduleRenderUpdate();
				updateSignalState();
				break;
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
		if (!(tile instanceof TileGenericPipe))
			return false;

		TileGenericPipe tilePipe = (TileGenericPipe) tile;

		if (!BlockGenericPipe.isFullyDefined(tilePipe.pipe))
			return false;

		if (!tilePipe.pipe.wireSet[color.ordinal()])
			return false;

		return (tilePipe.pipe.transport instanceof PipeTransportStructure || transport instanceof PipeTransportStructure || Utils.checkPipesConnections(
				container, tile));
	}

	public void dropContents() {
		transport.dropContents();
	}

	public void onDropped(EntityItem item) {

	}

	/**
	 * If this pipe is open on one side, return it.
	 */
	public ForgeDirection getOpenOrientation() {
		int Connections_num = 0;

		ForgeDirection target_orientation = ForgeDirection.UNKNOWN;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS)
			if (Utils.checkPipesConnections(container.getTile(o), container)) {

				Connections_num++;

				if (Connections_num == 1) {
					target_orientation = o;
				}
			}

		if (Connections_num > 1 || Connections_num == 0)
			return ForgeDirection.UNKNOWN;

		return target_orientation.getOpposite();
	}

	@Override
	public boolean doDrop() {
		return logic.doDrop();
	}

	public boolean isGateActive() {
		for (boolean b : broadcastSignal) {
			if (b)
				return true;
		}
		return broadcastRedstone;
	}

	/**
	 * Called when TileGenericPipe.invalidate() is called
	 */
	public void invalidate() {
	}

	/**
	 * Called when TileGenericPipe.validate() is called
	 */
	public void validate() {
	}

	/**
	 * Called when TileGenericPipe.onChunkUnload is called
	 */
	public void onChunkUnload() {
	}

}
