/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.gates.IGate;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.StatementManager;
import buildcraft.api.gates.TriggerParameterItemStack;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.GuiIds;
import buildcraft.core.statements.ActionRedstoneOutput;
import buildcraft.core.statements.StatementParameterRedstoneGateSideOnly;
import buildcraft.transport.gates.ActionSlot;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.statements.ActionRedstoneFaderOutput;

public final class Gate implements IGate {

	public static int MAX_STATEMENTS = 8;
	public static int MAX_PARAMETERS = 3;

	public final Pipe<?> pipe;
	public final GateMaterial material;
	public final GateLogic logic;
	public final BiMap<IGateExpansion, GateExpansionController> expansions = HashBiMap.create();

	public ITrigger[] triggers = new ITrigger[MAX_STATEMENTS];
	public ITriggerParameter[][] triggerParameters = new ITriggerParameter[8][MAX_PARAMETERS];

	public IAction[] actions = new IAction[MAX_STATEMENTS];
	public IActionParameter[][] actionParameters = new IActionParameter[8][MAX_PARAMETERS];

	public ActionActiveState[] actionsState = new ActionActiveState[MAX_STATEMENTS];
	public ArrayList<ActionSlot> activeActions = new ArrayList<ActionSlot>();

	public BitSet broadcastSignal = new BitSet(PipeWire.VALUES.length);
	public BitSet prevBroadcastSignal = new BitSet(PipeWire.VALUES.length);
	public int redstoneOutput = 0;
	public int redstoneOutputSide = 0;
	
	/**
	 * this is the internal pulsing state of the gate. Intended to be managed
	 * by the server side only, the client is supposed to be referring to the
	 * state of the renderer, and update moveStage accordingly.
	 */
	public boolean isPulsing = false;
	private float pulseStage = 0;
	private ForgeDirection direction;

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

	public void setTrigger(int position, ITrigger trigger) {
		triggers[position] = trigger;
	}

	public ITrigger getTrigger(int position) {
		return triggers[position];
	}

	public void setAction(int position, IAction action) {
		actions[position] = action;
	}

	public IAction getAction(int position) {
		return actions[position];
	}

	public void setTriggerParameter(int trigger, int param, ITriggerParameter p) {
		triggerParameters[trigger][param] = p;
	}

	public void setActionParameter(int action, int param, IActionParameter p) {
		actionParameters[action][param] = p;
	}

	public ITriggerParameter getTriggerParameter(int trigger, int param) {
		return triggerParameters[trigger][param];
	}

	public IActionParameter getActionParameter(int action, int param) {
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
			expansions.put(expansion, expansion.makeController(pipe.container));
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

		for (PipeWire wire : PipeWire.VALUES) {
			data.setBoolean("wireState[" + wire.ordinal() + "]", broadcastSignal.get(wire.ordinal()));
		}

		data.setByte("redstoneOutput", (byte) redstoneOutput);
	}

	public void readStatementsFromNBT(NBTTagCompound data) {
		for (int i = 0; i < material.numSlots; ++i) {
			if (data.hasKey("trigger[" + i + "]")) {
				triggers[i] = (ITrigger) StatementManager.statements.get(data.getString("trigger[" + i + "]"));
			}

			if (data.hasKey("action[" + i + "]")) {
				actions[i] = (IAction) StatementManager.statements.get(data.getString("action[" + i + "]"));
			}

			// This is for legacy trigger loading
			if (data.hasKey("triggerParameters[" + i + "]")) {
				triggerParameters[i][0] = new TriggerParameterItemStack();
				triggerParameters[i][0].readFromNBT(data.getCompoundTag("triggerParameters[" + i + "]"));
			}

			for (int j = 0; j < material.numTriggerParameters; ++j) {
				if (data.hasKey("triggerParameters[" + i + "][" + j + "]")) {
					NBTTagCompound cpt = data.getCompoundTag("triggerParameters[" + i + "][" + j + "]");
					triggerParameters[i][j] = (ITriggerParameter) StatementManager.createParameter(cpt.getString("kind"));
					triggerParameters[i][j].readFromNBT(cpt);
				}
			}

			for (int j = 0; j < material.numActionParameters; ++j) {
				if (data.hasKey("actionParameters[" + i + "][" + j + "]")) {
					NBTTagCompound cpt = data.getCompoundTag("actionParameters[" + i + "][" + j + "]");
					actionParameters[i][j] = (IActionParameter) StatementManager.createParameter(cpt.getString("kind"));
					actionParameters[i][j].readFromNBT(cpt);
				}
			}
		}
	}
	
