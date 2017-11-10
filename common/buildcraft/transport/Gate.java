/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.IGate;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionReceptor;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerExternalOverride;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.statements.containers.ISidedStatementContainer;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.GuiIds;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.statements.ActionValve;

public final class Gate implements IGate, ISidedStatementContainer, IRedstoneStatementContainer {

	public static int MAX_STATEMENTS = 8;
	public static int MAX_PARAMETERS = 3;

	public final Pipe<?> pipe;
	public final GateMaterial material;
	public final GateLogic logic;
	public final BiMap<IGateExpansion, GateExpansionController> expansions = HashBiMap.create();

	public IStatement[] triggers = new IStatement[MAX_STATEMENTS];
	public IStatementParameter[][] triggerParameters = new IStatementParameter[MAX_STATEMENTS][MAX_PARAMETERS];

	public IStatement[] actions = new IStatement[MAX_STATEMENTS];
	public IStatementParameter[][] actionParameters = new IStatementParameter[MAX_STATEMENTS][MAX_PARAMETERS];

	public ActionActiveState[] actionsState = new ActionActiveState[MAX_STATEMENTS];
	public ArrayList<StatementSlot> activeActions = new ArrayList<StatementSlot>();

	public byte broadcastSignal, prevBroadcastSignal;
	public int redstoneOutput = 0;
	public int redstoneOutputSide = 0;

	/**
	 * this is the internal pulsing state of the gate. Intended to be managed
	 * by the server side only, the client is supposed to be referring to the
	 * state of the renderer, and update moveStage accordingly.
	 */
	public boolean isPulsing = false;
	private ForgeDirection direction;

	private HashMultiset<IStatement> statementCounts = HashMultiset.create();
	private int[] actionGroups = new int[]{0, 1, 2, 3, 4, 5, 6, 7};

	// / CONSTRUCTOR
	public Gate(Pipe<?> pipe, GateMaterial material, GateLogic logic, ForgeDirection direction) {
		this.pipe = pipe;
		this.material = material;
		this.logic = logic;
		this.direction = direction;

		for (int i = 0; i < actionsState.length; ++i) {
			actionsState[i] = ActionActiveState.Deactivated;
		}
	}

	public void setTrigger(int position, IStatement trigger) {
		if (trigger != triggers[position]) {
			for (int i = 0; i < triggerParameters[position].length; i++) {
				triggerParameters[position][i] = null;
			}
		}
		triggers[position] = trigger;
	}

	public IStatement getTrigger(int position) {
		return triggers[position];
	}

	public void setAction(int position, IStatement action) {
		// HUGE HACK! TODO - Remove in an API rewrite by adding
		// ways for actions to fix their state on removal.
		if (actions[position] instanceof ActionValve && pipe != null && pipe.transport != null) {
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
				pipe.transport.allowInput(side, true);
				pipe.transport.allowOutput(side, true);
			}
		}

		if (action != actions[position]) {
			for (int i = 0; i < actionParameters[position].length; i++) {
				actionParameters[position][i] = null;
			}
		}

		actions[position] = action;

