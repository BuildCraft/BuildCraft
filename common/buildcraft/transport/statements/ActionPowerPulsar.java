/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import net.minecraft.util.EnumFacing;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.api.transport.neptune.PipePluggable;

import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.plug.PluggablePulsar;

public class ActionPowerPulsar extends BCStatement implements IActionInternalSided {

    public final boolean on;

    public ActionPowerPulsar(boolean on) {
        super("buildcraft:pulsar." + (on ? "on" : "off"), "buildcraft:pulsar.constant", "buildcraft.pulser.constant");
        this.on = on;
    }

    @Override
    public String getDescription() {
        return StringUtilBC.localize("gate.action.pulsar." + (on ? "on" : "off"));
    }

    @Override
    public void actionActivate(EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IGate) {
            IGate gate = (IGate) source;
            IPipeHolder pipe = gate.getPipeHolder();
            PipePluggable plug = pipe.getPluggable(side);
            if (plug instanceof PluggablePulsar) {
                PluggablePulsar pulsar = (PluggablePulsar) plug;
                boolean before = pulsar.isPulsing;
                if (before != on) {
                    pulsar.setPulsing(on);
                }
            }
        }
    }

    @Override
    public SpriteHolder getSpriteHolder() {
        return on ? BCTransportSprites.ACTION_PULSAR_ON : BCTransportSprites.ACTION_PULSAR_OFF;
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_PULSAR;
    }
}
