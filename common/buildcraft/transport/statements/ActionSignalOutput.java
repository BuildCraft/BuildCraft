/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.Locale;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.core.statements.BCStatement;
import buildcraft.transport.Gate;

public class ActionSignalOutput extends BCStatement implements IActionInternal {

    public final PipeWire color;

    public ActionSignalOutput(PipeWire color) {
        super("buildcraft:pipe.wire.output." + color.name().toLowerCase(Locale.ENGLISH), "buildcraft.pipe.wire.output." + color.name().toLowerCase(
                Locale.ENGLISH));

        setBuildCraftLocation("transport", "triggers/trigger_pipesignal_" + color.name().toLowerCase() + "_active");
        this.color = color;
    }

    @Override
    public String getDescription() {
        return String.format(BCStringUtils.localize("gate.action.pipe.wire"), BCStringUtils.localize("color." + color.name().toLowerCase(
                Locale.ENGLISH)));
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new ActionParameterSignal();
    }

    @Override
    public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
        Gate gate = (Gate) container;

        gate.broadcastSignal(color);

        for (IStatementParameter param : parameters) {
            if (param != null && param instanceof ActionParameterSignal) {
                ActionParameterSignal signal = (ActionParameterSignal) param;

                if (signal.getColor() != null) {
                    gate.broadcastSignal(signal.getColor());
                }
            }
        }
    }
}
