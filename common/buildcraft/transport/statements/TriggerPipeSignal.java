/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.statements;

import java.util.Locale;

import buildcraft.transport.wire.WireManager;
import net.minecraft.item.EnumDyeColor;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.neptune.IWireManager;

import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;

public class TriggerPipeSignal extends BCStatement implements ITriggerInternal {

    private final boolean active;
    private final EnumDyeColor colour;

    public TriggerPipeSignal(boolean active, EnumDyeColor colour) {
        super("buildcraft:pipe.wire.input." + colour.getName().toLowerCase(Locale.ROOT) + (active ? ".active" : ".inactive"),//
                "buildcraft.pipe.wire.input." + colour.getName().toLowerCase(Locale.ROOT) + (active ? ".active" : ".inactive"));

        this.active = active;
        this.colour = colour;
    }

    public static boolean doesGateHaveColour(IGate gate, EnumDyeColor c) {
        // FIXME: replace with a check to wires.hasWire(colour)!
        return gate.getPipeHolder().getWireManager().hasPartOfColor(c);
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public String getDescription() {
        return String.format(LocaleUtil.localize("gate.trigger.pipe.wire." + (active ? "active" : "inactive")), ColourUtil.getTextFullTooltip(colour));
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        if(!(container instanceof IGate)) {
            return false;
        }

        IGate gate = (IGate) container;
        IWireManager wires = gate.getPipeHolder().getWireManager();

        if(active) {
            if(!wires.isAnyPowered(colour)) {
                return false;
            }
        } else if(wires.isAnyPowered(colour)) {
            return false;
        }

        for(IStatementParameter param : parameters) {
            if(param != null && param instanceof TriggerParameterSignal) {
                TriggerParameterSignal signal = (TriggerParameterSignal) param;
                if(signal.colour != null) {
                    if(!wires.isAnyPowered(signal.colour)) {
                        return false;
                    }
                } else if(wires.isAnyPowered(signal.colour)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new TriggerParameterSignal();
    }

    @Override
    public SpriteHolder getSpriteHolder() {
        return BCTransportSprites.getPipeSignal(active, colour);
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.TRIGGER_PIPE_SIGNAL;
    }
}
