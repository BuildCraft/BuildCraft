package buildcraft.core.lib.engines;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

import buildcraft.core.lib.block.TileBuildCraft_BC8;
import buildcraft.core.mj.api.*;
import buildcraft.core.mj.helpers.MjSimpleProducer;

public abstract class TileEngineBase_BC8 extends TileBuildCraft_BC8 {
    protected final IMjMachineProducer mjProducer = createProducer();
    public EnumFacing currentDirection;

    protected abstract IMjMachineProducer createProducer();

    /** An implementation of {@link IConnectionLogic#getConnectableMachines(MjMachineIdentifier)}. */
    public Collection<MjMachineIdentifier> getConnectableMachines(MjMachineIdentifier identifier) {
        return ImmutableSet.of();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == MjAPI.CAP_MACHINE) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == MjAPI.CAP_MACHINE) return (T) mjProducer;
        return super.getCapability(capability, facing);
    }

    public abstract class EngineProducer extends MjSimpleProducer {
        public EngineProducer(EnumMjPowerType powerType) {
            super(TileEngineBase_BC8.this, TileEngineBase_BC8.this::getConnectableMachines, null, powerType);
        }

        // TODO: What does this need?
    }
}
