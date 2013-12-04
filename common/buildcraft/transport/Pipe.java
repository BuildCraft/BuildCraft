/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.TriggerParameter;
import buildcraft.api.transport.IPipe;
import buildcraft.core.IDropControlInventory;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.network.TilePacketWrapper;
import buildcraft.core.utils.Utils;
import buildcraft.transport.pipes.events.PipeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public abstract class Pipe<T extends PipeTransport> implements IPipe, IDropControlInventory {

	public int[] signalStrength = new int[]{0, 0, 0, 0};
	public TileGenericPipe container;
	public final T transport;
	public final int itemID;
	private boolean internalUpdateScheduled = false;
	public boolean[] wireSet = new boolean[]{false, false, false, false};
	public Gate gate;
	@SuppressWarnings("rawtypes")
	private static Map<Class, TilePacketWrapper> networkWrappers = new HashMap<Class, TilePacketWrapper>();
	public SafeTimeTracker actionTracker = new SafeTimeTracker();
	private static Map<Class<? extends Pipe>, Map<Class<? extends PipeEvent>, EventHandler>> eventHandlers = new HashMap<Class<? extends Pipe>, Map<Class<? extends PipeEvent>, EventHandler>>();

	public Pipe(T transport, int itemID) {
		this.transport = transport;
		this.itemID = itemID;

		if (!networkWrappers.containsKey(this.getClass())) {
			networkWrappers
					.put(this.getClass(), new TilePacketWrapper(new Class[]{TileGenericPipe.class, this.transport.getClass()}));
		}
	}

	public void setTile(TileEntity tile) {

		this.container = (TileGenericPipe) tile;

		transport.setTile((TileGenericPipe) tile);
	}

//	public final void handlePipeEvent(PipeEvent event) {
//		try {
//			Method method = getClass().getDeclaredMethod("eventHandler", event.getClass());
//			method.invoke(this, event);
//		} catch (Exception ex) {
//		}
//	}
	private static class EventHandler {

		public final Method method;

		public EventHandler(Method method) {
			this.method = method;
		}
	}

	public final void handlePipeEvent(PipeEvent event) {
		Map<Class<? extends PipeEvent>, EventHandler> handlerMap = eventHandlers.get(getClass());
		if (handlerMap == null) {
			handlerMap = new HashMap<Class<? extends PipeEvent>, EventHandler>();
			eventHandlers.put(getClass(), handlerMap);
		}
		EventHandler handler = handlerMap.get(event.getClass());
		if (handler == null)
			handler = makeEventHandler(event, handlerMap);
		if (handler.method == null)
			return;
		try {
			handler.method.invoke(this, event);
		} catch (Exception ex) {
		}
	}

	private EventHandler makeEventHandler(PipeEvent event, Map<Class<? extends PipeEvent>, EventHandler> handlerMap) {
		EventHandler handler;
		try {
			Method method = getClass().getDeclaredMethod("eventHandler", event.getClass());
			handler = new EventHandler(method);
		} catch (Exception ex) {
			handler = new EventHandler(null);
		}
		handlerMap.put(event.getClass(), handler);
		return handler;
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		return false;
	}

	public void onBlockPlaced() {
		transport.onBlockPlaced();
	}

	public void onBlockPlacedBy(EntityLivingBase placer) {
	}

	public void onNeighborBlockChange(int blockId) {
		transport.onNeighborBlockChange(blockId);

		updateSignalState();
	}

	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		Pipe otherPipe;
		if (tile instanceof TileGenericPipe) {
			otherPipe = ((TileGenericPipe) tile).pipe;

			if (!BlockGenericPipe.isFullyDefined(otherPipe))
				return false;

			if (!PipeConnectionBans.canPipesConnect(getClass(), otherPipe.getClass()))
				return false;
		}
		return transport.canPipeConnect(tile, side);
	}

	/**
	 * Should return the textureindex used by the Pipe Item Renderer, as this is
	 * done client-side the default implementation might not work if your
	 * getTextureIndex(Orienations.Unknown) has logic. Then override this
	 *
	 * @return
	 */
	public int getIconIndexForItem() {
		return getIconIndex(ForgeDirection.UNKNOWN);
	}

	/**
	 * Should return the IIconProvider that provides icons for this pipe
	 *
	 * @return An array of icons
	 */
	@SideOnly(Side.CLIENT)
	public abstract IIconProvider getIconProvider();

	/**
	 * Should return the index in the array returned by GetTextureIcons() for a
	 * specified direction
	 *
	 * @param direction - The direction for which the indexed should be
	 * rendered. Unknown for pipe center
	 *
	 * @return An index valid in the array returned by getTextureIcons()
	 */
	public abstract int getIconIndex(ForgeDirection direction);

	public void updateEntity() {

		transport.updateEntity();

		if (internalUpdateScheduled) {
			internalUpdate();
			internalUpdateScheduled = false;
		}

		// Do not try to update gates client side.
		if (container.worldObj.isRemote)
			return;

		// Update the gate if we have any
		if (gate != null) {
			gate.resolveActions();
			gate.update();
		}
	}

	private void internalUpdate() {
		updateSignalState();
	}

	public void writeToNBT(NBTTagCompound data) {
		transport.writeToNBT(data);

		// Save pulser if any
		if (gate != null) {
			NBTTagCompound gateNBT = new NBTTagCompound();
			gate.writeToNBT(gateNBT);
			data.setTag("Gate", gateNBT);
		}

		for (int i = 0; i < 4; ++i) {
			data.setBoolean("wireSet[" + i + "]", wireSet[i]);
		}
	}

	public void readFromNBT(NBTTagCompound data) {
		transport.readFromNBT(data);

		// Load pulser if any
		if (data.hasKey("Gate")) {
			NBTTagCompound gateNBT = data.getCompoundTag("Gate");
			gate = Gate.makeGate(this, gateNBT);
		} else if (data.hasKey("gateKind")) {
			// Legacy implementation
			Gate.GateKind kind = Gate.GateKind.values()[data.getInteger("gateKind")];
			if (kind != Gate.GateKind.None) {
				gate = new GateVanilla(this);
				gate.kind = kind;
			}
		}

		for (int i = 0; i < 4; ++i) {
			wireSet[i] = data.getBoolean("wireSet[" + i + "]");
		}

		// Legacy update code
		if (hasGate()) {
			for (int i = 0; i < 8; ++i) {
				if (data.hasKey("trigger[" + i + "]"))
					gate.triggers[i] = ActionManager.getTriggerFromLegacyId(data.getInteger("trigger[" + i + "]"));
				if (data.hasKey("action[" + i + "]"))
					gate.actions[i] = ActionManager.getActionFromLegacyId(data.getInteger("action[" + i + "]"));
				if (data.hasKey("triggerParameters[" + i + "]")) {
					gate.triggerParameters[i] = new TriggerParameter();
					gate.triggerParameters[i].readFromNBT(data.getCompoundTag("triggerParameters[" + i + "]"));
				}
			}
		}
	}
	private boolean initialized = false;

	public boolean needsInit() {
		return !initialized;
	}

	public void initialize() {
		transport.initialize();
		updateSignalState();
		initialized = true;
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
			// worldObj.markBlockNeedsUpdate(container.xCoord, container.yCoord, zCoord);
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

	public void updateSignalState() {
		for (IPipe.WireColor c : IPipe.WireColor.values()) {
			updateSignalStateForColor(c);
		}
	}

	private void updateSignalStateForColor(IPipe.WireColor color) {
		if (!wireSet[color.ordinal()])
			return;

		// STEP 1: compute internal signal strength

		if (gate != null && gate.broadcastSignal[color.ordinal()]) {
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
		if (container.worldObj == null)
			return false;

		int oldSignal = signalStrength[color.ordinal()];

		if (signal >= signalStrength[color.ordinal()] && signal != 0) {
			signalStrength[color.ordinal()] = signal;
			internalUpdateScheduled = true;

			if (oldSignal == 0) {
				container.scheduleRenderUpdate();
			}

			return true;
		} else
			return false;
	}

	public boolean inputOpen(ForgeDirection from) {
		return transport.inputOpen(from);
	}

	public boolean outputOpen(ForgeDirection to) {
		return transport.outputOpen(to);
	}

	public void onEntityCollidedWithBlock(Entity entity) {
	}

	public boolean canConnectRedstone() {
		if (hasGate())
			return true;

		return false;
	}

	public int isPoweringTo(int side) {
		if (gate != null && gate.isEmittingRedstone()) {
			ForgeDirection o = ForgeDirection.getOrientation(side).getOpposite();
			TileEntity tile = container.getTile(o);

			if (tile instanceof TileGenericPipe && container.isPipeConnected(o))
				return 0;

			return 15;
		}
		return 0;
	}

	public int isIndirectlyPoweringTo(int l) {
		return isPoweringTo(l);
	}

	public void randomDisplayTick(Random random) {
	}

	// / @Override TODO: should be in IPipe
	public boolean isWired() {
		for (WireColor color : WireColor.values()) {
			if (isWired(color))
				return true;
		}

		return false;
	}

	@Override
	public boolean isWired(WireColor color) {
		return wireSet[color.ordinal()];
	}

	@Override
	public boolean hasGate() {
		return gate != null;
	}

	public boolean hasGate(ForgeDirection side) {
		if (!hasGate())
			return false;
		if (container.hasFacade(side))
			return false;
		if (container.hasPlug(side))
			return false;

		int connections = 0;
		ForgeDirection targetOrientation = ForgeDirection.UNKNOWN;
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (container.isPipeConnected(o)) {
				connections++;
				if (connections == 1)
					targetOrientation = o;
			}
		}

		if (connections > 1 || connections == 0)
			return true;

		return targetOrientation.getOpposite() != side;
	}

	protected void notifyBlocksOfNeighborChange(ForgeDirection side) {
		container.worldObj.notifyBlocksOfNeighborChange(container.xCoord + side.offsetX, container.yCoord + side.offsetY, container.zCoord + side.offsetZ, BuildCraftTransport.genericPipeBlock.blockID);
	}

	protected void updateNeighbors(boolean needSelf) {
		if (needSelf) {
			container.worldObj.notifyBlocksOfNeighborChange(container.xCoord, container.yCoord, container.zCoord, BuildCraftTransport.genericPipeBlock.blockID);
		}
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			notifyBlocksOfNeighborChange(side);
		}
	}

	public void dropItem(ItemStack stack) {
		InvUtils.dropItems(container.worldObj, stack, container.xCoord, container.yCoord, container.zCoord);
	}

	public void onBlockRemoval() {
		if (wireSet[IPipe.WireColor.Red.ordinal()]) {
			dropItem(new ItemStack(BuildCraftTransport.redPipeWire));
		}

		if (wireSet[IPipe.WireColor.Blue.ordinal()]) {
			dropItem(new ItemStack(BuildCraftTransport.bluePipeWire));
		}

		if (wireSet[IPipe.WireColor.Green.ordinal()]) {
			dropItem(new ItemStack(BuildCraftTransport.greenPipeWire));
		}

		if (wireSet[IPipe.WireColor.Yellow.ordinal()]) {
			dropItem(new ItemStack(BuildCraftTransport.yellowPipeWire));
		}

		if (hasGate()) {
			gate.dropGate();
			resetGate();
		}

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (container.hasFacade(direction)) {
				container.dropFacade(direction);
			}
			if (container.hasPlug(direction)) {
				container.removeAndDropPlug(direction);
			}
		}
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

	public void resetGate() {
		gate.resetGate();
		gate = null;
		container.scheduleRenderUpdate();
	}

	protected void actionsActivated(Map<IAction, Boolean> actions) {
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

	/**
	 * If this pipe is open on one side, return it.
	 */
	public ForgeDirection getOpenOrientation() {
		int Connections_num = 0;

		ForgeDirection target_orientation = ForgeDirection.UNKNOWN;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (container.isPipeConnected(o)) {

				Connections_num++;

				if (Connections_num == 1)
					target_orientation = o;
			}
		}

		if (Connections_num > 1 || Connections_num == 0)
			return ForgeDirection.UNKNOWN;

		return target_orientation.getOpposite();
	}

	@Override
	public boolean doDrop() {
		return true;
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

	public World getWorld() {
		return container.worldObj;
	}
}
