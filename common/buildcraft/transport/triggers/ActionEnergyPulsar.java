package buildcraft.transport.triggers;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.StringUtils;

public class ActionEnergyPulsar extends BCAction {

	private Icon icon;
	
	public ActionEnergyPulsar() {
		super("buildcraft:pulsar.constant", "buildcraft.pulser.constant");
	}

	@Override
	public Icon getIcon() {
		return icon;
	}
	
	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.pulsar.constant");
	}
	
	@Override
	public void registerIcons(IconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_pulsar");
	}

}
