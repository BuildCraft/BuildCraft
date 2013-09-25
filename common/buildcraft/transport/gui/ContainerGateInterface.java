/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import buildcraft.api.core.Position;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadArrays;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import java.util.Iterator;
import java.util.LinkedList;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public class ContainerGateInterface extends BuildCraftContainer {

	IInventory playerIInventory;
	Pipe pipe;
	private final LinkedList<ITrigger> _potentialTriggers = new LinkedList<ITrigger>();
	private final LinkedList<IAction> _potentialActions = new LinkedList<IAction>();
	private boolean isSynchronized = false;
	private boolean isNetInitialized = false;
	public boolean[] triggerState = new boolean[8];
	private int lastTriggerState = 0;

	public ContainerGateInterface(IInventory playerInventory, Pipe pipe) {
		super(0);
		this.playerIInventory = playerInventory;

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 123 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 181));
		}

		this.pipe = pipe;

		// Do not attempt to create a list of potential actions and triggers on
		// the client.
		if (!CoreProxy.proxy.isRenderWorld(pipe.container.worldObj)) {
			_potentialActions.addAll(pipe.getActions());
			_potentialTriggers.addAll(ActionManager.getPipeTriggers(pipe));

			if (pipe.container instanceof IOverrideDefaultTriggers) {
				_potentialTriggers.addAll(((IOverrideDefaultTriggers) pipe.container).getTriggers());
			}

			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = pipe.container.getTile(o);
				int blockID = pipe.container.getBlockId(o);
				Block block = Block.blocksList[blockID];

				LinkedList<ITrigger> nearbyTriggers = ActionManager.getNeighborTriggers(block, tile);

				for (ITrigger t : nearbyTriggers) {
					if (!_potentialTriggers.contains(t)) {
						_potentialTriggers.add(t);
					}
				}

				LinkedList<IAction> nearbyActions = ActionManager.getNeighborActions(block, tile);

				for (IAction a : nearbyActions) {
					if (!_potentialActions.contains(a)) {
						_potentialActions.add(a);
					}
				}
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	/**
	 * CLIENT SIDE *
	 */
	/**
	 * Marks client side gate container as needing to be synchronized with the
	 * server.
	 */
	public void markDirty() {
		isSynchronized = false;
	}

	/**
	 * Clears list of potential actions and refills it according to packet.
	 *
	 * @param packet
	 */
	public void updateActions(PacketUpdate packet) {
		_potentialActions.clear();
		PacketPayloadArrays payload = (PacketPayloadArrays) packet.payload;
		int length = payload.intPayload[0];

		for (int i = 0; i < length; i++) {
			_potentialActions.add(ActionManager.actions.get(payload.stringPayload[i]));
		}
	}

	/**
	 * Clears list of potential triggers and refills it according to packet.
	 *
	 * @param packet
	 */
	public void updateTriggers(PacketUpdate packet) {
		_potentialTriggers.clear();
		PacketPayloadArrays payload = (PacketPayloadArrays) packet.payload;
		int length = payload.intPayload[0];

		for (int i = 0; i < length; i++) {
			_potentialTriggers.add(ActionManager.triggers.get(payload.stringPayload[i]));
		}
	}

	/**
	 * Sets the currently selected actions and triggers according to packet.
	 *
	 * @param packet
	 */
	public void setSelection(PacketUpdate packet, boolean notify) {
		PacketPayloadArrays payload = (PacketPayloadArrays) packet.payload;
		int position = payload.intPayload[0];

		setTrigger(position, ActionManager.triggers.get(payload.stringPayload[0]), notify);
		setAction(position, ActionManager.actions.get(payload.stringPayload[1]), notify);

		int itemID = payload.intPayload[1];
		if (itemID <= 0) {
			setTriggerParameter(position, null, notify);
			return;
		}

		ITriggerParameter param = new TriggerParameter();
		param.set(new ItemStack(itemID, payload.intPayload[2], payload.intPayload[3]));
		setTriggerParameter(position, param, notify);
	}

	private PacketPayload getSelectionPayload(int position) {
		PacketPayloadArrays payload = new PacketPayloadArrays(4, 0, 2);

		payload.intPayload[0] = position;

		if (pipe.gate.triggers[position] != null) {
			payload.stringPayload[0] = pipe.gate.triggers[position].getUniqueTag();
		} else {
			payload.stringPayload[0] = "";
		}

		if (pipe.gate.actions[position] != null) {
			payload.stringPayload[1] = pipe.gate.actions[position].getUniqueTag();
		} else {
			payload.stringPayload[1] = "";
		}

		if (pipe.gate.triggerParameters[position] != null && pipe.gate.triggerParameters[position].getItemStack() != null) {
			payload.intPayload[1] = pipe.gate.triggerParameters[position].getItemStack().itemID;
			payload.intPayload[2] = pipe.gate.triggerParameters[position].getItemStack().stackSize;
			payload.intPayload[3] = pipe.gate.triggerParameters[position].getItemStack().getItemDamage();
		}

		return payload;
	}

	public void sendSelectionChange(int position) {
		CoreProxy.proxy.sendToServer(new PacketUpdate(PacketIds.GATE_SELECTION_CHANGE, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, getSelectionPayload(position)).getPacket());
	}

	/**
	 * Initializes the list of triggers and actions on the gate and
	 * (re-)requests the current selection on the gate if needed.
	 */
	public void synchronize() {

		if (!isNetInitialized && CoreProxy.proxy.isRenderWorld(pipe.container.worldObj)) {
			isNetInitialized = true;
			CoreProxy.proxy.sendToServer(new PacketCoordinates(PacketIds.GATE_REQUEST_INIT, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord).getPacket());
		}

		if (!isSynchronized && CoreProxy.proxy.isRenderWorld(pipe.container.worldObj)) {
			isSynchronized = true;
			CoreProxy.proxy.sendToServer(new PacketCoordinates(PacketIds.GATE_REQUEST_SELECTION, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord).getPacket());
		}
	}

	@Override
	public void updateProgressBar(int id, int state) {
		if (id == 0 /* Trigger state update */) {
			for (int i = 0; i < 8; i++) {
				/* Bit mask of triggers */
				triggerState[i] = ((state >> i) & 0x01) == 0x01;
			}
		}
	}

	/**
	 * SERVER SIDE *
	 */
	private int calculateTriggerState() {
		if (pipe.gate == null)
			return 0;
		int state = 0;
		for (int i = 0; i < triggerState.length; i++) {
			if (pipe.gate.triggers[i] != null) {
				triggerState[i] = isNearbyTriggerActive(pipe.gate.triggers[i], pipe.gate.getTriggerParameter(i));
			}
			state |= triggerState[i] ? 0x01 << i : 0x0;
		}
		return state;
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		int state = calculateTriggerState();
		if (state != lastTriggerState) {
			for (int i = 0; i < this.crafters.size(); i++) {
				ICrafting viewingPlayer = (ICrafting) this.crafters.get(i);

				viewingPlayer.sendProgressBarUpdate(this, 0 /* State update */, state);
			}
			lastTriggerState = state;
		}
	}

	public void handleInitRequest(EntityPlayer player) {
		sendActions(player);
		sendTriggers(player);
		sendSelection(player);
	}

	/**
	 * Sends the list of potential actions to the client
	 *
	 * @param player
	 */
	private void sendActions(EntityPlayer player) {

		// Compose update packet
		int length = _potentialActions.size();
		PacketPayloadArrays payload = new PacketPayloadArrays(1, 0, length);

		payload.intPayload[0] = length;
		for (int i = 0; i < length; i++) {
			payload.stringPayload[i] = _potentialActions.get(i).getUniqueTag();
		}

		PacketUpdate packet = new PacketUpdate(PacketIds.GATE_ACTIONS, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, payload);

		// Send to player
		CoreProxy.proxy.sendToPlayer(player, packet);
	}

	/**
	 * Sends the list of potential triggers to the client
	 *
	 * @param player
	 */
	private void sendTriggers(EntityPlayer player) {

		// Compose update packet
		int length = _potentialTriggers.size();
		PacketPayloadArrays payload = new PacketPayloadArrays(1, 0, length);

		payload.intPayload[0] = length;
		for (int i = 0; i < length; i++) {
			payload.stringPayload[i] = _potentialTriggers.get(i).getUniqueTag();
		}

		PacketUpdate packet = new PacketUpdate(PacketIds.GATE_TRIGGERS, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, payload);

		// Send to player
		CoreProxy.proxy.sendToPlayer(player, packet);
	}

	/**
	 * Sends the current selection on the gate to the client.
	 *
	 * @param player
	 */
	public void sendSelection(EntityPlayer player) {
		if (pipe == null || pipe.gate == null)
			return;
		int positions = 0;
		switch (pipe.gate.kind) {
			case Single:
				positions = 1;
				break;
			case AND_2:
			case OR_2:
				positions = 2;
				break;
			case AND_3:
			case OR_3:
				positions = 4;
				break;
			case OR_4:
			case AND_4:
				positions = 8;
				break;
			case OR_5:
			case AND_5:
			default:
				positions = 16;
				break;
		}

		for (int position = 0; position < positions; position++) {
			CoreProxy.proxy.sendToPlayer(player, new PacketUpdate(PacketIds.GATE_SELECTION, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, getSelectionPayload(position)));
		}

		// System.out.println("Sending current selection to player");
	}

	/**
	 * TRIGGERS *
	 */
	public boolean hasTriggers() {
		return _potentialTriggers.size() > 0;
	}

	public ITrigger getFirstTrigger() {
		return _potentialTriggers.size() > 0 ? _potentialTriggers.getFirst() : null;
	}

	public ITrigger getLastTrigger() {
		return _potentialTriggers.size() > 0 ? _potentialTriggers.getLast() : null;
	}

	public Iterator<ITrigger> getTriggerIterator(boolean descending) {
		return descending ? _potentialTriggers.descendingIterator() : _potentialTriggers.iterator();
	}

	public boolean isNearbyTriggerActive(ITrigger trigger, ITriggerParameter parameter) {
		return pipe.gate.isNearbyTriggerActive(trigger, parameter);
	}

	public void setTrigger(int position, ITrigger trigger, boolean notify) {
		pipe.gate.setTrigger(position, trigger);
		if (CoreProxy.proxy.isRenderWorld(pipe.container.worldObj) && notify) {
			sendSelectionChange(position);
		}
	}

	public void setTriggerParameter(int position, ITriggerParameter parameter, boolean notify) {
		pipe.gate.setTriggerParameter(position, parameter);
		if (CoreProxy.proxy.isRenderWorld(pipe.container.worldObj) && notify) {
			sendSelectionChange(position);
		}
	}

	/**
	 * ACTIONS *
	 */
	public boolean hasActions() {
		return !_potentialActions.isEmpty();
	}

	public IAction getFirstAction() {
		return _potentialActions.peekFirst();
	}

	public IAction getLastAction() {
		return _potentialActions.peekLast();
	}

	public Iterator<IAction> getActionIterator(boolean descending) {
		return descending ? _potentialActions.descendingIterator() : _potentialActions.iterator();
	}

	public void setAction(int position, IAction action, boolean notify) {
		pipe.gate.setAction(position, action);
		if (CoreProxy.proxy.isRenderWorld(pipe.container.worldObj) && notify) {
			sendSelectionChange(position);
		}
	}

	/**
	 * GATE INFORMATION *
	 */
	public ResourceLocation getGateGuiFile() {
		return pipe.gate.getGuiFile();
	}

	public int getGateOrdinal() {
		return pipe.gate.kind.ordinal();
	}

	public String getGateName() {
		return pipe.gate.getName();
	}
}
