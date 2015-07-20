/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.Locale;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.statements.BCStatement;
import buildcraft.transport.Pipe;

public class TriggerPipeSignal extends BCStatement implements ITriggerInternal {

    boolean active;
    PipeWire color;

    public TriggerPipeSignal(boolean active, PipeWire color) {
        super(new ResourceLocation("buildcrafttransport:triggers/trigger_pipesignal_" + color.name().toLowerCase() + "_" + (active ? "active"
            : "inactive")), "buildcraft:pipe.wire.input." + color.name().toLowerCase(Locale.ENGLISH) + (active ? ".active" : ".inactive"),
                "buildcraft.pipe.wire.input." + color.name().toLowerCase(Locale.ENGLISH) + (active ? ".active" : ".inactive"));

        this.active = active;
        this.color = color;
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public String getDescription() {
        return String.format(StringUtils.localize("gate.trigger.pipe.wire." + (active ? "active" : "inactive")), StringUtils.localize("color." + color
                .name().toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        if (!(container instanceof IGate)) {
            return false;
        }

        Pipe<?> pipe = (Pipe<?>) ((IGate) container).getPipe();

        if (active) {
            if (pipe.signalStrength[color.ordinal()] == 0) {
                return false;
            }
        } else {
            if (pipe.signalStrength[color.ordinal()] > 0) {
                return false;
            }
        }

        for (IStatementParameter param : parameters) {
            if (param != null && param instanceof TriggerParameterSignal) {
                TriggerParameterSignal signal = (TriggerParameterSignal) param;

                if (signal.color != null) {
                    if (signal.active) {
                        if (pipe.signalStrength[signal.color.ordinal()] == 0) {
                            return false;
                        }
                    } else {
                        if (pipe.signalStrength[signal.color.ordinal()] > 0) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new TriggerParameterSignal();
    }
}
