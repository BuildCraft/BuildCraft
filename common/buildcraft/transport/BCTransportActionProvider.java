/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.misc.ColourUtil;

import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.plug.PluggablePulsar;
import buildcraft.transport.statements.TriggerPipeSignal;
import buildcraft.transport.wire.IWireEmitter;

public enum BCTransportActionProvider implements IActionProvider {
    INSTANCE;

    @Override
    public void addInternalActions(Collection<IActionInternal> actions, IStatementContainer container) {
        if (container instanceof IGate) {
            IGate gate = (IGate) container;
            IPipeHolder holder = gate.getPipeHolder();
            holder.fireEvent(new PipeEventStatement.AddActionInternal(holder, actions));

            if (container instanceof IWireEmitter) {
                for (EnumDyeColor colour : ColourUtil.COLOURS) {
                    if (TriggerPipeSignal.doesGateHaveColour(gate, colour)) {
                        actions.add(BCTransportStatements.ACTION_PIPE_SIGNAL[colour.ordinal()]);
                    }
                }
            }
        }
    }

    @Override
    public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container, @Nonnull EnumFacing side) {
        if (container instanceof IGate) {
            IGate gate = (IGate) container;
            IPipeHolder holder = gate.getPipeHolder();
            holder.fireEvent(new PipeEventStatement.AddActionInternalSided(holder, actions, side));
            PipePluggable plug = holder.getPluggable(side);
            if (plug instanceof PluggablePulsar) {
                actions.add(BCTransportStatements.ACTION_PULSAR_CONSTANT);
                actions.add(BCTransportStatements.ACTION_PULSAR_SINGLE);
            }
        }
    }

    @Override
    public void addExternalActions(Collection<IActionExternal> actions, @Nonnull EnumFacing side, TileEntity tile) {

    }
}
