package buildcraft.transport.triggers;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.StringUtils;

public class ActionSingleEnergyPulse extends BCAction {
	
	private IIcon icon;

	public ActionSingleEnergyPulse() {
		super("buildcraft:pulsar.single", "buildcraft.pulser.single");
	}

	@Override
	public IIcon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.pulsar.single");
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_single_pulsar");
	}

}
