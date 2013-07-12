/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.TileEngine.EnergyStage;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TriggerEngineHeat extends BCTrigger {

	public EnergyStage stage;

	public TriggerEngineHeat(int id, EnergyStage stage) {
		super(id);

		this.stage = stage;
	}

	@Override
	public String getDescription() {
		switch (stage) {
			case BLUE:
				return StringUtils.localize("gate.engine.blue");
			case GREEN:
				return StringUtils.localize("gate.engine.green");
			case YELLOW:
				return StringUtils.localize("gate.engine.yellow");
			default:
				return StringUtils.localize("gate.engine.red");
		}
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof TileEngine) {
			TileEngine engine = ((TileEngine) tile);

			return engine.getEnergyStage() == stage;
		}

		return false;
	}

	@Override
	public int getIconIndex() {
		switch (stage) {
			case BLUE:
				return ActionTriggerIconProvider.Trigger_EngineHeat_Blue;
			case GREEN:
				return ActionTriggerIconProvider.Trigger_EngineHeat_Green;
			case YELLOW:
				return ActionTriggerIconProvider.Trigger_EngineHeat_Yellow;
			default:
				return ActionTriggerIconProvider.Trigger_EngineHeat_Red;
		}
	}
}
