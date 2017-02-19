/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import net.minecraft.util.EnumFacing;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.*;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.plug.PluggablePulsar;

public class ActionPowerPulsar extends BCStatement implements IActionInternalSided, IActionSingle {

    public final boolean constant;

    public ActionPowerPulsar(boolean constant) {
        super("buildcraft:pulsar." + (constant ? "constant" : "single"), "buildcraft.pulser.constant" + (constant ? "constant" : "single"));
        this.constant = constant;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize(constant ? "gate.action.pulsar.constant" : "gate.action.pulsar.single");
    }

    @Override
    public void actionActivate(EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IGate) {
            IGate gate = (IGate) source;
            IPipeHolder pipe = gate.getPipeHolder();
            PipePluggable plug = pipe.getPluggable(side);
            if (plug instanceof PluggablePulsar) {
                PluggablePulsar pulsar = (PluggablePulsar) plug;
                if (constant) {
                    pulsar.enablePulsar();
                } else {
                    pulsar.addSinglePulse();
                }
            }
        }
    }

    @Override
    public boolean singleActionTick() {
        return !constant;
    }

    @Override
    public SpriteHolder getSpriteHolder() {
        return constant ? BCTransportSprites.ACTION_PULSAR_CONSTANT : BCTransportSprites.ACTION_PULSAR_SINGLE;
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_PULSAR;
    }
}