	public boolean verifyGateStatements() {
		List<ITrigger> triggerList = getAllValidTriggers();
		List<IAction> actionList = getAllValidActions();
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
		
		return !warning;
	}
	
	public void readFromNBT(NBTTagCompound data) {
		readStatementsFromNBT(data);

		for (PipeWire wire : PipeWire.VALUES) {
			broadcastSignal.set(wire.ordinal(), data.getBoolean("wireState[" + wire.ordinal() + "]"));
		}

		redstoneOutput = data.getByte("redstoneOutput");
	}

	// GUI
	public void openGui(EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			player.openGui(BuildCraftTransport.instance, GuiIds.GATES, pipe.container.getWorldObj(), pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
			((ContainerGateInterface) player.openContainer).setGate(direction.ordinal());
		}
	}

	/**
	 *  This code is aimed at being active on the client only, and moves
	 *  the internal position of the gate. There's no need to do that
	 *  or to synchronize that with the server as this is only for animation.
	 */
	public void updatePulse () {
		if (pipe.container.renderState.gateMatrix.isGatePulsing(direction) || pulseStage > 0.11F) {
			// if it is moving, or is still in a moved state, then complete
			// the current movement
			pulseStage = (pulseStage + 0.01F) % 1F;
		} else {
			pulseStage = 0;
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

		/* for (ForgeDirection ioSide : ForgeDirection.VALID_DIRECTIONS) {
			pipe.transport.allowInput(ioSide, true);
			pipe.transport.allowOutput(ioSide, true);
		} */
		
		boolean wasActive = activeActions.size() > 0;

		BitSet temp = prevBroadcastSignal;
		temp.clear();
		prevBroadcastSignal = broadcastSignal;
		broadcastSignal = temp;

		// Tell the gate to prepare for resolving actions. (Disable pulser)
		startResolution();

		int [] actionGroups = new int [] {0, 1, 2, 3, 4, 5, 6, 7};

		for (int i = 0; i < MAX_STATEMENTS; ++i) {
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
						}
					}

					if (sameParams) {
						actionGroups[i] = j;
					}
				}
			}
		}

		// Computes the actions depending on the triggers
		for (int it = 0; it < MAX_STATEMENTS; ++it) {
			actionsState[it] = ActionActiveState.Deactivated;

			ITrigger trigger = triggers[it];
			ITriggerParameter[] parameter = triggerParameters[it];

			if (trigger != null) {
				if (isTriggerActive(trigger, parameter)) {
					actionsState[it] = ActionActiveState.Partial;
				}
			}
		}

		activeActions = new ArrayList<ActionSlot>();

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

				ActionSlot slot = new ActionSlot();
				slot.action = actions[it];
				slot.parameters = actionParameters[it];
				activeActions.add(slot);
			}

			if (logic == GateLogic.OR && actionsState[it] == ActionActiveState.Partial) {
				actionsState[it] = ActionActiveState.Activated;
			}
		}

		// Activate the actions
		for (ActionSlot slot : activeActions) {
			IAction action = slot.action;
			action.actionActivate(this, slot.parameters);

			// TODO: A lot of the code below should be removed in favor
			// of calls to actionActivate

			// Custom gate actions take precedence over defaults.
			if (resolveAction(action)) {
				continue;
			}

			if (action instanceof ActionRedstoneOutput || action instanceof ActionRedstoneFaderOutput) {
				if (slot.parameters != null && slot.parameters.length >= 1 &&
						slot.parameters[0] instanceof StatementParameterRedstoneGateSideOnly &&
						((StatementParameterRedstoneGateSideOnly) slot.parameters[0]).isOn) {
					redstoneOutputSide = (action instanceof ActionRedstoneFaderOutput) ? ((ActionRedstoneFaderOutput) action).level : 15;
				} else {
					redstoneOutput = (action instanceof ActionRedstoneFaderOutput) ? ((ActionRedstoneFaderOutput) action).level : 15;
					if (redstoneOutput > redstoneOutputSide) {
						redstoneOutputSide = redstoneOutput;
					}
				}
			} else {
				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
					TileEntity tile = pipe.container.getTile(side);
					if (tile instanceof IActionReceptor) {
						IActionReceptor recept = (IActionReceptor) tile;
						recept.actionActivated(action, slot.parameters);
					}
				}
			}
		}

		pipe.actionsActivated(activeActions);

		if (oldRedstoneOutput != redstoneOutput || oldRedstoneOutputSide != redstoneOutputSide) {
			pipe.updateNeighbors(true);
		}

		if (!prevBroadcastSignal.equals(broadcastSignal)) {
			pipe.updateSignalState();
		}

		boolean isActive = activeActions.size() > 0;

		if (wasActive != isActive) {
			pipe.container.scheduleRenderUpdate();
		}
	}

	public boolean resolveAction(IAction action) {
		for (GateExpansionController expansion : expansions.values()) {
			if (expansion.resolveAction(action)) {
				return true;
			}
		}
		return false;
	}

	public boolean isTriggerActive(ITrigger trigger, ITriggerParameter[] parameters) {
		if (trigger == null) {
			return false;
		}

		if (trigger.isTriggerActive(this, parameters)) {
			return true;
		}

		// TODO: This can probably be refactored with regular triggers instead
		// of yet another system.
		for (GateExpansionController expansion : expansions.values()) {
			if (expansion.isTriggerActive(trigger, parameters[0])) {
				return true;
			}
		}

		return false;
	}

	// TRIGGERS
	public void addTriggers(List<ITrigger> list) {
		for (PipeWire wire : PipeWire.VALUES) {
			if (pipe.wireSet[wire.ordinal()] && material.ordinal() >= wire.ordinal()) {
				list.add(BuildCraftTransport.triggerPipeWireActive[wire.ordinal()]);
				list.add(BuildCraftTransport.triggerPipeWireInactive[wire.ordinal()]);
			}
		}

		for (GateExpansionController expansion : expansions.values()) {
			expansion.addTriggers(list);
		}
	}
	
	public List<ITrigger> getAllValidTriggers() {
		ArrayList<ITrigger> allTriggers = new ArrayList<ITrigger>(64);
		allTriggers.addAll(StatementManager.getPipeTriggers(pipe.container));
		
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = pipe.container.getTile(o);
			Block block = pipe.container.getBlock(o);
			allTriggers.addAll(StatementManager.getNeighborTriggers(o, block, tile));
		}
		
		return allTriggers;
	}

	// ACTIONS
	public void addActions(List<IAction> list) {
		for (PipeWire wire : PipeWire.VALUES) {
			if (pipe.wireSet[wire.ordinal()] && material.ordinal() >= wire.ordinal()) {
				list.add(BuildCraftTransport.actionPipeWire[wire.ordinal()]);
			}
		}

		for (GateExpansionController expansion : expansions.values()) {
			expansion.addActions(list);
		}
	}
	
	public List<IAction> getAllValidActions() {
		ArrayList<IAction> allActions = new ArrayList<IAction>(64);
		allActions.addAll(StatementManager.getPipeActions(pipe.container));
		
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = pipe.container.getTile(o);
			Block block = pipe.container.getBlock(o);
			allActions.addAll(StatementManager.getNeighborActions(o, block, tile));
		}
		
		return allActions;
	}
	
	@Override
	public void setPulsing(boolean pulsing) {
		if (pulsing != isPulsing) {
			isPulsing = pulsing;
			pipe.container.scheduleRenderUpdate();
		}
	}

	public float getPulseStage() {
		return pulseStage;
	}

	public void broadcastSignal(PipeWire color) {
		broadcastSignal.set(color.ordinal());
	}

	@Override
	public IPipe getPipe() {
		return pipe;
	}

	@Override
	public ForgeDirection getSide() {
		return direction;
	}
}
