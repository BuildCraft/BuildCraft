/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import net.minecraft.util.EnumFacing;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IActionSingle;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.statements.BCStatement;
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
