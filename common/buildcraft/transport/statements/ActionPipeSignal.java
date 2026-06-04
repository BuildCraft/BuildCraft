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
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.IWireEmitter;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;

public class ActionPipeSignal extends BCStatement implements IActionInternal {

    public final DyeColor colour;

    public ActionPipeSignal(DyeColor colour) {
        super("buildcraft:pipe.wire.output." + colour.name().toLowerCase(Locale.ROOT),
            "buildcraft.pipe.wire.output." + colour.name().toLowerCase(Locale.ROOT));
        this.colour = colour;
    }

    @Override
    public String getDescription() {
        return "gate.action.pipe.wire." + colour.getName();
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return ActionParameterSignal.EMPTY;
    }

    @Override
    public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
        if (!(container instanceof IWireEmitter)) {
            return;
        }
        IWireEmitter emitter = (IWireEmitter) container;
        emitter.emitWire(colour);
        for (IStatementParameter param : parameters) {
            if (param instanceof ActionParameterSignal) {
                DyeColor c = ((ActionParameterSignal) param).getColor();
                if (c != null) {
                    emitter.emitWire(c);
                }
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return BCTransportSprites.getPipeSignal(true, colour);
    }

    @Override
    public ActionPipeSignal[] getPossible() {
        return BCTransportStatements.ACTION_PIPE_SIGNAL;
    }
}
