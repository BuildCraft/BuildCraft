/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.statements;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;

public class TriggerPowerRequested extends BCStatement implements ITriggerInternal {

    public TriggerPowerRequested() {
        super("buildcraft:powerRequested");
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        if (!(source instanceof IGate)) {
            return false;
        }
        PipeFlow f = ((IGate) source).getPipeHolder().getPipe().getFlow();
        if (f instanceof PipeFlowPower) {
            return ((PipeFlowPower) f).getPowerRequested(null) > 0;
        } else if (f instanceof PipeFlowRedstoneFlux) {
            return ((PipeFlowRedstoneFlux) f).getPowerRequested(null) > 0;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "gate.trigger.pipe.requestsEnergy";
    }

    @Nullable
    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_POWER_REQUESTED;
    }
}
