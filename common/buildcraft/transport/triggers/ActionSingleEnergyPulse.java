package buildcraft.transport.triggers;

import buildcraft.api.gates.Action;
import buildcraft.core.DefaultProps;

public class ActionSingleEnergyPulse extends Action {

	public ActionSingleEnergyPulse(int id) {
		super(id);
	}

	@Override
	public int getIndexInTexture() {
		return 4 * 16 + 0;
	}

	@Override
	public String getTexture() {
		return DefaultProps.TEXTURE_TRIGGERS;
	}

	@Override
	public String getDescription() {
		return "Single Energy Pulse";
	}

}
