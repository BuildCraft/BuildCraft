package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.buildcraft.api.Action;

public class ActionEnergyPulser extends Action {

	public ActionEnergyPulser(int id) {
		super(id);
	}

	@Override
	public int getIndexInTexture () {
		return 4 * 16 + 0;
	}

	@Override
	public String getTexture() {
		return BuildCraftCore.triggerTextures;
	}

	@Override
	public String getDescription() {
		return "Energy Pulser";
	}

}