		recalculateActionGroups();
	}

	public IStatement getAction(int position) {
		return actions[position];
	}

	public void setTriggerParameter(int trigger, int param, IStatementParameter p) {
		triggerParameters[trigger][param] = p;
	}

	public void setActionParameter(int action, int param, IStatementParameter p) {
		actionParameters[action][param] = p;

		recalculateActionGroups();
	}

	public IStatementParameter getTriggerParameter(int trigger, int param) {
		return triggerParameters[trigger][param];
	}

	public IStatementParameter getActionParameter(int action, int param) {
		return actionParameters[action][param];
	}

	public ForgeDirection getDirection() {
		return direction;
	}

	public void setDirection(ForgeDirection direction) {
		this.direction = direction;
	}

	public void addGateExpansion(IGateExpansion expansion) {
		if (!expansions.containsKey(expansion)) {
			expansions.put(expansion, expansion.makeController(pipe != null ? pipe.container : null));
		}
	}

	public void writeStatementsToNBT(NBTTagCompound data) {
		for (int i = 0; i < material.numSlots; ++i) {
			if (triggers[i] != null) {
				data.setString("trigger[" + i + "]", triggers[i].getUniqueTag());
			}

			if (actions[i] != null) {
				data.setString("action[" + i + "]", actions[i].getUniqueTag());
			}

			for (int j = 0; j < material.numTriggerParameters; ++j) {
				if (triggerParameters[i][j] != null) {
					NBTTagCompound cpt = new NBTTagCompound();
					cpt.setString("kind", triggerParameters[i][j].getUniqueTag());
					triggerParameters[i][j].writeToNBT(cpt);
					data.setTag("triggerParameters[" + i + "][" + j + "]", cpt);
				}
			}

			for (int j = 0; j < material.numActionParameters; ++j) {
				if (actionParameters[i][j] != null) {
					NBTTagCompound cpt = new NBTTagCompound();
					cpt.setString("kind", actionParameters[i][j].getUniqueTag());
					actionParameters[i][j].writeToNBT(cpt);
					data.setTag("actionParameters[" + i + "][" + j + "]", cpt);
				}
			}
		}
	}

	// / SAVING & LOADING
	public void writeToNBT(NBTTagCompound data) {
		data.setString("material", material.name());
		data.setString("logic", logic.name());
		data.setInteger("direction", direction.ordinal());
		NBTTagList exList = new NBTTagList();
		for (GateExpansionController con : expansions.values()) {
			NBTTagCompound conNBT = new NBTTagCompound();
			conNBT.setString("type", con.getType().getUniqueIdentifier());
			NBTTagCompound conData = new NBTTagCompound();
			con.writeToNBT(conData);
			conNBT.setTag("data", conData);
			exList.appendTag(conNBT);
		}
		data.setTag("expansions", exList);

		writeStatementsToNBT(data);

		data.setByte("wireState", broadcastSignal);

		data.setByte("redstoneOutput", (byte) redstoneOutput);
	}

	public void readStatementsFromNBT(NBTTagCompound data) {
		// Clear
		for (int i = 0; i < material.numSlots; ++i) {
			triggers[i] = null;
			actions[i] = null;
			for (int j = 0; j < material.numTriggerParameters; j++) {
				triggerParameters[i][j] = null;
			}
			for (int j = 0; j < material.numActionParameters; j++) {
				actionParameters[i][j] = null;
			}
		}

		// Read
		for (int i = 0; i < material.numSlots; ++i) {
			if (data.hasKey("trigger[" + i + "]")) {
				triggers[i] = StatementManager.statements.get(data.getString("trigger[" + i + "]"));
			}

			if (data.hasKey("action[" + i + "]")) {
				actions[i] = StatementManager.statements.get(data.getString("action[" + i + "]"));
			}

			// This is for legacy trigger loading
			if (data.hasKey("triggerParameters[" + i + "]")) {
				triggerParameters[i][0] = new StatementParameterItemStack();
				triggerParameters[i][0].readFromNBT(data.getCompoundTag("triggerParameters[" + i + "]"));
			}

			for (int j = 0; j < material.numTriggerParameters; ++j) {
				if (data.hasKey("triggerParameters[" + i + "][" + j + "]")) {
					NBTTagCompound cpt = data.getCompoundTag("triggerParameters[" + i + "][" + j + "]");
					triggerParameters[i][j] = StatementManager.createParameter(cpt.getString("kind"));
					triggerParameters[i][j].readFromNBT(cpt);
				}
			}

			for (int j = 0; j < material.numActionParameters; ++j) {
				if (data.hasKey("actionParameters[" + i + "][" + j + "]")) {
					NBTTagCompound cpt = data.getCompoundTag("actionParameters[" + i + "][" + j + "]");
					actionParameters[i][j] = StatementManager.createParameter(cpt.getString("kind"));
					actionParameters[i][j].readFromNBT(cpt);
				}
			}
		}

		recalculateActionGroups();
	}

	public boolean verifyGateStatements() {
		List<IStatement> triggerList = getAllValidTriggers();
		List<IStatement> actionList = getAllValidActions();
		boolean warning = false;

		for (int i = 0; i < MAX_STATEMENTS; ++i) {
			if ((triggers[i] != null || actions[i] != null) && i >= material.numSlots) {
				triggers[i] = null;
				actions[i] = null;
				warning = true;
				continue;
			}

			if (triggers[i] != null) {
				if (!triggerList.contains(triggers[i]) ||
						triggers[i].minParameters() > material.numTriggerParameters) {
					triggers[i] = null;
					warning = true;
				}
			}

			if (actions[i] != null) {
				if (!actionList.contains(actions[i]) ||
						actions[i].minParameters() > material.numActionParameters) {
					actions[i] = null;
					warning = true;
				}
			}
		}

		if (warning) {
			recalculateActionGroups();
		}

		return !warning;
	}

	public void readFromNBT(NBTTagCompound data) {
		readStatementsFromNBT(data);

		if (data.hasKey("wireState[0]")) {
			for (PipeWire wire : PipeWire.VALUES) {
				if (data.getBoolean("wireState[" + wire.ordinal() + "]")) {
					broadcastSignal |= 1 << wire.ordinal();
				}
			}
		} else {
			broadcastSignal = data.getByte("wireState");
		}

		redstoneOutput = data.getByte("redstoneOutput");
	}

	// GUI
	public void openGui(EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			player.openGui(BuildCraftTransport.instance, GuiIds.GATES, pipe.container.getWorldObj(), pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
			// ThermalExpansion Autonomous Activator crash fix
			if (player.openContainer instanceof ContainerGateInterface) {
				((ContainerGateInterface) player.openContainer).setGate(direction.ordinal());
			}
		}
	}

	// UPDATING
	public void tick() {
		for (GateExpansionController expansion : expansions.values()) {
			expansion.tick(this);
		}
	}

	public ItemStack getGateItem() {
		return ItemGate.makeGateItem(this);
	}

	public void dropGate() {
		pipe.dropItem(getGateItem());
	}

	public void resetGate() {
		if (redstoneOutput != 0) {
			redstoneOutput = 0;
			pipe.updateNeighbors(true);
		}
	}

	public boolean isGateActive() {
		for (ActionActiveState state : actionsState) {
			if (state == ActionActiveState.Activated) {
				return true;
			}
		}

		return false;
	}

	public boolean isGatePulsing() {
		return isPulsing;
	}

	public int getRedstoneOutput() {
		return redstoneOutput;
	}

	public int getSidedRedstoneOutput() {
		return redstoneOutputSide;
	}

	public void setRedstoneOutput(boolean sideOnly, int value) {
		redstoneOutputSide = value;

		if (!sideOnly) {
			redstoneOutput = value;
		}
	}

	public void startResolution() {
		for (GateExpansionController expansion : expansions.values()) {
			expansion.startResolution();
		}
	}

	public void resolveActions() {
		int oldRedstoneOutput = redstoneOutput;
		redstoneOutput = 0;

		int oldRedstoneOutputSide = redstoneOutputSide;
		redstoneOutputSide = 0;

		boolean wasActive = activeActions.size() > 0;

		prevBroadcastSignal = broadcastSignal;
		broadcastSignal = 0;

		// Tell the gate to prepare for resolving actions. (Disable pulser)
		startResolution();

		// Computes the actions depending on the triggers
		for (int it = 0; it < MAX_STATEMENTS; ++it) {
			actionsState[it] = ActionActiveState.Deactivated;

			IStatement trigger = triggers[it];
			IStatementParameter[] parameter = triggerParameters[it];

			if (trigger != null) {
				if (isTriggerActive(trigger, parameter)) {
					actionsState[it] = ActionActiveState.Partial;
				}
			}
		}

		activeActions.clear();

		for (int it = 0; it < MAX_STATEMENTS; ++it) {
			boolean allActive = true;
			boolean oneActive = false;

			if (actions[it] == null) {
				continue;
			}

			for (int j = 0; j < MAX_STATEMENTS; ++j) {
				if (actionGroups[j] == it) {
					if (actionsState[j] != ActionActiveState.Partial) {
						allActive = false;
					} else {
						oneActive = true;
					}
				}
			}

			if ((logic == GateLogic.AND && allActive && oneActive) || (logic == GateLogic.OR && oneActive)) {
				if (logic == GateLogic.AND) {
					for (int j = 0; j < MAX_STATEMENTS; ++j) {
						if (actionGroups[j] == it) {
							actionsState[j] = ActionActiveState.Activated;
						}
					}
				}

				StatementSlot slot = new StatementSlot();
				slot.statement = actions[it];
				slot.parameters = actionParameters[it];
				activeActions.add(slot);
			}

			if (logic == GateLogic.OR && actionsState[it] == ActionActiveState.Partial) {
				actionsState[it] = ActionActiveState.Activated;
			}
		}

		statementCounts.clear();

		for (int it = 0; it < MAX_STATEMENTS; ++it) {
			if (actionsState[it] == ActionActiveState.Activated) {
				statementCounts.add(actions[it], 1);
			}
		}

		// Activate the actions
		for (StatementSlot slot : activeActions) {
			IStatement action = slot.statement;
			if (action instanceof IActionInternal) {
				((IActionInternal) action).actionActivate(this, slot.parameters);
			} else if (action instanceof IActionExternal) {
				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
					TileEntity tile = this.getPipe().getTile().getNeighborTile(side);
					if (tile != null) {
						((IActionExternal) action).actionActivate(tile, side, this, slot.parameters);
					}
				}
			} else {
				continue;
			}

			// Custom gate actions take precedence over defaults.
			if (resolveAction(action)) {
				continue;
			}

			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = pipe.container.getTile(side);
				if (tile instanceof IActionReceptor) {
					IActionReceptor recept = (IActionReceptor) tile;
					recept.actionActivated(action, slot.parameters);
				}
			}
		}

		pipe.actionsActivated(activeActions);

		if (oldRedstoneOutput != redstoneOutput || oldRedstoneOutputSide != redstoneOutputSide) {
			pipe.updateNeighbors(true);
		}

		if (prevBroadcastSignal != broadcastSignal) {
			pipe.scheduleWireUpdate();
		}

		boolean isActive = activeActions.size() > 0;

		if (wasActive != isActive) {
			pipe.container.scheduleRenderUpdate();
		}
	}

	public boolean resolveAction(IStatement action) {
		for (GateExpansionController expansion : expansions.values()) {
			if (expansion.resolveAction(action, statementCounts.count(action))) {
				return true;
			}
		}
		return false;
	}

	public boolean isTriggerActive(IStatement trigger, IStatementParameter[] parameters) {
		if (trigger == null) {
			return false;
		}

		if (trigger instanceof ITriggerInternal) {
			if (((ITriggerInternal) trigger).isTriggerActive(this, parameters)) {
				return true;
			}
		} else if (trigger instanceof ITriggerExternal) {
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = this.getPipe().getTile().getNeighborTile(side);
				if (tile != null) {
					if (tile instanceof ITriggerExternalOverride) {
						ITriggerExternalOverride.Result result = ((ITriggerExternalOverride) tile).override(side, this, parameters);
						if (result == ITriggerExternalOverride.Result.TRUE) {
							return true;
						} else if (result == ITriggerExternalOverride.Result.FALSE) {
							continue;
						}
					}
					if (((ITriggerExternal) trigger).isTriggerActive(tile, side, this, parameters)) {
						return true;
					}
				}
			}
		}

		// TODO: This can probably be refactored with regular triggers instead
		// of yet another system.
		for (GateExpansionController expansion : expansions.values()) {
			if (expansion.isTriggerActive(trigger, parameters)) {
				return true;
			}
		}

		return false;
	}

	// TRIGGERS
	public void addTriggers(List<ITriggerInternal> list) {
		for (PipeWire wire : PipeWire.VALUES) {
			if (pipe.wireSet[wire.ordinal()] && wire.ordinal() < material.maxWireColor) {
				list.add(BuildCraftTransport.triggerPipeWireActive[wire.ordinal()]);
				list.add(BuildCraftTransport.triggerPipeWireInactive[wire.ordinal()]);
			}
		}

		for (GateExpansionController expansion : expansions.values()) {
			expansion.addTriggers(list);
		}
	}

	public List<IStatement> getAllValidTriggers() {
		ArrayList<IStatement> allTriggers = new ArrayList<IStatement>(64);
		allTriggers.addAll(StatementManager.getInternalTriggers(this));

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = pipe.container.getTile(o);
			allTriggers.addAll(StatementManager.getExternalTriggers(o, tile));
		}

		return allTriggers;
	}

	// ACTIONS
	public void addActions(List<IActionInternal> list) {
		for (PipeWire wire : PipeWire.VALUES) {
			if (pipe.wireSet[wire.ordinal()] && wire.ordinal() < material.maxWireColor) {
				list.add(BuildCraftTransport.actionPipeWire[wire.ordinal()]);
			}
		}

		for (GateExpansionController expansion : expansions.values()) {
			expansion.addActions(list);
		}
	}

	public List<IStatement> getAllValidActions() {
		ArrayList<IStatement> allActions = new ArrayList<IStatement>(64);
		allActions.addAll(StatementManager.getInternalActions(this));

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = pipe.container.getTile(o);
			allActions.addAll(StatementManager.getExternalActions(o, tile));
		}

		return allActions;
	}

	//@Override TODO
	public void setPulsing(boolean pulsing) {
		if (pulsing != isPulsing) {
			isPulsing = pulsing;
			pipe.container.scheduleRenderUpdate();
		}
	}

	private void recalculateActionGroups() {
		for (int i = 0; i < MAX_STATEMENTS; ++i) {
			actionGroups[i] = i;

			for (int j = i - 1; j >= 0; --j) {
				if (actions[i] != null && actions[j] != null
						&& actions[i].getUniqueTag().equals(actions[j].getUniqueTag())) {
					boolean sameParams = true;

					for (int p = 0; p < MAX_PARAMETERS; ++p) {
						if ((actionParameters[i][p] != null && actionParameters[j][p] == null)
								|| (actionParameters[i][p] == null && actionParameters[j][p] != null)
								|| (actionParameters[i][p] != null
								&& actionParameters[j][p] != null
								&& !actionParameters[i][p].equals(actionParameters[j][p]))) {
							sameParams = false;
							break;
						}
					}

					if (sameParams) {
						actionGroups[i] = j;
					}
				}
			}
		}
	}

	public void broadcastSignal(PipeWire color) {
		broadcastSignal |= 1 << color.ordinal();
	}

	public IPipe getPipe() {
		return pipe;
	}

	@Override
	public ForgeDirection getSide() {
		return direction;
	}

	@Override
	public TileEntity getTile() {
		return pipe.container;
	}

	@Override
	public List<IStatement> getTriggers() {
		return Arrays.asList(triggers).subList(0, material.numSlots);
	}

	@Override
	public List<IStatement> getActions() {
		return Arrays.asList(actions).subList(0, material.numSlots);
	}

	@Override
	public List<StatementSlot> getActiveActions() {
		return activeActions;
	}

	@Override
	public List<IStatementParameter> getTriggerParameters(int index) {
		if (index < 0 || index >= material.numSlots) {
			return null;
		}
		return Arrays.asList(triggerParameters[index]).subList(0, material.numTriggerParameters);
	}

	@Override
	public List<IStatementParameter> getActionParameters(int index) {
		if (index < 0 || index >= material.numSlots) {
			return null;
		}
		return Arrays.asList(actionParameters[index]).subList(0, material.numActionParameters);
	}

	@Override
	public int getRedstoneInput(ForgeDirection side) {
		return side == ForgeDirection.UNKNOWN ? pipe.container.redstoneInput : pipe.container.redstoneInputSide[side.ordinal()];
	}

	@Override
	public boolean setRedstoneOutput(ForgeDirection side, int value) {
		if (side != this.getSide() && side != ForgeDirection.UNKNOWN) {
			return false;
		}

		setRedstoneOutput(side != ForgeDirection.UNKNOWN, value);
		return true;
	}
}
