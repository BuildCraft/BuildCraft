package buildcraft.transport.triggers;

import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.gates.IAction;
import buildcraft.core.DefaultProps;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;

public class ActionSingleEnergyPulse extends BCAction {

	public ActionSingleEnergyPulse(int id) {
		super(id);
	}

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getTexture() {
        return getIconProvider().getIcon(ActionTriggerIconProvider.Trigger_Machine_Active);
    }

	@Override
	public String getDescription() {
		return "Single Energy Pulse";
	}

}
