/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IStatementParameter;
import buildcraft.api.gates.IStatement;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.IStatementParameter;
import buildcraft.api.gates.StatementManager;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;
import buildcraft.transport.ActionActiveState;
import buildcraft.transport.Gate;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.GateDefinition;

public class ContainerGateInterface extends BuildCraftContainer {

	public ActionActiveState[] actionsState = new ActionActiveState[8];
	public GuiGateInterface gateCallback;

	IInventory playerIInventory;
	private final Pipe<?> pipe;
	private Gate gate;
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

	public ContainerGateInterface(IInventory playerInventory, Pipe<?> pipe) {
		super(0);

		for (int i = 0; i < actionsState.length; ++i) {
			actionsState[i] = ActionActiveState.Deactivated;
		}

		this.pipe = pipe;
		this.playerIInventory = playerInventory;

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new Slot(playerIInventory, x + y * 9 + 9, 8 + x * 18, 0));
			}
		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(playerIInventory, x, 8 + x * 18, 0));
		}
	}

	public void init() {
		if (gate == null) {
			return;
		}

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				getSlot(x + y * 9).yDisplayPosition = gate.material.guiHeight - 84 + y * 18;
			}
		}

		for (int x = 0; x < 9; x++) {
			getSlot(x + 27).yDisplayPosition = gate.material.guiHeight - 26;
		}

		// Do not attempt to create a list of potential actions and triggers on
		// the client.
		if (!pipe.container.getWorldObj().isRemote) {
			potentialTriggers.addAll(gate.getAllValidTriggers());
			potentialActions.addAll(gate.getAllValidActions());

			if (gate.material.numTriggerParameters == 0) {
				Iterator<ITrigger> it = potentialTriggers.iterator();

				while (it.hasNext()) {
					ITrigger trigger = it.next();

					if (trigger.minParameters() > 0) {
						it.remove();
					}
				}
			}

			if (gate.material.numActionParameters == 0) {
				Iterator<IAction> it = potentialActions.iterator();

				while (it.hasNext()) {
					IAction action = it.next();

					if (action.minParameters() > 0) {
						it.remove();
					}
				}
			}
		}
		if (gateCallback != null) {
			gateCallback.setGate(gate);
		}
	}

	private static <T extends IStatement> String[] statementsToStrings(Collection<T> statements) {
		final int size = statements.size();
		String[] array = new String[size];
		int pos = 0;
		for (T statement : statements) {
			array[pos++] = statement.getUniqueTag();
		}
		return array;
	}

	private static <T extends IStatement> void stringsToStatements(Collection<T> statements, String[] strings) {
		statements.clear();
		for (String id : strings) {
			statements.add((T) StatementManager.statements.get(id));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return gate != null;
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
	 * Initializes the list of triggers and actions on the gate and
	 * (re-)requests the current selection on the gate if needed.
	 */
	public void synchronize() {
		if (!isNetInitialized && pipe.container.getWorldObj().isRemote) {
			isNetInitialized = true;
			RPCHandler.rpcServer(this, "initRequest");
		}

		if (!isSynchronized && pipe.container.getWorldObj().isRemote && gate != null) {
			isSynchronized = true;
			RPCHandler.rpcServer(this, "selectionRequest");
		}
	}

	@Override
	public void updateProgressBar(int id, int state) {
		if (id == 0 /* Action state update */) {
			for (int i = 0; i < 8; i++) {
				/* Bit mask of triggers */
				actionsState[i] = ActionActiveState.values()[(state >> (i * 2)) & 0x03];
			}
		}
	}

	/**
	 * SERVER SIDE *
	 */
	private int calculateTriggerState() {
		if (gate == null) {
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


	/**
	 * Sends gate info to the client
	 */
	@RPC(RPCSide.SERVER)
	public void initRequest(RPCMessageInfo info) {
		EntityPlayer player = info.sender;
		RPCHandler.rpcPlayer(player, this, "setGate", gate.getDirection().ordinal());
		RPCHandler.rpcPlayer(player, this, "setPotential", statementsToStrings(potentialActions), statementsToStrings(potentialTriggers));
	}

	@RPC(RPCSide.CLIENT)
	public void setPotential(String[] potentialActions, String[] potentialTriggers) {
		stringsToStatements(this.potentialActions, potentialActions);
		stringsToStatements(this.potentialTriggers, potentialTriggers);
	}

	@RPC(RPCSide.CLIENT)
	public void setGate(int direction) {
		this.gate = pipe.gates[direction];
		init();
	}

	@RPC(RPCSide.SERVER)
	public void selectionRequest(RPCMessageInfo info) {
		EntityPlayer player = info.sender;
		for (int position = 0; position < gate.material.numSlots; position++) {
			IAction action = gate.getAction(position);
			ITrigger trigger = gate.getTrigger(position);
			RPCHandler.rpcPlayer(player, this, "setAction", position, action != null ? action.getUniqueTag() : null, false);
			RPCHandler.rpcPlayer(player, this, "setTrigger", position, trigger != null ? trigger.getUniqueTag() : null, false);
			for (int p = 0; p < 3; ++p) {
				RPCHandler.rpcPlayer(player, this, "setActionParameter", position, p,
						gate.getActionParameter(position, p), false);
				RPCHandler.rpcPlayer(player, this, "setTriggerParameter", position, p,
						gate.getTriggerParameter(position, p), false);
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

	public ActionActiveState getActionState(int i) {
		if (gate == null) {
			return ActionActiveState.Deactivated;
		} else {
			return gate.actionsState [i];
		}
	}

	@RPC(RPCSide.BOTH)
	public void setTrigger(int trigger, String tag, boolean notifyServer) {
		if (gate == null) {
			return;
		}

		if (tag != null) {
			gate.setTrigger(trigger, (ITrigger) StatementManager.statements.get(tag));
		} else {
			gate.setTrigger(trigger, null);
		}

		if (pipe.container.getWorldObj().isRemote && notifyServer) {
			RPCHandler.rpcServer(this, "setTrigger", trigger, tag, false);
		}
	}

	@RPC(RPCSide.BOTH)
	public void setTriggerParameter(int trigger, int param, IStatementParameter parameter, boolean notifyServer) {
		if (gate == null) {
			return;
		}

		gate.setTriggerParameter(trigger, param, parameter);

		if (pipe.container.getWorldObj().isRemote && notifyServer) {
			RPCHandler.rpcServer(this, "setTriggerParameter", trigger, param, parameter, false);
		}
	}

	public IStatementParameter getTriggerParameter(int trigger, int param) {
		if (gate == null) {
			return null;
		} else {
			return gate.getTriggerParameter(trigger, param);
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
		if (gate == null) {
			return;
		}

		if (tag != null) {
			gate.setAction(action, (IAction) StatementManager.statements.get(tag));
		} else {
			gate.setAction(action, null);
		}

		if (pipe.container.getWorldObj().isRemote && notifyServer) {
			RPCHandler.rpcServer(this, "setAction", action, tag, false);
		}
	}

	@RPC(RPCSide.BOTH)
	public void setActionParameter(int action, int param, IStatementParameter parameter, boolean notifyServer) {
		if (gate == null) {
			return;
		}

		gate.setActionParameter(action, param, parameter);

		if (pipe.container.getWorldObj().isRemote && notifyServer) {
			RPCHandler.rpcServer(this, "setActionParameter", action, param, parameter, false);
		}
	}

	/**
	 * GATE INFORMATION *
	 */
	public ResourceLocation getGateGuiFile() {
		return gate.material.guiFile;
	}

	public String getGateName() {
		return GateDefinition.getLocalizedName(gate.material, gate.logic);
	}
}
