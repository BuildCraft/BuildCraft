package buildcraft.transport;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;
import buildcraft.api.transport.IPipe;
import buildcraft.core.GuiIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.ActionRedstoneOutput;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.api.gates.GateExpansionController;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.triggers.ActionSignalOutput;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public final class Gate {

	public final Pipe pipe;
	public final GateMaterial material;
	public final GateLogic logic;
	public final Set<GateExpansionController> expansions = new HashSet<GateExpansionController>();
	public ITrigger[] triggers = new ITrigger[8];
	public ITriggerParameter[] triggerParameters = new ITriggerParameter[8];
	public IAction[] actions = new IAction[8];
	public boolean broadcastSignal[] = new boolean[4];
	public boolean broadcastRedstone = false;

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

	// / SAVING & LOADING
	public void writeToNBT(NBTTagCompound data) {
		data.setString("material", material.name());
		data.setString("logic", logic.name());

		for (int i = 0; i < 8; ++i) {
			if (triggers[i] != null)
				data.setString("trigger[" + i + "]", triggers[i].getUniqueTag());
			if (actions[i] != null)
				data.setString("action[" + i + "]", actions[i].getUniqueTag());
			if (triggerParameters[i] != null) {
				NBTTagCompound cpt = new NBTTagCompound();
				triggerParameters[i].writeToNBT(cpt);
				data.setTag("triggerParameters[" + i + "]", cpt);
			}
		}

		for (int i = 0; i < 4; ++i) {
			data.setBoolean("wireState[" + i + "]", broadcastSignal[i]);
		}
		data.setBoolean("redstoneState", broadcastRedstone);
	}

	public void readFromNBT(NBTTagCompound data) {
		for (int i = 0; i < 8; ++i) {
			if (data.hasKey("trigger[" + i + "]"))
				triggers[i] = ActionManager.triggers.get(data.getString("trigger[" + i + "]"));
			if (data.hasKey("action[" + i + "]"))
				actions[i] = ActionManager.actions.get(data.getString("action[" + i + "]"));
			if (data.hasKey("triggerParameters[" + i + "]")) {
				triggerParameters[i] = new TriggerParameter();
				triggerParameters[i].readFromNBT(data.getCompoundTag("triggerParameters[" + i + "]"));
			}
		}

		for (int i = 0; i < 4; ++i) {
			broadcastSignal[i] = data.getBoolean("wireState[" + i + "]");
		}
		broadcastRedstone = data.getBoolean("redstoneState");
	}

	// GUI
	public void openGui(EntityPlayer player) {
		if (!CoreProxy.proxy.isRenderWorld(player.worldObj)) {
			player.openGui(BuildCraftTransport.instance, GuiIds.GATES, pipe.container.worldObj, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
		}
	}

	// / UPDATING
	public void tick() {
		for (GateExpansionController expansion : expansions) {
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
		if (broadcastRedstone) {
			broadcastRedstone = false;
			pipe.updateNeighbors(true);
		}
	}

	public boolean isGateActive() {
		for (boolean b : broadcastSignal) {
			if (b)
				return true;
		}
		return broadcastRedstone;
	}

	public boolean isEmittingRedstone() {
		return broadcastRedstone;
	}

	public void startResolution() {
		for (GateExpansionController expansion : expansions) {
			expansion.startResolution();
		}
	}

	public void resolveActions() {
		boolean oldBroadcastRedstone = broadcastRedstone;
		boolean[] oldBroadcastSignal = broadcastSignal;

		broadcastRedstone = false;
		broadcastSignal = new boolean[4];

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
					broadcastRedstone = true;
				} else if (action instanceof ActionSignalOutput) {
					broadcastSignal[((ActionSignalOutput) action).color.ordinal()] = true;
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

		if (oldBroadcastRedstone != broadcastRedstone) {
			pipe.container.scheduleRenderUpdate();
			pipe.updateNeighbors(true);
		}

		for (int i = 0; i < oldBroadcastSignal.length; ++i) {
			if (oldBroadcastSignal[i] != broadcastSignal[i]) {
				pipe.container.scheduleRenderUpdate();
				pipe.updateSignalState();
				break;
			}
		}
	}

	public boolean resolveAction(IAction action, int count) {
		for (GateExpansionController expansion : expansions) {
			if (expansion.resolveAction(action, count))
				return true;
		}
		return false;
	}

	public boolean isNearbyTriggerActive(ITrigger trigger, ITriggerParameter parameter) {
		if (trigger instanceof ITriggerPipe)
			return ((ITriggerPipe) trigger).isTriggerActive(pipe, parameter);
		else if (trigger != null) {
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = pipe.container.getTile(o);

				if (tile != null && !(tile instanceof TileGenericPipe)) {
					if (trigger.isTriggerActive(o.getOpposite(), tile, parameter))
						return true;
				}
			}
		}

		return false;
	}

	// / TRIGGERS
	public void addTrigger(List<ITrigger> list) {

		if (pipe.wireSet[IPipe.WireColor.Red.ordinal()] && material == GateMaterial.IRON) {
			list.add(BuildCraftTransport.triggerRedSignalActive);
			list.add(BuildCraftTransport.triggerRedSignalInactive);
		}

		if (pipe.wireSet[IPipe.WireColor.Blue.ordinal()] && material == GateMaterial.IRON) {
			list.add(BuildCraftTransport.triggerBlueSignalActive);
			list.add(BuildCraftTransport.triggerBlueSignalInactive);
		}

		if (pipe.wireSet[IPipe.WireColor.Green.ordinal()] && material == GateMaterial.GOLD) {
			list.add(BuildCraftTransport.triggerGreenSignalActive);
			list.add(BuildCraftTransport.triggerGreenSignalInactive);
		}

		if (pipe.wireSet[IPipe.WireColor.Yellow.ordinal()] && material == GateMaterial.DIAMOND) {
			list.add(BuildCraftTransport.triggerYellowSignalActive);
			list.add(BuildCraftTransport.triggerYellowSignalInactive);
		}

		for (GateExpansionController expansion : expansions) {
			expansion.addTriggers(list);
		}

//		if (pipe.gate.kind == GateKind.AND_5 || pipe.gate.kind == GateKind.OR_5) {
//			list.add(BuildCraftTransport.triggerTimerShort);
//			list.add(BuildCraftTransport.triggerTimerMedium);
//			list.add(BuildCraftTransport.triggerTimerLong);
//		}

	}

	// / ACTIONS
	public void addActions(List<IAction> list) {
		if (pipe.wireSet[IPipe.WireColor.Red.ordinal()] && material == GateMaterial.IRON)
			list.add(BuildCraftTransport.actionRedSignal);

		if (pipe.wireSet[IPipe.WireColor.Blue.ordinal()] && material == GateMaterial.IRON)
			list.add(BuildCraftTransport.actionBlueSignal);

		if (pipe.wireSet[IPipe.WireColor.Green.ordinal()] && material == GateMaterial.GOLD)
			list.add(BuildCraftTransport.actionGreenSignal);

		if (pipe.wireSet[IPipe.WireColor.Yellow.ordinal()] && material == GateMaterial.DIAMOND)
			list.add(BuildCraftTransport.actionYellowSignal);


		for (GateExpansionController expansion : expansions) {
			expansion.addActions(list);
		}
	}
}
