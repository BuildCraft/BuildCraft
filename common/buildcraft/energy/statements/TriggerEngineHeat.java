/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.statements;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.EnumFacing;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.TileEngine;
import buildcraft.energy.TileEngine.EnergyStage;

public class TriggerEngineHeat extends BCStatement implements ITriggerExternal {

	public EnergyStage stage;

	public TriggerEngineHeat(EnergyStage stage) {
		super("buildcraft:engine.stage." + stage.name().toLowerCase(Locale.ENGLISH), "buildcraft.engine.stage." + stage.name().toLowerCase(Locale.ENGLISH));

		this.stage = stage;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.engine." + stage.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public int getSheetLocation() {
		return 14 + (3 + stage.ordinal()) * 16;
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer container, IStatementParameter[] parameters) {
		if (tile instanceof TileEngine) {
			TileEngine engine = (TileEngine) tile;

			return engine.getEnergyStage() == stage;
		}

		return false;
	}
}
