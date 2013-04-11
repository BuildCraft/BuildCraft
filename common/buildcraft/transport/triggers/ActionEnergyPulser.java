package buildcraft.transport.triggers;

import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;

public class ActionEnergyPulser extends BCAction {

	public ActionEnergyPulser(int id) {
		super(id);
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
