/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import net.minecraft.tileentity.TileEntity;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import buildcraft.core.DefaultProps;

public class TriggerEngineHeat extends Trigger {

	public Engine.EnergyStage stage;

	public TriggerEngineHeat(int id, Engine.EnergyStage stage) {
		super(id);

		this.stage = stage;
	}

	@Override
	public int getIndexInTexture() {
		switch (stage) {
		case Blue:
			return 1 * 16 + 0;
		case Green:
			return 1 * 16 + 1;
		case Yellow:
			return 1 * 16 + 2;
		default:
			return 1 * 16 + 3;
		}
	}

	@Override
	public String getDescription() {
		switch (stage) {
		case Blue:
			return "Engine Blue";
		case Green:
			return "Engine Green";
		case Yellow:
			return "Engine Yellow";
		default:
			return "Engine Red";
		}
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof TileEngine) {
			Engine engine = ((TileEngine) tile).engine;

			return engine != null && engine.getEnergyStage() == stage;
		}

		return false;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_TRIGGERS;
	}
}
