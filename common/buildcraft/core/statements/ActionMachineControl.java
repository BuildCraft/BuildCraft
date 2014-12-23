/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.EnumFacing;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IControllable.Mode;
import buildcraft.core.utils.StringUtils;

public class ActionMachineControl extends BCStatement implements IActionExternal {
	public final Mode mode;

	public ActionMachineControl(Mode mode) {
		super("buildcraft:machine." + mode.name().toLowerCase(Locale.ENGLISH), "buildcraft.machine." + mode.name().toLowerCase(Locale.ENGLISH));

		this.mode = mode;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.machine." + mode.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public int getSheetLocation() {
		return 15 + (4 + mode.ordinal()) * 16;
	}

	@Override
	public void actionActivate(TileEntity target, EnumFacing side,
			IStatementContainer source, IStatementParameter[] parameters) {
		if (target instanceof IControllable) {
			((IControllable) target).setControlMode(mode);
		}
	}
}
