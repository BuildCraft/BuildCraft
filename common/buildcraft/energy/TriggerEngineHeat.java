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
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCTrigger;

public class TriggerEngineHeat extends BCTrigger {

	public Engine.EnergyStage stage;

	public TriggerEngineHeat(int id, Engine.EnergyStage stage) {
		super(id);

		this.stage = stage;
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
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof TileEngine) {
			Engine engine = ((TileEngine) tile).engine;

			return engine != null && engine.getEnergyStage() == stage;
		}

		return false;
	}

	@Override
	public int getIconIndex() {
		switch (stage) {
		case Blue:
			return ActionTriggerIconProvider.Trigger_EngineHeat_Blue;
		case Green:
			return ActionTriggerIconProvider.Trigger_EngineHeat_Green;
		case Yellow:
			return ActionTriggerIconProvider.Trigger_EngineHeat_Yellow;
		default:
			return ActionTriggerIconProvider.Trigger_EngineHeat_Red;
		}
	}
}
