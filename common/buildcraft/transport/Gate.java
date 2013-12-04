package buildcraft.transport;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadArrays;
import buildcraft.core.triggers.ActionRedstoneOutput;
import buildcraft.transport.triggers.ActionSignalOutput;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public abstract class Gate {

	public static enum GateKind {

		None, Single, AND_2, OR_2, AND_3, OR_3, AND_4, OR_4;

		public static GateKind getKindFromDamage(ItemStack itemstack) {
			switch (itemstack.getItemDamage()) {
				case 0:
					return Single;
				case 1:
					return AND_2;
				case 2:
					return OR_2;
				case 3:
					return AND_3;
				case 4:
					return OR_3;
				case 5:
					return AND_4;
				case 6:
					return OR_4;
				default:
					return None;
			}
		}
	}

	public static enum GateConditional {

		None, AND, OR
	}
	protected Pipe pipe;
	public GateKind kind;
	public ITrigger[] triggers = new ITrigger[8];
	public ITriggerParameter[] triggerParameters = new ITriggerParameter[8];
	public IAction[] actions = new IAction[8];
	public boolean broadcastSignal[] = new boolean[4];
	public boolean broadcastRedstone = false;

	// / CONSTRUCTOR
	public Gate(Pipe pipe) {
		this.pipe = pipe;
	}

	public Gate(Pipe pipe, ItemStack stack) {

		this.pipe = pipe;
		kind = GateKind.getKindFromDamage(stack);
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
		data.setInteger("Kind", kind.ordinal());

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
		kind = Gate.GateKind.values()[data.getInteger("Kind")];

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

	// / SMP
	public PacketPayload toPayload() {
		PacketPayloadArrays payload = new PacketPayloadArrays(1, 0, 0);
		payload.intPayload[0] = kind.ordinal();
		return payload;
	}

	// GUI
	public abstract void openGui(EntityPlayer player);

	// / UPDATING
	public abstract void update();

	public abstract ItemStack getGateItem();

	public void dropGate() {
		pipe.dropItem(getGateItem());
	}

	public void resetGate() {
		if (broadcastRedstone) {
			broadcastRedstone = false;
			pipe.updateNeighbors(true);
		}
	}

	// / INFORMATION
	public abstract String getName();

	public abstract GateConditional getConditional();

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

	public abstract void startResolution();

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
				} else if (getConditional() == GateConditional.AND) {
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

	public abstract boolean resolveAction(IAction action, int count);

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
	public abstract void addTrigger(LinkedList<ITrigger> list);

	// / ACTIONS
	public abstract void addActions(LinkedList<IAction> list);

	// / TEXTURES
	public abstract int getTextureIconIndex(boolean isSignalActive);

	public abstract ResourceLocation getGuiFile();

	public int getGuiHeight() {
		return 207;
	}

	public static boolean isGateItem(ItemStack stack) {
		return stack.itemID == BuildCraftTransport.pipeGate.itemID || stack.itemID == BuildCraftTransport.pipeGateAutarchic.itemID;
	}

	public static Gate makeGate(Pipe pipe, NBTTagCompound data) {
		Gate gate = new GateVanilla(pipe);
		gate.readFromNBT(data);
		return gate;
	}

	public static Gate makeGate(Pipe pipe, ItemStack stack) {
		return new GateVanilla(pipe, stack);
	}
}
