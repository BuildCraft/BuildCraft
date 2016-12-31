/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;

import buildcraft.core.statements.BCStatement;
import buildcraft.core.statements.StatementParamGateSideOnly;
import buildcraft.core.statements.StatementParameterRedstoneLevel;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.TileGenericPipe;

public class TriggerRedstoneFaderInput extends BCStatement implements ITriggerInternal {
    public static enum Mode {
        LESS,
        EQUAL,
        GREATER;

        private final String key;

        Mode() {
            key = name().toLowerCase();
        }

        public String key() {
            return key;
        }
    }

    public final Mode mode;

    public TriggerRedstoneFaderInput(Mode mode) {
        super("buildcraft:redstone.input." + mode.key());
        setBuildCraftLocation("core", "triggers/trigger_redstoneinput_" + mode.key());
        this.mode = mode;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.redstone.input." + mode.key());
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
        if (parameters.length > 1 && parameters[1] instanceof StatementParamGateSideOnly
            && ((StatementParamGateSideOnly) parameters[1]).isOn) {
            inputLevel = tile.redstoneInputSide[gate.getSide().ordinal()];
        }

        switch (mode) {
            case LESS:
                return inputLevel < level;
            case EQUAL:
            default:
                return inputLevel == level;
            case GREATER:
                return inputLevel > level;
        }
    }

    @Override
    public IStatementParameter createParameter(int index) {
        IStatementParameter param = null;

        if (index == 0) {
            param = new StatementParameterRedstoneLevel((mode == Mode.LESS) ? 1 : 0, (mode == Mode.GREATER) ? 14 : 15);
        } else if (index == 1) {
            param = new StatementParamGateSideOnly();
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
