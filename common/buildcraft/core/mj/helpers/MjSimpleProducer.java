package buildcraft.core.mj.helpers;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.core.mj.api.*;

public class MjSimpleProducer extends MjSimpleMachine implements IMjMachineProducer {
    private final EnumMjPowerType powerType;

    public MjSimpleProducer(TileEntity tile, IConnectionLogic logic, EnumFacing[] faces, EnumMjPowerType powerType) {
        super(tile, logic, faces);
        this.powerType = powerType;
    }

    @Override
    public boolean onConnectionCreate(IMjConnection connection) {
        return true;
    }

    @Override
    public void onConnectionActivate(IMjConnection connection) {}

    @Override
    public void onConnectionBroken(IMjConnection connection) {}

    @Override
    public int getSuppliable(IMjRequest request) {
        return 0;
    }

    @Override
    public EnumMjPowerType getPowerType() {
        return powerType;
    }

    @Override
    public boolean canProduceFor(IMjRequest request, List<IMjMachine> machinesSoFar) {
        return true;
    }
}
