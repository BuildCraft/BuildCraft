/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.wire.IWireEmitter;
import net.minecraft.item.EnumDyeColor;

import java.util.Locale;

public class ActionPipeSignal extends BCStatement implements IActionInternal {

    public final EnumDyeColor colour;

    public ActionPipeSignal(EnumDyeColor colour) {
        super("buildcraft:pipe.wire.output." + colour.name().toLowerCase(Locale.ROOT), //
                "buildcraft.pipe.wire.output." + colour.name().toLowerCase(Locale.ROOT));

        this.colour = colour;
    }

    @Override
    public String getDescription() {
        return String.format(LocaleUtil.localize("gate.action.pipe.wire"), ColourUtil.getTextFullTooltip(colour));
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
        if (!(container instanceof IWireEmitter)) {
            return;
        }
        IWireEmitter emitter = (IWireEmitter) container;
        emitter.emitWire(colour);

        for (IStatementParameter param : parameters) {
            if (param != null && param instanceof ActionParameterSignal) {
                ActionParameterSignal signal = (ActionParameterSignal) param;

                if (signal.getColor() != null) {
                    emitter.emitWire(signal.getColor());
                }
            }
        }
    }

    @Override
    public SpriteHolder getSpriteHolder() {
        return BCTransportSprites.getPipeSignal(true, colour);
    }

    @Override
    public ActionPipeSignal[] getPossible() {
        return BCTransportStatements.ACTION_PIPE_SIGNAL;
    }
}
