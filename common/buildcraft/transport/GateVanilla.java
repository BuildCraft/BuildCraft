package buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.transport.IPipe;
import buildcraft.core.DefaultProps;
import buildcraft.core.GuiIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.StringUtils;
import buildcraft.core.utils.Utils;
import buildcraft.transport.pipes.PipePowerWood;
import buildcraft.transport.triggers.ActionEnergyPulser;
import buildcraft.transport.triggers.ActionSingleEnergyPulse;

public class GateVanilla extends Gate {

	private EnergyPulser pulser;

	public GateVanilla(Pipe pipe) {
		super(pipe);
	}

	public GateVanilla(Pipe pipe, ItemStack stack) {
		super(pipe, stack);

		if (stack.itemID == BuildCraftTransport.pipeGateAutarchic.itemID) {
			addEnergyPulser(pipe);
		}
	}

	// / SAVING & LOADING
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (pulser != null) {
			NBTTagCompound nbttagcompoundC = new NBTTagCompound();
			pulser.writeToNBT(nbttagcompoundC);
			nbttagcompound.setTag("Pulser", nbttagcompoundC);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		// Load pulser if any
		if (nbttagcompound.hasKey("Pulser")) {
			NBTTagCompound nbttagcompoundP = nbttagcompound.getCompoundTag("Pulser");
			addEnergyPulser(pipe);
			pulser.readFromNBT(nbttagcompoundP);
		}

	}

