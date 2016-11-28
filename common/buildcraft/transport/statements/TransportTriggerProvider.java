package buildcraft.transport.statements;

import java.util.Collection;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.*;

public enum TransportTriggerProvider implements ITriggerProvider {
    INSTANCE;

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer container) {
        
    }

    @Override
    public void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container, EnumFacing side) {

    }

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> triggers, EnumFacing side, TileEntity tile) {

    }
}
