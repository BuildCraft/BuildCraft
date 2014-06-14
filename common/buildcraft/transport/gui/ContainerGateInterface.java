/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.StatementManager;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;
import buildcraft.core.utils.Utils;
import buildcraft.transport.ActionState;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.GateDefinition;

public class ContainerGateInterface extends BuildCraftContainer {
	public ActionState[] actionsState = new ActionState[8];

	IInventory playerIInventory;
	Pipe pipe;

	private final NavigableSet<ITrigger> potentialTriggers = new TreeSet<ITrigger>(new Comparator<ITrigger>() {
		@Override
		public int compare(ITrigger o1, ITrigger o2) {
			return o1.getUniqueTag().compareTo(o2.getUniqueTag());
		}
	});

	private final NavigableSet<IAction> potentialActions = new TreeSet<IAction>(new Comparator<IAction>() {
		@Override
		public int compare(IAction o1, IAction o2) {
			return o1.getUniqueTag().compareTo(o2.getUniqueTag());
		}
	});

	private boolean isSynchronized = false;
	private boolean isNetInitialized = false;
	private int lastTriggerState = 0;

	public ContainerGateInterface(IInventory playerInventory, Pipe pipe) {
		super(0);

		for (int i = 0; i < actionsState.length; ++i) {
			actionsState[i] = ActionState.Deactivated;
		}

		this.playerIInventory = playerInventory;

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, pipe.gate.material.guiHeight - 84 + y * 18));
			}
		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, pipe.gate.material.guiHeight - 26));
		}

		this.pipe = pipe;

		// Do not attempt to create a list of potential actions and triggers on
		// the client.
		if (!pipe.container.getWorldObj().isRemote) {
			potentialActions.addAll(pipe.getActions());
			potentialTriggers.addAll(StatementManager.getPipeTriggers(pipe.container));

			if (pipe.container instanceof IOverrideDefaultTriggers) {
				potentialTriggers.addAll(((IOverrideDefaultTriggers) pipe.container).getTriggers());
			}

			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				if (pipe.hasGate(o)) {
					TileEntity tile = pipe.container.getTile(o);
					Block block = pipe.container.getBlock(o);
					potentialTriggers.addAll(StatementManager.getNeighborTriggers(block, tile));
					potentialActions.addAll(StatementManager.getNeighborActions(block, tile));
				}
			}

			if (pipe.gate.material.numTriggerParameters == 0) {
				Iterator<ITrigger> it = potentialTriggers.iterator();

				while (it.hasNext()) {
					ITrigger trigger = it.next();

					if (trigger.minParameters() > 0) {
						it.remove();
					}
				}
			}

			if (pipe.gate.material.numActionParameters == 0) {
				Iterator<IAction> it = potentialActions.iterator();

				while (it.hasNext()) {
					IAction action = it.next();

					if (action.minParameters() > 0) {
						it.remove();
					}
				}
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return !(pipe == null || pipe.gate == null);
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
		potentialActions.clear();
		PacketPayload payload = packet.payload;

		int length = payload.stream.readInt();
		for (int i = 0; i < length; i++) {
			potentialActions.add((IAction) StatementManager.statements.get(Utils.readUTF(payload.stream)));
		}
	}

	/**
	 * Clears list of potential triggers and refills it according to packet.
	 *
	 * @param packet
	 */
	public void updateTriggers(PacketUpdate packet) {
		potentialTriggers.clear();
		PacketPayload payload = packet.payload;

		int length = payload.stream.readInt();

		for (int i = 0; i < length; i++) {
			String trigger = Utils.readUTF(payload.stream);
			potentialTriggers.add((ITrigger) StatementManager.statements.get(trigger));
		}
	}

	/**
	 * Initializes the list of triggers and actions on the gate and
	 * (re-)requests the current selection on the gate if needed.
	 */
	public void synchronize() {
		if (!isNetInitialized && pipe.container.getWorldObj().isRemote) {
			isNetInitialized = true;
			BuildCraftTransport.instance.sendToServer(new PacketCoordinates(PacketIds.GATE_REQUEST_INIT, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord));
		}

		if (!isSynchronized && pipe.container.getWorldObj().isRemote) {
			isSynchronized = true;
			BuildCraftTransport.instance.sendToServer(new PacketCoordinates(PacketIds.GATE_REQUEST_SELECTION, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord));
		}
	}

	@Override
	public void updateProgressBar(int id, int state) {
		if (id == 0 /* Action state update */) {
			for (int i = 0; i < 8; i++) {
				/* Bit mask of triggers */
				actionsState[i] = ActionState.values()[(state >> (i * 2)) & 0x03];
			}
		}
	}

	/**
	 * SERVER SIDE *
	 */
	private int calculateTriggerState() {
		if (pipe.gate == null) {
			return 0;
		}

		int state = 0;

		for (int i = 0; i < actionsState.length; i++) {
			actionsState[i] = getActionState(i);
			state |= (actionsState[i].ordinal() & 0x03) << i * 2;
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
		PacketPayload payload = new PacketPayload(
				new PacketPayload.StreamWriter() {
					@Override
					public void writeData(ByteBuf data) {
						int length = potentialActions.size();
						data.writeInt(length);

						for (IAction action : potentialActions) {
							Utils.writeUTF(data, action.getUniqueTag());
						}
					}
				});

		PacketUpdate packet = new PacketUpdate(PacketIds.GATE_ACTIONS, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, payload);

		// Send to player
		BuildCraftTransport.instance.sendToPlayer(player, packet);
	}

	/**
	 * Sends the list of potential triggers to the client
	 *
	 * @param player
	 */
	private void sendTriggers(EntityPlayer player) {
		PacketPayload payload = new PacketPayload(
				new PacketPayload.StreamWriter() {
					@Override
					public void writeData(ByteBuf data) {

						int length = potentialTriggers.size();
						data.writeInt(length);

						for (ITrigger trigger : potentialTriggers) {
							Utils.writeUTF(data, trigger.getUniqueTag());
						}
					}
				});

		PacketUpdate packet = new PacketUpdate(PacketIds.GATE_TRIGGERS, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, payload);

		// Send to player
		BuildCraftTransport.instance.sendToPlayer(player, packet);
	}

	/**
	 * Sends the current selection on the gate to the client.
	 *
	 * @param player
	 */
	public void sendSelection(EntityPlayer player) {
		if (pipe == null || pipe.gate == null) {
			return;
		}

		for (int position = 0; position < pipe.gate.material.numSlots; position++) {
			for (int p = 0; p < 3; ++p) {
				RPCHandler.rpcPlayer(player, this, "setTriggerParameter", position, p,
						pipe.gate.getTriggerParameter(position, p), false);
			}
		}
	}

	/**
	 * TRIGGERS *
	 */
	public boolean hasTriggers() {
		return potentialTriggers.size() > 0;
	}

	public ITrigger getFirstTrigger() {
		if (potentialTriggers.isEmpty()) {
			return null;
		} else {
			return potentialTriggers.first();
		}
	}

	public ITrigger getLastTrigger() {
		if (potentialTriggers.isEmpty()) {
			return null;
		} else {
			return potentialTriggers.last();
		}
	}

	public Iterator<ITrigger> getTriggerIterator(boolean descending) {
		return descending ? potentialTriggers.descendingIterator() : potentialTriggers.iterator();
	}

	public ActionState getActionState(int i) {
		if (pipe.gate == null) {
			return ActionState.Deactivated;
		} else {
			return pipe.gate.actionsState [i];
		}
	}

	@RPC(RPCSide.BOTH)
	public void setTrigger(int trigger, String tag, boolean notifyServer) {
		if (pipe.gate == null) {
			return;
		}

		if (tag != null) {
			pipe.gate.setTrigger(trigger, (ITrigger) StatementManager.statements.get(tag));
		} else {
			pipe.gate.setTrigger(trigger, null);
		}

		if (pipe.container.getWorldObj().isRemote && notifyServer) {
			RPCHandler.rpcServer(this, "setTrigger", trigger, tag, false);
		}
	}

	@RPC(RPCSide.BOTH)
	public void setTriggerParameter(int trigger, int param, ITriggerParameter parameter, boolean notifyServer) {
		if (pipe.gate == null) {
			return;
		}

		pipe.gate.setTriggerParameter(trigger, param, parameter);

		if (pipe.container.getWorldObj().isRemote && notifyServer) {
			RPCHandler.rpcServer(this, "setTriggerParameter", trigger, param, parameter, false);
		}
	}

	/**
	 * ACTIONS *
	 */
	public boolean hasActions() {
		return !potentialActions.isEmpty();
	}

	public IAction getFirstAction() {
		if (potentialActions.isEmpty()) {
			return null;
		} else {
			return potentialActions.first();
		}
	}

	public IAction getLastAction() {
		if (potentialActions.isEmpty()) {
			return null;
		} else {
			return potentialActions.last();
		}
	}

	public Iterator<IAction> getActionIterator(boolean descending) {
		return descending ? potentialActions.descendingIterator() : potentialActions.iterator();
	}

	@RPC(RPCSide.BOTH)
	public void setAction(int action, String tag, boolean notifyServer) {
		if (pipe.gate == null) {
			return;
		}

		if (tag != null) {
			pipe.gate.setAction(action, (IAction) StatementManager.statements.get(tag));
		} else {
			pipe.gate.setAction(action, null);
		}

		if (pipe.container.getWorldObj().isRemote && notifyServer) {
			RPCHandler.rpcServer(this, "setAction", action, tag, false);
		}
	}

	/**
	 * GATE INFORMATION *
	 */
	public ResourceLocation getGateGuiFile() {
		return pipe.gate.material.guiFile;
	}

	public String getGateName() {
		return GateDefinition.getLocalizedName(pipe.gate.material, pipe.gate.logic);
	}
}
