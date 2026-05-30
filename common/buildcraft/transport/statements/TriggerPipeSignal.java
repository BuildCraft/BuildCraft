/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.statements;

import java.util.Locale;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.DyeColor;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.IWireManager;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;

public class TriggerPipeSignal extends BCStatement implements ITriggerInternal {

    private final boolean active;
    private final DyeColor colour;

    public TriggerPipeSignal(boolean active, DyeColor colour) {
        super("buildcraft:pipe.wire.input." + colour.getName().toLowerCase(Locale.ROOT)
                + (active ? ".active" : ".inactive"),
            "buildcraft.pipe.wire.input." + colour.getName().toLowerCase(Locale.ROOT)
                + (active ? ".active" : ".inactive"));
        this.active = active;
        this.colour = colour;
    }

    public static boolean doesGateHaveColour(IGate gate, DyeColor c) {
        return gate.getPipeHolder().getWireManager().hasPartOfColor(c);
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return TriggerParameterSignal.EMPTY;
    }

    @Override
    public String getDescription() {
        return "gate.trigger.pipe.wire." + (active ? "active" : "inactive") + "." + colour.getName();
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        if (!(container instanceof IGate)) {
            return false;
        }
        IGate gate = (IGate) container;
        IWireManager wires = gate.getPipeHolder().getWireManager();
        if (this.active != wires.isAnyPowered(this.colour)) {
            return false;
        }
        for (IStatementParameter param : parameters) {
            if (param instanceof TriggerParameterSignal) {
                TriggerParameterSignal signal = (TriggerParameterSignal) param;
                if (signal.colour == null) continue;
                if (signal.active != wires.isAnyPowered(signal.colour)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return BCTransportSprites.getPipeSignal(active, colour);
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.TRIGGER_PIPE_SIGNAL;
    }
}
