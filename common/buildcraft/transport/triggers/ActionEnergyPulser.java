package buildcraft.transport.triggers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.Icon;
import buildcraft.BuildCraftTransport;
import buildcraft.core.DefaultProps;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;

public class ActionEnergyPulser extends BCAction {

	public ActionEnergyPulser(int id) {
		super(id);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getTexture() {
		return getIconProvider().getIcon(ActionTriggerIconProvider.Trigger_Machine_Active);
	}

	@Override
	public String getDescription() {
		return "Energy Pulser";
	}

}
