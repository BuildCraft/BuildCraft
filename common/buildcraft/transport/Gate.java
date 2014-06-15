/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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
import buildcraft.core.triggers.ActionRedstoneOutput;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.triggers.ActionRedstoneFaderOutput;

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

	public ActionState[] actionsState = new ActionState[MAX_STATEMENTS];

	public BitSet broadcastSignal = new BitSet(PipeWire.VALUES.length);
	public BitSet prevBroadcastSignal = new BitSet(PipeWire.VALUES.length);
	public int redstoneOutput = 0;

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
			actionsState[i] = ActionState.Deactivated;
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

		for (int i = 0; i < 8; ++i) {
			if (triggers[i] != null) {
				data.setString("trigger[" + i + "]", triggers[i].getUniqueTag());
			}

			if (actions[i] != null) {
				data.setString("action[" + i + "]", actions[i].getUniqueTag());
			}

			for (int j = 0; j < 3; ++j) {
				if (triggerParameters[i][j] != null) {
					NBTTagCompound cpt = new NBTTagCompound();
					cpt.setString("kind", StatementManager.getParameterKind(triggerParameters[i][j]));
					triggerParameters[i][j].writeToNBT(cpt);
					data.setTag("triggerParameters[" + i + "][" + j + "]", cpt);
				}
			}

			for (int j = 0; j < 3; ++j) {
				if (actionParameters[i][j] != null) {
					NBTTagCompound cpt = new NBTTagCompound();
					cpt.setString("kind", StatementManager.getParameterKind(actionParameters[i][j]));
					actionParameters[i][j].writeToNBT(cpt);
					data.setTag("actionParameters[" + i + "][" + j + "]", cpt);
				}
			}
		}

		for (PipeWire wire : PipeWire.VALUES) {
			data.setBoolean("wireState[" + wire.ordinal() + "]", broadcastSignal.get(wire.ordinal()));
		}

		data.setByte("redstoneOutput", (byte) redstoneOutput);
	}

	public void readFromNBT(NBTTagCompound data) {
		for (int i = 0; i < 8; ++i) {
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

			for (int j = 0; j < 3; ++j) {
				if (data.hasKey("triggerParameters[" + i + "][" + j + "]")) {
					NBTTagCompound cpt = data.getCompoundTag("triggerParameters[" + i + "][" + j + "]");
					triggerParameters[i][j] = (ITriggerParameter) StatementManager.createParameter(cpt
							.getString("kind"));
					triggerParameters[i][j].readFromNBT(cpt);
				}
			}

			for (int j = 0; j < 3; ++j) {
				if (data.hasKey("actionParameters[" + i + "][" + j + "]")) {
					NBTTagCompound cpt = data.getCompoundTag("actionParameters[" + i + "][" + j + "]");
					actionParameters[i][j] = (IActionParameter) StatementManager.createParameter(cpt.getString("kind"));
					actionParameters[i][j].readFromNBT(cpt);
				}
			}
		}

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
		for (GateExpansionController expansion : expansions.values()) {
			if (expansion.isActive()) {
				return true;
			}
		}
		return redstoneOutput > 0 || !broadcastSignal.isEmpty();
	}

	public boolean isGatePulsing() {
		return isPulsing;
	}

	public int getRedstoneOutput() {
		return redstoneOutput;
	}

	public void startResolution() {
		for (GateExpansionController expansion : expansions.values()) {
			expansion.startResolution();
		}
	}

	public void resolveActions() {
		int oldRedstoneOutput = redstoneOutput;
		redstoneOutput = 0;

		BitSet temp = prevBroadcastSignal;
		temp.clear();
		prevBroadcastSignal = broadcastSignal;
		broadcastSignal = temp;

		// Tell the gate to prepare for resolving actions. (Disable pulser)
		startResolution();

		int [] actionGroups = new int [] {0, 1, 2, 3, 4, 5, 6, 7};

		for (int i = 0; i < MAX_PARAMETERS; ++i) {
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
			actionsState[it] = ActionState.Deactivated;

			ITrigger trigger = triggers[it];
			ITriggerParameter[] parameter = triggerParameters[it];

			if (trigger != null) {
				boolean active = isTriggerActive(trigger, parameter);

				if (actionGroups[it] == it) {
					if (active) {
						actionsState[it] = ActionState.Activated;
					}
				} else {
					if (active && actionsState[actionGroups[it]] != ActionState.Activated) {
						actionsState[actionGroups[it]] = ActionState.Partial;
					} else if (!active && actionsState[actionGroups[it]] == ActionState.Activated) {
						actionsState[actionGroups[it]] = ActionState.Partial;
					}
				}
			}
		}

		// Activate the actions
		for (int it = 0; it < MAX_STATEMENTS; ++it) {
			if (actions[it] != null && actionGroups[it] == it && actionsState[it] == ActionState.Activated) {
				IAction action = actions[it];
				action.actionActivate(this, actionParameters[it]);

				// TODO: A lot of the code below should be removed in favor
				// of calls to actionActivate

				// Custom gate actions take precedence over defaults.
				if (resolveAction(action)) {
					continue;
				}

				if (action instanceof ActionRedstoneOutput) {
					redstoneOutput = 15;
				} else if (action instanceof ActionRedstoneFaderOutput) {
					redstoneOutput = ((ActionRedstoneFaderOutput) action).level;
				} else {
					for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
						TileEntity tile = pipe.container.getTile(side);
						if (tile instanceof IActionReceptor) {
							IActionReceptor recept = (IActionReceptor) tile;
							recept.actionActivated(action);
						}
					}
				}
			}
		}

		LinkedList<IAction> activeActions = new LinkedList<IAction>();

		for (int it = 0; it < MAX_STATEMENTS; ++it) {
			if (actionGroups[it] == it && actionsState[it] == ActionState.Activated) {
				activeActions.add(actions[it]);
			}
		}

		pipe.actionsActivated(activeActions);

		if (oldRedstoneOutput != redstoneOutput) {
			if (redstoneOutput == 0 ^ oldRedstoneOutput == 0) {
				pipe.container.scheduleRenderUpdate();
			}
			pipe.updateNeighbors(true);
		}

		if (!prevBroadcastSignal.equals(broadcastSignal)) {
			pipe.container.scheduleRenderUpdate();
			pipe.updateSignalState();
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
	public void addTrigger(List<ITrigger> list) {

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
