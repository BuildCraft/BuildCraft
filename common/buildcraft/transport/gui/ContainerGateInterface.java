/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
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

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.transport.IPipe;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.transport.ActionActiveState;
import buildcraft.transport.Gate;
import buildcraft.transport.gates.GateDefinition;

public class ContainerGateInterface extends BuildCraftContainer implements ICommandReceiver {

	public ActionActiveState[] actionsState = new ActionActiveState[8];
	public GuiGateInterface gateCallback;

	IInventory playerIInventory;
	private final IPipe pipe;
	private Gate gate;
	private final NavigableSet<IStatement> potentialTriggers = new TreeSet<IStatement>(new Comparator<IStatement>() {
		@Override
		public int compare(IStatement o1, IStatement o2) {
			return o1.getUniqueTag().compareTo(o2.getUniqueTag());
		}
	});

	private final NavigableSet<IStatement> potentialActions = new TreeSet<IStatement>(new Comparator<IStatement>() {
		@Override
		public int compare(IStatement o1, IStatement o2) {
			return o1.getUniqueTag().compareTo(o2.getUniqueTag());
		}
	});

	private boolean isSynchronized = false;
	private boolean isNetInitialized = false;
	private int lastTriggerState = 0;

	public ContainerGateInterface(IInventory playerInventory, IPipe pipe) {
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
		if (!pipe.getTile().getWorld().isRemote) {
			potentialTriggers.addAll(gate.getAllValidTriggers());
			potentialActions.addAll(gate.getAllValidActions());

			Iterator<IStatement> it = potentialTriggers.iterator();

			while (it.hasNext()) {
				IStatement trigger = it.next();

				if (trigger.minParameters() > gate.material.numTriggerParameters) {
					it.remove();
				}
			}

			it = potentialActions.iterator();

			while (it.hasNext()) {
				IStatement action = it.next();

				if (action.minParameters() > gate.material.numActionParameters) {
					it.remove();
				}
			}
		}
		if (gateCallback != null) {
			gateCallback.setGate(gate);
		}
	}

	private static String[] statementsToStrings(Collection<IStatement> statements) {
		final int size = statements.size();
		String[] array = new String[size];
		int pos = 0;
		for (IStatement statement : statements) {
			array[pos++] = statement.getUniqueTag();
		}
		return array;
	}

	private static void stringsToStatements(Collection<IStatement> statements, String[] strings) {
		statements.clear();
		for (String id : strings) {
			statements.add(StatementManager.statements.get(id));
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
		if (!isNetInitialized && pipe.getTile().getWorld().isRemote) {
			isNetInitialized = true;
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "initRequest", null));
		}

