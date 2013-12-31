package buildcraft.transport;

import buildcraft.api.transport.PipeWire;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;
import buildcraft.core.GuiIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.ActionRedstoneOutput;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.gates.ITileTrigger;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.triggers.ActionRedstoneFaderOutput;
import buildcraft.transport.triggers.ActionSignalOutput;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public final class Gate {

	public final Pipe pipe;
	public final GateMaterial material;
	public final GateLogic logic;
	public final BiMap<IGateExpansion, GateExpansionController> expansions = HashBiMap.create();
	public ITrigger[] triggers = new ITrigger[8];
	public ITriggerParameter[] triggerParameters = new ITriggerParameter[8];
	public IAction[] actions = new IAction[8];
	public boolean broadcastSignal[] = new boolean[4];
	public int redstoneOutput = 0;

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
		if (!expansions.containsKey(expansion))
			expansions.put(expansion, expansion.makeController(pipe.container));
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
		data.setByte("redstoneOutput", (byte) redstoneOutput);
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
		redstoneOutput = data.getByte("redstoneOutput");
	}

	// GUI
	public void openGui(EntityPlayer player) {
		if (!CoreProxy.proxy.isRenderWorld(player.worldObj)) {
			player.openGui(BuildCraftTransport.instance, GuiIds.GATES, pipe.container.worldObj, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
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
		for (boolean b : broadcastSignal) {
			if (b)
				return true;
		}
		return redstoneOutput > 0;
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
		boolean[] oldBroadcastSignal = broadcastSignal;

		redstoneOutput = 0;
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
					redstoneOutput = 15;
				} else if (action instanceof ActionRedstoneFaderOutput) {
					redstoneOutput = ((ActionRedstoneFaderOutput) action).level;
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

		if (oldRedstoneOutput != redstoneOutput) {
			if (redstoneOutput == 0 ^ oldRedstoneOutput == 0)
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
		for (GateExpansionController expansion : expansions.values()) {
			if (expansion.resolveAction(action, count))
				return true;
		}
		return false;
	}

	public boolean isNearbyTriggerActive(ITrigger trigger, ITriggerParameter parameter) {
		if (trigger == null)
			return false;

		if (trigger instanceof IPipeTrigger)
			return ((IPipeTrigger) trigger).isTriggerActive(pipe, parameter);

		if (trigger instanceof ITileTrigger) {
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = pipe.container.getTile(o);
				if (tile != null && !(tile instanceof TileGenericPipe)) {
					if (((ITileTrigger) trigger).isTriggerActive(o.getOpposite(), tile, parameter))
						return true;
				}
			}
			return false;
		}

		for (GateExpansionController expansion : expansions.values()) {
			if (expansion.isTriggerActive(trigger, parameter))
				return true;
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
}
