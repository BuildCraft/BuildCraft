/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.statements;

import java.util.Locale;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.transport.IWireEmitter;

import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import ct.buildcraft.lib.misc.LocaleUtil;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.transport.BCTransportSprites;
import ct.buildcraft.transport.BCTransportStatements;

public class ActionPipeSignal extends BCStatement implements IActionInternal {

    public final DyeColor colour;

    public ActionPipeSignal(DyeColor colour) {
        super("buildcraft:pipe.wire.output." + colour.name().toLowerCase(Locale.ROOT), //
                "buildcraft.pipe.wire.output." + colour.name().toLowerCase(Locale.ROOT));

        this.colour = colour;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.action.pipe.wire", LocaleUtil.localizeColourComponent(colour));
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
            if (param != null && param instanceof ActionParameterSignal) {
                ActionParameterSignal signal = (ActionParameterSignal) param;

                if (signal.getColor() != null) {
                    emitter.emitWire(signal.getColor());
                }
            }
        }
    }

    @Override
    public SpriteHolder getSprite() {
        return BCTransportSprites.getPipeSignal(true, colour);
    }

    @Override
    public ActionPipeSignal[] getPossible() {
        return BCTransportStatements.ACTION_PIPE_SIGNAL;
    }
}
