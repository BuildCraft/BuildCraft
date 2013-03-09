package buildcraft.transport.triggers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.Icon;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.Action;
import buildcraft.core.DefaultProps;
import buildcraft.transport.IconItemConstants;

public class ActionEnergyPulser extends Action {

	public ActionEnergyPulser(int id) {
		super(id);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getTexture() {
		return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_Machine_Active];
	}

	@Override
	public String getDescription() {
		return "Energy Pulser";
	}

}
