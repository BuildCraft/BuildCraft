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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.gates.ITileTrigger;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.GuiIds;
import buildcraft.core.triggers.ActionRedstoneOutput;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.triggers.ActionRedstoneFaderOutput;
import buildcraft.transport.triggers.ActionSignalOutput;

public final class Gate {

	public final Pipe pipe;
	public final GateMaterial material;
	public final GateLogic logic;
	public final BiMap<IGateExpansion, GateExpansionController> expansions = HashBiMap.create();
	public ITrigger[] triggers = new ITrigger[8];
	public ITriggerParameter[] triggerParameters = new ITriggerParameter[8];
	public IAction[] actions = new IAction[8];
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

	// / CONSTRUCTOR
	public Gate(Pipe pipe, GateMaterial material, GateLogic logic) {
		this.pipe = pipe;
		this.material = material;
		this.logic = logic;
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

	public void setTriggerParameter(int position, ITriggerParameter p) {
		triggerParameters[position] = p;
	}

	public ITriggerParameter getTriggerParameter(int position) {
		return triggerParameters[position];
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
			if (triggerParameters[i] != null) {
				NBTTagCompound cpt = new NBTTagCompound();
				triggerParameters[i].writeToNBT(cpt);
				data.setTag("triggerParameters[" + i + "]", cpt);
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
				triggers[i] = ActionManager.triggers.get(data.getString("trigger[" + i + "]"));
			}
			if (data.hasKey("action[" + i + "]")) {
				actions[i] = ActionManager.actions.get(data.getString("action[" + i + "]"));
			}
			if (data.hasKey("triggerParameters[" + i + "]")) {
				triggerParameters[i] = new TriggerParameter();
				triggerParameters[i].readFromNBT(data.getCompoundTag("triggerParameters[" + i + "]"));
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
		}
	}

	/**
	 *  This code is aimed at being active on the client only, and moves
	 *  the internal position of the gate. There's no need to do that
	 *  or to synchronize that with the server as this is only for animation.
	 */
	public void updatePulse () {
		if (pipe.container.renderState.isGatePulsing () || pulseStage > 0.11F) {
			// if it is moving, or is still in a moved state, then complete
			// the current movement
			pulseStage = (pulseStage + 0.01F) % 1F;
		} else {
			pulseStage = 0;
		}
	}

	// / UPDATING
	public void tick() {
		for (GateExpansionController expansion : expansions.values()) {
			expansion.tick();
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

		Map<IAction, Boolean> activeActions = new HashMap<IAction, Boolean>();
		Multiset<IAction> actionCount = HashMultiset.create();

		// Computes the actions depending on the triggers
		for (int it = 0; it < 8; ++it) {
			ITrigger trigger = triggers[it];
			IAction action = actions[it];
			ITriggerParameter parameter = triggerParameters[it];

			if (trigger != null && action != null) {
				actionCount.add(action);
				if (!activeActions.containsKey(action)) {
					activeActions.put(action, isNearbyTriggerActive(trigger, parameter));
				} else if (logic == GateLogic.AND) {
					activeActions.put(action, activeActions.get(action) && isNearbyTriggerActive(trigger, parameter));
				} else {
					activeActions.put(action, activeActions.get(action) || isNearbyTriggerActive(trigger, parameter));
				}
			}
		}

		// Activate the actions
		for (Map.Entry<IAction, Boolean> entry : activeActions.entrySet()) {
			if (entry.getValue()) {
				IAction action = entry.getKey();

				// Custom gate actions take precedence over defaults.
				if (resolveAction(action, actionCount.count(action))) {
					continue;
				}

				if (action instanceof ActionRedstoneOutput) {
					redstoneOutput = 15;
				} else if (action instanceof ActionRedstoneFaderOutput) {
					redstoneOutput = ((ActionRedstoneFaderOutput) action).level;
				} else if (action instanceof ActionSignalOutput) {
					broadcastSignal.set(((ActionSignalOutput) action).color.ordinal());
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

	public boolean resolveAction(IAction action, int count) {
		for (GateExpansionController expansion : expansions.values()) {
			if (expansion.resolveAction(action, count)) {
				return true;
			}
		}
		return false;
	}

	public boolean isNearbyTriggerActive(ITrigger trigger, ITriggerParameter parameter) {
		if (trigger == null) {
			return false;
		}

		if (trigger instanceof IPipeTrigger) {
			return ((IPipeTrigger) trigger).isTriggerActive(pipe, parameter);
		}

		if (trigger instanceof ITileTrigger) {
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = pipe.container.getTile(o);
				if (tile != null && !(tile instanceof TileGenericPipe) && pipe.hasGate(o)) {
					if (((ITileTrigger) trigger).isTriggerActive(o.getOpposite(), tile, parameter)) {
						return true;
					}
				}
			}
			return false;
		}

		for (GateExpansionController expansion : expansions.values()) {
			if (expansion.isTriggerActive(trigger, parameter)) {
				return true;
			}
		}

		return false;
	}

	// / TRIGGERS
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

	// / ACTIONS
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

	public void setPulsing (boolean pulsing) {
		if (pulsing != isPulsing) {
			isPulsing = pulsing;
			pipe.container.scheduleRenderUpdate();
		}
	}

	public float getPulseStage () {
		return pulseStage;
	}
}
