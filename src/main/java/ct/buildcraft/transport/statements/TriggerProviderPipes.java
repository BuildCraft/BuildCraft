/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.statements;

import java.util.Collection;

import javax.annotation.Nonnull;

import ct.buildcraft.api.gates.IGate;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.ITriggerExternal;
import ct.buildcraft.api.statements.ITriggerInternal;
import ct.buildcraft.api.statements.ITriggerInternalSided;
import ct.buildcraft.api.statements.ITriggerProvider;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pipe.PipeEventStatement;
import ct.buildcraft.lib.misc.ColourUtil;
import ct.buildcraft.transport.BCTransportStatements;
import ct.buildcraft.transport.pipe.flow.PipeFlowPower;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;

public enum TriggerProviderPipes implements ITriggerProvider {
    INSTANCE;

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer container) {
        if (container instanceof IGate) {
            IGate gate = (IGate) container;
            IPipeHolder holder = gate.getPipeHolder();
            holder.fireEvent(new PipeEventStatement.AddTriggerInternal(holder, triggers));

            for (DyeColor colour : ColourUtil.COLOURS) {
                if (TriggerPipeSignal.doesGateHaveColour(gate, colour)) {
                    triggers.add(BCTransportStatements.TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 0]);
                    triggers.add(BCTransportStatements.TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 1]);
                }
            }

            if (holder.getPipe().getFlow() instanceof PipeFlowPower) {
                triggers.add(BCTransportStatements.TRIGGER_POWER_REQUESTED);
            }
        }
    }

    @Override
    public void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container, @Nonnull Direction side) {
        if (container instanceof IGate) {
            IGate gate = (IGate) container;
            IPipeHolder holder = gate.getPipeHolder();
            holder.fireEvent(new PipeEventStatement.AddTriggerInternalSided(holder, triggers, side));
        }
    }

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> triggers, @Nonnull Direction side, BlockEntity tile) {

    }
}
