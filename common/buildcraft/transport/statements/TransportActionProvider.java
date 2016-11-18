package buildcraft.transport.statements;

import java.util.Collection;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.*;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.api.transport.neptune.PipePluggable;

import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.plug.PluggablePulsar;

public enum TransportActionProvider implements IActionProvider {
    INSTANCE;

    @Override
    public void addInternalActions(Collection<IActionInternal> actions, IStatementContainer container) {

    }

    @Override
    public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container, EnumFacing side) {
        if (container instanceof IGate) {
            IGate gate = (IGate) container;
            IPipeHolder holder = gate.getPipeHolder();
            PipePluggable plug = holder.getPluggable(side);
            if (plug instanceof PluggablePulsar) {
                actions.add(BCTransportStatements.ACTION_PULSAR_CONSTANT);
                actions.add(BCTransportStatements.ACTION_PULSAR_SINGLE);
            }
        }
    }

    @Override
    public void addExternalActions(Collection<IActionExternal> actions, EnumFacing side, TileEntity tile) {

    }
}
