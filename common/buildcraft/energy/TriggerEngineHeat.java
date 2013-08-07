/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.TileEngine.EnergyStage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;

public class TriggerEngineHeat extends BCTrigger {

	public EnergyStage stage;
	@SideOnly(Side.CLIENT)
	private Icon iconBlue, iconGreen, iconYellow, iconRed;

	public TriggerEngineHeat(int id, EnergyStage stage, String uniqueTag) {
		super(id, uniqueTag);

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
	@SideOnly(Side.CLIENT)
	public Icon getIcon() {
		switch (stage) {
			case BLUE:
				return iconBlue;
			case GREEN:
				return iconGreen;
			case YELLOW:
				return iconYellow;
			default:
				return iconRed;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		iconBlue = iconRegister.registerIcon("buildcraft:triggers/trigger_engineheat_blue");
		iconGreen = iconRegister.registerIcon("buildcraft:triggers/trigger_engineheat_green");
		iconYellow = iconRegister.registerIcon("buildcraft:triggers/trigger_engineheat_yellow");
		iconRed = iconRegister.registerIcon("buildcraft:triggers/trigger_engineheat_red");
	}
}
