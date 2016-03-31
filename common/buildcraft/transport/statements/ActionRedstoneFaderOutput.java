/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.statements.ActionRedstoneOutput;
import buildcraft.core.statements.StatementParameterRedstoneGateSideOnly;
import buildcraft.core.statements.StatementParameterRedstoneLevel;

public class ActionRedstoneFaderOutput extends ActionRedstoneOutput {
    public ActionRedstoneFaderOutput() {
        super("buildcraft:redstone.output.analog");
        setBuildCraftLocation("core", "triggers/action_redstoneoutput");
    }

    @Override
    protected int getRGSOSlot() { return 1; }

    @Override
    protected int getSignalLevel(IStatementParameter[] parameters) {
        if (parameters.length >= 1 && (parameters[0] instanceof StatementParameterRedstoneLevel)) {
            return ((StatementParameterRedstoneLevel) parameters[0]).level;
        }

        return 15;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        IStatementParameter param = null;

        if (index == 0) {
            param = new StatementParameterRedstoneLevel(15, 1, 15);
        } else if (index == 1) {
            param = new StatementParameterRedstoneGateSideOnly();
        }

        return param;
    }

    @Override
    public int maxParameters() {
        return 2;
    }

    @Override
    public int minParameters() {
        return 1;
    }
}
