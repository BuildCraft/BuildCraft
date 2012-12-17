/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.gui;

import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
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
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;

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
		if (!CoreProxy.proxy.isRenderWorld(pipe.worldObj)) {
			_potentialActions.addAll(pipe.getActions());
			_potentialTriggers.addAll(ActionManager.getPipeTriggers(pipe));

			TileEntity ptile = pipe.worldObj.getBlockTileEntity(pipe.xCoord, pipe.yCoord, pipe.zCoord);
			if (ptile instanceof IOverrideDefaultTriggers) {
				_potentialTriggers.addAll(((IOverrideDefaultTriggers) ptile).getTriggers());
			}

			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				Position pos = new Position(pipe.xCoord, pipe.yCoord, pipe.zCoord, o);
				pos.moveForwards(1.0);
				TileEntity tile = pipe.worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
				int blockID = pipe.worldObj.getBlockId((int) pos.x, (int) pos.y, (int) pos.z);
				Block block = Block.blocksList[blockID];

				LinkedList<ITrigger> nearbyTriggers = ActionManager.getNeighborTriggers(block, tile);

				for (ITrigger t : nearbyTriggers)
					if (!_potentialTriggers.contains(t)) {
						_potentialTriggers.add(t);
					}

				LinkedList<IAction> nearbyActions = ActionManager.getNeighborActions(block, tile);

				for (IAction a : nearbyActions)
					if (!_potentialActions.contains(a)) {
						_potentialActions.add(a);
					}
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	/** CLIENT SIDE **/

	/**
	 * Marks client side gate container as needing to be synchronized with the server.
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
		int length = packet.payload.intPayload[0];

		for (int i = 0; i < length; i++) {
			_potentialActions.add(ActionManager.actions[packet.payload.intPayload[i + 1]]);
		}

	}

	/**
	 * Clears list of potential triggers and refills it according to packet.
	 * 
	 * @param packet
	 */
	public void updateTriggers(PacketUpdate packet) {
		_potentialTriggers.clear();
		int length = packet.payload.intPayload[0];

		for (int i = 0; i < length; i++) {
			_potentialTriggers.add(ActionManager.triggers[packet.payload.intPayload[i + 1]]);
		}
	}

	/**
	 * Sets the currently selected actions and triggers according to packet.
	 * 
	 * @param packet
	 */
	public void setSelection(PacketUpdate packet) {
		PacketPayload payload = packet.payload;
		int position = payload.intPayload[0];

		if (payload.intPayload[1] >= 0 && payload.intPayload[1] < ActionManager.triggers.length) {
			setTrigger(position, ActionManager.triggers[payload.intPayload[1]], false);
			// System.out.println("Trigger["+ position + "]: " +
			// pipe.activatedTriggers[position].getDescription());
		} else {
			setTrigger(position, null, false);
			// System.out.println("Trigger["+ position + "] clear!");
		}

		if (payload.intPayload[2] >= 0 && payload.intPayload[2] < ActionManager.actions.length) {
			setAction(position, ActionManager.actions[payload.intPayload[2]], false);
			// System.out.println("Action["+ position + "]: " +
			// pipe.activatedActions[position].getDescription());
		} else {
			setAction(position, null, false);
			// System.out.println("Action["+ position + "] clear!");
		}

		int itemID = payload.intPayload[3];
		if (itemID <= 0) {
			setTriggerParameter(position, null, false);
			return;
		}

		ITriggerParameter param = new TriggerParameter();
		param.set(new ItemStack(itemID, payload.intPayload[4], payload.intPayload[5]));
		setTriggerParameter(position, param, false);
	}

	public void sendSelectionChange(int position) {
		PacketPayload payload = new PacketPayload(6, 0, 0);

		payload.intPayload[0] = position;

		if (pipe.activatedTriggers[position] != null) {
			payload.intPayload[1] = pipe.activatedTriggers[position].getId();
		} else {
			payload.intPayload[1] = -1;
		}

		if (pipe.activatedActions[position] != null) {
			payload.intPayload[2] = pipe.activatedActions[position].getId();
		} else {
			payload.intPayload[2] = -1;
		}

		if (pipe.triggerParameters[position] != null && pipe.triggerParameters[position].getItemStack() != null) {
			payload.intPayload[3] = pipe.triggerParameters[position].getItemStack().itemID;
			payload.intPayload[4] = pipe.triggerParameters[position].getItemStack().stackSize;
			payload.intPayload[5] = pipe.triggerParameters[position].getItemStack().getItemDamage();
		}

		CoreProxy.proxy.sendToServer(new PacketUpdate(PacketIds.GATE_SELECTION_CHANGE, pipe.xCoord, pipe.yCoord, pipe.zCoord, payload).getPacket());
	}

	/**
	 * Initializes the list of triggers and actions on the gate and (re-)requests the current selection on the gate if needed.
	 */
	public void synchronize() {

		if (!isNetInitialized && CoreProxy.proxy.isRenderWorld(pipe.worldObj)) {
			isNetInitialized = true;
			CoreProxy.proxy.sendToServer(new PacketCoordinates(PacketIds.GATE_REQUEST_INIT, pipe.xCoord, pipe.yCoord, pipe.zCoord).getPacket());
		}

		if (!isSynchronized && CoreProxy.proxy.isRenderWorld(pipe.worldObj)) {
			isSynchronized = true;
			CoreProxy.proxy.sendToServer(new PacketCoordinates(PacketIds.GATE_REQUEST_SELECTION, pipe.xCoord, pipe.yCoord, pipe.zCoord).getPacket());
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

	/** SERVER SIDE **/
	private int calculateTriggerState() {
		int state = 0;
		for (int i = 0; i < triggerState.length; i++) {
			if (pipe.activatedTriggers[i] != null) {
				triggerState[i] = isNearbyTriggerActive(pipe.activatedTriggers[i], pipe.getTriggerParameter(i));
			}
			state |= triggerState[i] ? 0x01 << i : 0x0;
		}
		return state;
	}

	@Override
	public void updateCraftingResults() {
		super.updateCraftingResults();
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

	public void handleSelectionChange(PacketUpdate packet) {
		PacketPayload payload = packet.payload;
		int position = payload.intPayload[0];

		if (payload.intPayload[1] >= 0 && payload.intPayload[1] < ActionManager.triggers.length) {
			setTrigger(position, ActionManager.triggers[payload.intPayload[1]], true);
			// System.out.println("Trigger["+ position + "]: " +
			// pipe.activatedTriggers[position].getDescription());
		} else {
			setTrigger(position, null, true);
			// System.out.println("Trigger["+ position + "] clear!");
		}

		if (payload.intPayload[2] >= 0 && payload.intPayload[2] < ActionManager.actions.length) {
			setAction(position, ActionManager.actions[payload.intPayload[2]], true);
			// System.out.println("Action["+ position + "]: " +
			// pipe.activatedActions[position].getDescription());
		} else {
			setAction(position, null, true);
			// System.out.println("Action["+ position + "] clear!");
		}

		int itemID = payload.intPayload[3];
		if (itemID <= 0) {
			setTriggerParameter(position, null, true);
			return;
		}

		ITriggerParameter param = new TriggerParameter();
		param.set(new ItemStack(itemID, payload.intPayload[4], payload.intPayload[5]));
		setTriggerParameter(position, param, true);
	}

	/**
	 * Sends the list of potential actions to the client
	 * 
	 * @param player
	 */
	private void sendActions(EntityPlayer player) {

		// Compose update packet
		int length = _potentialActions.size();
		PacketPayload payload = new PacketPayload(length + 1, 0, 0);

		payload.intPayload[0] = length;
		for (int i = 0; i < length; i++) {
			payload.intPayload[i + 1] = _potentialActions.get(i).getId();
		}

		PacketUpdate packet = new PacketUpdate(PacketIds.GATE_ACTIONS, pipe.xCoord, pipe.yCoord, pipe.zCoord, payload);

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
		PacketPayload payload = new PacketPayload(length + 1, 0, 0);

		payload.intPayload[0] = length;
		for (int i = 0; i < length; i++) {
			payload.intPayload[i + 1] = _potentialTriggers.get(i).getId();
		}

		PacketUpdate packet = new PacketUpdate(PacketIds.GATE_TRIGGERS, pipe.xCoord, pipe.yCoord, pipe.zCoord, payload);

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
		default:
			positions = 8;
			break;
		}

		for (int position = 0; position < positions; position++) {
			PacketPayload payload = new PacketPayload(6, 0, 0);

			payload.intPayload[0] = position;

			if (pipe.activatedTriggers[position] != null) {
				payload.intPayload[1] = pipe.activatedTriggers[position].getId();
			} else {
				payload.intPayload[1] = -1;
			}

			if (pipe.activatedActions[position] != null) {
				payload.intPayload[2] = pipe.activatedActions[position].getId();
			} else {
				payload.intPayload[2] = -1;
			}

			if (pipe.triggerParameters[position] != null && pipe.triggerParameters[position].getItemStack() != null) {
				payload.intPayload[3] = pipe.triggerParameters[position].getItemStack().itemID;
				payload.intPayload[4] = pipe.triggerParameters[position].getItemStack().stackSize;
				payload.intPayload[5] = pipe.triggerParameters[position].getItemStack().getItemDamage();
			}

			CoreProxy.proxy.sendToPlayer(player, new PacketUpdate(PacketIds.GATE_SELECTION, pipe.xCoord, pipe.yCoord, pipe.zCoord, payload));
		}

		// System.out.println("Sending current selection to player");
	}

	/** TRIGGERS **/
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
		return pipe.isNearbyTriggerActive(trigger, parameter);
	}

	public void setTrigger(int position, ITrigger trigger, boolean notify) {
		pipe.setTrigger(position, trigger);
		if (CoreProxy.proxy.isRenderWorld(pipe.worldObj) && notify) {
			sendSelectionChange(position);
		}
	}

	public void setTriggerParameter(int position, ITriggerParameter parameter, boolean notify) {
		pipe.setTriggerParameter(position, parameter);
		if (CoreProxy.proxy.isRenderWorld(pipe.worldObj) && notify) {
			sendSelectionChange(position);
		}
	}

	/** ACTIONS **/
	public boolean hasActions() {
		return _potentialActions.size() > 0;
	}

	public IAction getFirstAction() {
		return _potentialActions.size() > 0 ? _potentialActions.getFirst() : null;
	}

	public IAction getLastAction() {
		return _potentialActions.size() > 0 ? _potentialActions.getLast() : null;
	}

	public Iterator<IAction> getActionIterator(boolean descending) {
		return descending ? _potentialActions.descendingIterator() : _potentialActions.iterator();
	}

	public void setAction(int position, IAction action, boolean notify) {
		pipe.setAction(position, action);
		if (CoreProxy.proxy.isRenderWorld(pipe.worldObj) && notify) {
			sendSelectionChange(position);
		}
	}

	/** GATE INFORMATION **/
	public String getGateGuiFile() {
		return pipe.gate.getGuiFile();
	}

	public int getGateOrdinal() {
		return pipe.gate.kind.ordinal();
	}

	public String getGateName() {
		return pipe.gate.getName();
	}

}
