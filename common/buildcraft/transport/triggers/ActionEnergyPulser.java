package buildcraft.transport.triggers;

import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;

public class ActionEnergyPulser extends BCAction {

	public ActionEnergyPulser(int legacyId) {
		super(legacyId, "buildcraft.pulser.constant");
	}

	@Override
	public int getIconIndex() {
		return ActionTriggerIconProvider.Trigger_Machine_Active;
	}
	
	@Override
	public String getDescription() {
		return "Energy Pulser";
	}

}
