/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.statements;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

import ct.buildcraft.api.gates.IGate;
import ct.buildcraft.api.statements.IActionExternal;
import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IActionInternalSided;
import ct.buildcraft.api.statements.IActionProvider;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.transport.IWireEmitter;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pipe.PipeEventStatement;

import ct.buildcraft.lib.misc.ColourUtil;

import ct.buildcraft.transport.BCTransportStatements;

public enum ActionProviderPipes implements IActionProvider {
    INSTANCE;

    @Override
    public void addInternalActions(Collection<IActionInternal> actions, IStatementContainer container) {
        if (container instanceof IGate) {
            IGate gate = (IGate) container;
            IPipeHolder holder = gate.getPipeHolder();
            holder.fireEvent(new PipeEventStatement.AddActionInternal(holder, actions));

            if (container instanceof IWireEmitter) {
                for (DyeColor colour : ColourUtil.COLOURS) {
                    if (TriggerPipeSignal.doesGateHaveColour(gate, colour)) {
                        actions.add(BCTransportStatements.ACTION_PIPE_SIGNAL[colour.ordinal()]);
                    }
                }
            }
        }
    }

    @Override
    public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container,
        @Nonnull Direction side) {
        if (container instanceof IGate) {
            IGate gate = (IGate) container;
            IPipeHolder holder = gate.getPipeHolder();
            holder.fireEvent(new PipeEventStatement.AddActionInternalSided(holder, actions, side));
        }
    }

    @Override
    public void addExternalActions(Collection<IActionExternal> actions, @Nonnull Direction side, BlockEntity tile) {

    }
}
