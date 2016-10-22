/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import buildcraft.api.statements.IActionInternal.IActionInternalSingle;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.StringUtilBC;

public class ActionSingleEnergyPulse extends BCStatement implements IActionInternalSingle {

    public ActionSingleEnergyPulse() {
        super("buildcraft:pulsar.single", "buildcraft.pulser.single");
        setBuildCraftLocation("transport", "triggers/action_single_pulsar");
    }

    @Override
    public String getDescription() {
        return StringUtilBC.localize("gate.action.pulsar.single");
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}

    @Override
    public boolean singleActionTick() {
        return true;
    }
}
