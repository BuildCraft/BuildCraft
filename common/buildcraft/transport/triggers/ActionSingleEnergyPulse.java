package buildcraft.transport.triggers;

import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.StringUtils;

public class ActionSingleEnergyPulse extends BCAction {

	public ActionSingleEnergyPulse() {
		super("buildcraft:pulsar.single", "buildcraft.pulser.single");
	}

	@Override
	public int getIconIndex() {
		return ActionTriggerIconProvider.Trigger_Machine_Active;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.pulsar.single");
	}
}