	// GUI
	@Override
	public void openGui(EntityPlayer player) {
		if (!CoreProxy.proxy.isRenderWorld(player.worldObj)) {
			player.openGui(BuildCraftTransport.instance, GuiIds.GATES, pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
		}
	}

	// / UPDATING
	@Override
	public void update() {
		if (hasPulser()) {
			pulser.update();
		}
	}

	// / INFORMATION
	public boolean hasPulser() {
		return pulser != null;
	}

	@Override
	public String getName() {
		switch (kind) {
		case Single:
			return StringUtils.localize("item.pipeGate.0");
		case AND_2:
			return StringUtils.localize("item.pipeGate.1");
		case AND_3:
			return StringUtils.localize("item.pipeGate.3");
		case AND_4:
			return StringUtils.localize("item.pipeGate.5");
		case OR_2:
			return StringUtils.localize("item.pipeGate.2");
		case OR_3:
			return StringUtils.localize("item.pipeGate.4");
		case OR_4:
			return StringUtils.localize("item.pipeGate.6");
		default:
			return "";
		}

	}

	@Override
	public GateConditional getConditional() {
		if (kind == GateKind.OR_2 || kind == GateKind.OR_3 || kind == GateKind.OR_4)
			return GateConditional.OR;
		else if (kind == GateKind.AND_2 || kind == GateKind.AND_3 || kind == GateKind.AND_4)
			return GateConditional.AND;
		else
			return GateConditional.None;
	}

	/**
	 * Tries to add an energy pulser to gates that accept energy.
	 *
	 * @param pipe
	 * @return
	 */
	private boolean addEnergyPulser(Pipe pipe) {
		if (!(pipe instanceof IPowerReceptor) || pipe instanceof PipePowerWood) {
			pulser = new EnergyPulser(null);
			return false;
		}
		pulser = new EnergyPulser((IPowerReceptor) pipe);

		return true;
	}

	/**
	 * Drops a gate item of the specified kind.
	 *
	 * @param kind
	 * @param world
	 * @param i
	 * @param j
	 * @param k
	 */
	@Override
	public void dropGate(World world, int i, int j, int k) {

		int gateDamage = 0;
		switch (kind) {
		case Single:
			gateDamage = 0;
			break;
		case AND_2:
			gateDamage = 1;
			break;
		case OR_2:
			gateDamage = 2;
			break;
		case AND_3:
			gateDamage = 3;
			break;
		case OR_3:
			gateDamage = 4;
			break;
		case AND_4:
			gateDamage = 5;
			break;
		case OR_4:
		default:
			gateDamage = 6;
			break;
		}

		Item gateItem;
		if (hasPulser()) {
			gateItem = BuildCraftTransport.pipeGateAutarchic;
		} else {
			gateItem = BuildCraftTransport.pipeGate;
		}

		Utils.dropItems(world, new ItemStack(gateItem, 1, gateDamage), i, j, k);

	}

	// / ACTIONS
	@Override
	public void addActions(LinkedList<IAction> list) {

		if (pipe.wireSet[IPipe.WireColor.Red.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_2.ordinal()) {
			list.add(BuildCraftTransport.actionRedSignal);
		}

		if (pipe.wireSet[IPipe.WireColor.Blue.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_3.ordinal()) {
			list.add(BuildCraftTransport.actionBlueSignal);
		}

		if (pipe.wireSet[IPipe.WireColor.Green.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_4.ordinal()) {
			list.add(BuildCraftTransport.actionGreenSignal);
		}

		if (pipe.wireSet[IPipe.WireColor.Yellow.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_4.ordinal()) {
			list.add(BuildCraftTransport.actionYellowSignal);
		}

		if (hasPulser()) {
			list.add(BuildCraftTransport.actionEnergyPulser);
			list.add(BuildCraftTransport.actionSingleEnergyPulse);
		}

	}

	@Override
	public void startResolution() {
		if (hasPulser()) {
			pulser.disablePulse();
		}
	}

	@Override
	public boolean resolveAction(IAction action, int count) {

		if (action instanceof ActionEnergyPulser) {
			pulser.enablePulse(count);
			return true;
		} else if (action instanceof ActionSingleEnergyPulse) {
			pulser.enableSinglePulse(count);
			return true;
		}
		return false;
	}

	// / TRIGGERS
	@Override
	public void addTrigger(LinkedList<ITrigger> list) {

		if (pipe.wireSet[IPipe.WireColor.Red.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_2.ordinal()) {
			list.add(BuildCraftTransport.triggerRedSignalActive);
			list.add(BuildCraftTransport.triggerRedSignalInactive);
		}

		if (pipe.wireSet[IPipe.WireColor.Blue.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_3.ordinal()) {
			list.add(BuildCraftTransport.triggerBlueSignalActive);
			list.add(BuildCraftTransport.triggerBlueSignalInactive);
		}

		if (pipe.wireSet[IPipe.WireColor.Green.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_4.ordinal()) {
			list.add(BuildCraftTransport.triggerGreenSignalActive);
			list.add(BuildCraftTransport.triggerGreenSignalInactive);
		}

		if (pipe.wireSet[IPipe.WireColor.Yellow.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_4.ordinal()) {
			list.add(BuildCraftTransport.triggerYellowSignalActive);
			list.add(BuildCraftTransport.triggerYellowSignalInactive);
		}

	}

	// / ICONS
	@Override
	public final int getTextureIconIndex(boolean isSignalActive) {

		boolean isGateActive = isSignalActive;
		if (hasPulser() && pulser.isActive()) {
			isGateActive = true;
		}
	
		if (!hasPulser()){
			switch (kind){
				case None: return 0;
				case Single: return isGateActive ? GateIconProvider.Gate_Lit : GateIconProvider.Gate_Dark;
				case AND_2: return isGateActive ? GateIconProvider.Gate_Iron_And_Lit : GateIconProvider.Gate_Iron_And_Dark;
				case OR_2: return isGateActive ? GateIconProvider.Gate_Iron_Or_Lit : GateIconProvider.Gate_Iron_Or_Dark;
				case AND_3: return isGateActive ? GateIconProvider.Gate_Gold_And_Lit : GateIconProvider.Gate_Gold_And_Dark;
				case OR_3: return isGateActive ? GateIconProvider.Gate_Gold_Or_Lit : GateIconProvider.Gate_Gold_Or_Dark;
				case AND_4: return isGateActive ? GateIconProvider.Gate_Diamond_And_Lit : GateIconProvider.Gate_Diamond_And_Dark;
				case OR_4: return isGateActive ? GateIconProvider.Gate_Diamond_Or_Lit : GateIconProvider.Gate_Diamond_Or_Dark;
			}
		} else {
			switch (kind){
				case None: return 0; 
				case Single: return isGateActive ? GateIconProvider.Gate_Autarchic_Lit : GateIconProvider.Gate_Autarchic_Dark;
				case AND_2: return isGateActive ? GateIconProvider.Gate_Autarchic_Iron_And_Lit : GateIconProvider.Gate_Autarchic_Iron_And_Dark;
				case OR_2: return isGateActive ? GateIconProvider.Gate_Autarchic_Iron_Or_Lit : GateIconProvider.Gate_Autarchic_Iron_Or_Dark;
				case AND_3: return isGateActive ? GateIconProvider.Gate_Autarchic_Gold_And_Lit : GateIconProvider.Gate_Autarchic_Gold_And_Dark;
				case OR_3: return isGateActive ? GateIconProvider.Gate_Autarchic_Gold_Or_Lit : GateIconProvider.Gate_Autarchic_Gold_Or_Dark;
				case AND_4: return isGateActive ? GateIconProvider.Gate_Autarchic_Diamond_And_Lit : GateIconProvider.Gate_Autarchic_Diamond_And_Dark;
				case OR_4: return isGateActive ? GateIconProvider.Gate_Autarchic_Diamond_Or_Lit : GateIconProvider.Gate_Autarchic_Diamond_Or_Dark;
			}
		}

		return 0;
	}

	@Override
	public String getGuiFile() {
		if (kind == GateKind.Single)
			return DefaultProps.TEXTURE_PATH_GUI + "/gate_interface_1.png";
		else if (kind == GateKind.AND_2 || kind == GateKind.OR_2)
			return DefaultProps.TEXTURE_PATH_GUI + "/gate_interface_2.png";
		else if (kind == GateKind.AND_3 || kind == GateKind.OR_3)
			return DefaultProps.TEXTURE_PATH_GUI + "/gate_interface_3.png";
		else
			return DefaultProps.TEXTURE_PATH_GUI + "/gate_interface_4.png";
	}

}