		if (!isSynchronized && pipe.getTile().getWorld().isRemote && gate != null) {
			isSynchronized = true;
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "selectionRequest", null));
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
			for (Object crafter : this.crafters) {
				ICrafting viewingPlayer = (ICrafting) crafter;

				viewingPlayer.sendProgressBarUpdate(this, 0 /* State update */, state);
			}

			lastTriggerState = state;
		}
	}


	/**
	 * TRIGGERS *
	 */
	public boolean hasTriggers() {
		return potentialTriggers.size() > 0;
	}

	public IStatement getFirstTrigger() {
		if (potentialTriggers.isEmpty()) {
			return null;
		} else {
			return potentialTriggers.first();
		}
	}

	public IStatement getLastTrigger() {
		if (potentialTriggers.isEmpty()) {
			return null;
		} else {
			return potentialTriggers.last();
		}
	}

	public Iterator<IStatement> getTriggerIterator(boolean descending) {
		return descending ? potentialTriggers.descendingIterator() : potentialTriggers.iterator();
	}

	public ActionActiveState getActionState(int i) {
		if (gate == null) {
			return ActionActiveState.Deactivated;
		} else {
			return gate.actionsState[i];
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

	public IStatement getFirstAction() {
		if (potentialActions.isEmpty()) {
			return null;
		} else {
			return potentialActions.first();
		}
	}

	public IStatement getLastAction() {
		if (potentialActions.isEmpty()) {
			return null;
		} else {
			return potentialActions.last();
		}
	}

	public Iterator<IStatement> getActionIterator(boolean descending) {
		return descending ? potentialActions.descendingIterator() : potentialActions.iterator();
	}

	// PACKET GENERATION
	public Packet getStatementPacket(final String name, final int slot, final IStatement statement) {
		final String statementKind = statement != null ? statement.getUniqueTag() : null;
		return new PacketCommand(this, name, new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeByte(slot);
				NetworkUtils.writeUTF(data, statementKind);
			}
		});
	}

	public Packet getStatementParameterPacket(final String name, final int slot,
											  final int paramSlot, final IStatementParameter parameter) {
		final String parameterName = parameter != null ? parameter.getUniqueTag() : null;
		final NBTTagCompound parameterNBT = new NBTTagCompound();
		if (parameter != null) {
			parameter.writeToNBT(parameterNBT);
		}
		return new PacketCommand(this, name, new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeByte(slot);
				data.writeByte(paramSlot);
				NetworkUtils.writeUTF(data, parameterName);
				NetworkUtils.writeNBT(data, parameterNBT);
			}
		});
	}

	public void setGate(int direction) {
		this.gate = (Gate) pipe.getGate(ForgeDirection.getOrientation(direction));
		init();
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isServer()) {
			EntityPlayer player = (EntityPlayer) sender;
			if ("initRequest".equals(command)) {
				final String[] triggerStrings = statementsToStrings(potentialTriggers);
				final String[] actionStrings = statementsToStrings(potentialActions);

				BuildCraftCore.instance.sendToPlayer(player, new PacketCommand(this, "init", new CommandWriter() {
					public void write(ByteBuf data) {
						data.writeByte(gate.getDirection().ordinal());
						data.writeShort(triggerStrings.length);
						data.writeShort(actionStrings.length);
						for (String trigger : triggerStrings) {
							NetworkUtils.writeUTF(data, trigger);
						}
						for (String action : actionStrings) {
							NetworkUtils.writeUTF(data, action);
						}
					}
				}));
			} else if ("selectionRequest".equals(command)) {
				for (int position = 0; position < gate.material.numSlots; position++) {
					IStatement action = gate.getAction(position);
					IStatement trigger = gate.getTrigger(position);
					BuildCraftCore.instance.sendToPlayer(player, getStatementPacket("setAction", position, action));
					BuildCraftCore.instance.sendToPlayer(player, getStatementPacket("setTrigger", position, trigger));
					for (int p = 0; p < gate.material.numActionParameters; ++p) {
						BuildCraftCore.instance.sendToPlayer(player, getStatementParameterPacket(
								"setActionParameter", position, p, gate.getActionParameter(position, p)));
					}
					for (int q = 0; q < gate.material.numTriggerParameters; ++q) {
						BuildCraftCore.instance.sendToPlayer(player, getStatementParameterPacket(
								"setTriggerParameter", position, q, gate.getTriggerParameter(position, q)));
					}
				}
			}
		} else if (side.isClient()) {
			if ("init".equals(command)) {
				setGate(stream.readByte());
				String[] triggerStrings = new String[stream.readShort()];
				String[] actionStrings = new String[stream.readShort()];
				for (int i = 0; i < triggerStrings.length; i++) {
					triggerStrings[i] = NetworkUtils.readUTF(stream);
				}
				for (int i = 0; i < actionStrings.length; i++) {
					actionStrings[i] = NetworkUtils.readUTF(stream);
				}

				stringsToStatements(this.potentialTriggers, triggerStrings);
				stringsToStatements(this.potentialActions, actionStrings);
			}
		}

		if ("setAction".equals(command)) {
			setAction(stream.readUnsignedByte(), NetworkUtils.readUTF(stream), false);
		} else if ("setTrigger".equals(command)) {
			setTrigger(stream.readUnsignedByte(), NetworkUtils.readUTF(stream), false);
		} else if ("setActionParameter".equals(command) || "setTriggerParameter".equals(command)) {
			int slot = stream.readUnsignedByte();
			int param = stream.readUnsignedByte();
			String parameterName = NetworkUtils.readUTF(stream);
			NBTTagCompound parameterData = NetworkUtils.readNBT(stream);
			IStatementParameter parameter = null;
			if (parameterName != null && parameterName.length() > 0) {
				parameter = StatementManager.createParameter(parameterName);
			}

			if (parameter != null) {
				parameter.readFromNBT(parameterData);
				if ("setActionParameter".equals(command)) {
					setActionParameter(slot, param, parameter, false);
				} else {
					setTriggerParameter(slot, param, parameter, false);
				}
			}
		}
	}

	public void setAction(int action, String tag, boolean notifyServer) {
		if (gate == null) {
			return;
		}

		IStatement statement = null;

		if (tag != null && tag.length() > 0) {
			statement = StatementManager.statements.get(tag);
		}
		gate.setAction(action, statement);

		if (pipe.getTile().getWorld().isRemote && notifyServer) {
			BuildCraftCore.instance.sendToServer(getStatementPacket("setAction", action, statement));
		}
	}

	public void setTrigger(int trigger, String tag, boolean notifyServer) {
		if (gate == null) {
			return;
		}

		IStatement statement = null;

		if (tag != null && tag.length() > 0) {
			statement = StatementManager.statements.get(tag);
		}
		gate.setTrigger(trigger, statement);

		if (pipe.getTile().getWorld().isRemote && notifyServer) {
			BuildCraftCore.instance.sendToServer(getStatementPacket("setTrigger", trigger, statement));
		}
	}

	public void setActionParameter(int action, int param, IStatementParameter parameter, boolean notifyServer) {
		if (gate == null) {
			return;
		}

		gate.setActionParameter(action, param, parameter);

		if (pipe.getTile().getWorld().isRemote && notifyServer) {
			BuildCraftCore.instance.sendToServer(getStatementParameterPacket("setActionParameter", action, param, parameter));
		}
	}

	public void setTriggerParameter(int trigger, int param, IStatementParameter parameter, boolean notifyServer) {
		if (gate == null) {
			return;
		}

		gate.setTriggerParameter(trigger, param, parameter);

		if (pipe.getTile().getWorld().isRemote && notifyServer) {
			BuildCraftCore.instance.sendToServer(getStatementParameterPacket("setTriggerParameter", trigger, param, parameter));
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
