/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.statements.StatementParameterRedstoneGateSideOnly;
import buildcraft.core.statements.StatementParameterRedstoneLevel;
import buildcraft.transport.TileGenericPipe;

public class TriggerRedstoneComparedInput extends BCStatement implements ITriggerInternal {
    public final boolean less;

    public TriggerRedstoneComparedInput(boolean less) {
        super("buildcraft:redstone.input." + (less ? "less" : "greater"));
        setBuildCraftLocation("core", "triggers/trigger_redstoneinput_" + (less ? "less" : "greater"));
        this.less = less;
    }

    @Override
    public String getDescription() {
        return BCStringUtils.localize("gate.trigger.redstone.input." + (less ? "less" : "greater"));
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        if (!(container instanceof IGate) || parameters.length < 1 || !(parameters[0] instanceof StatementParameterRedstoneLevel)) {
            return false;
        }

        int level = ((StatementParameterRedstoneLevel) parameters[0]).level;

        IGate gate = (IGate) container;
        TileGenericPipe tile = (TileGenericPipe) gate.getPipe().getTile();
        int inputLevel = tile.redstoneInput;
        if (parameters.length > 1 && parameters[1] instanceof StatementParameterRedstoneGateSideOnly
            && ((StatementParameterRedstoneGateSideOnly) parameters[1]).isOn) {
            inputLevel = tile.redstoneInputSide[gate.getSide().ordinal()];
        }

        return less ? inputLevel < level : inputLevel > level;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        IStatementParameter param = null;

        if (index == 0) {
            param = new StatementParameterRedstoneLevel(less ? 1 : 0, less ? 15 : 14);
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
