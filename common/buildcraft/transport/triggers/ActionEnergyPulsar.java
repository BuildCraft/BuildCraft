package buildcraft.transport.triggers;

import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.StringUtils;

public class ActionEnergyPulsar extends BCAction {

	public ActionEnergyPulsar() {
		super("buildcraft:pulsar.constant", "buildcraft.pulser.constant");
	}

	@Override
	public int getIconIndex() {
		return ActionTriggerIconProvider.Action_Pulsar;
	}
	
	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.pulsar.constant");
	}

}
