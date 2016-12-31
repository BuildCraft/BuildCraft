/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.Locale;

import net.minecraft.item.EnumDyeColor;

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
