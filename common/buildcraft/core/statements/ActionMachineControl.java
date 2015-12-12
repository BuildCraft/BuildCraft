/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IControllable.Mode;
import buildcraft.core.lib.utils.BCStringUtils;

public class ActionMachineControl extends BCStatement implements IActionExternal {
    public final Mode mode;

    public ActionMachineControl(Mode mode) {
        super("buildcraft:machine." + mode.name().toLowerCase(Locale.ROOT), "buildcraft.machine." + mode.name().toLowerCase(Locale.ROOT));
		setBuildCraftLocation("core", "triggers/action_machinecontrol_" + mode.name().toLowerCase(Locale.ROOT));
        this.mode = mode;
    }

    @Override
    public String getDescription() {
        return BCStringUtils.localize("gate.action.machine." + mode.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public void actionActivate(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        if (target instanceof IControllable) {
            ((IControllable) target).setControlMode(mode);
        }
    }
}
