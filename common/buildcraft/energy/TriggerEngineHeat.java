/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import buildcraft.core.DefaultProps;
import buildcraft.transport.IconItemConstants;

public class TriggerEngineHeat extends Trigger {

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
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof TileEngine) {
			Engine engine = ((TileEngine) tile).engine;

			return engine != null && engine.getEnergyStage() == stage;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getTextureIcon() {
		switch (stage) {
		case Blue:
			return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_EngineHeat_Blue];
		case Green:
			return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_EngineHeat_Green];
		case Yellow:
			return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_EngineHeat_Yellow];
		default:
			return BuildCraftTransport.instance.itemIcons[IconItemConstants.Trigger_EngineHeat_Red];
		}
	}
}
