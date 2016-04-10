package buildcraft.lib.mj.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.mj.EnumMjPowerType;
import buildcraft.api.mj.IConnectionLogic;
import buildcraft.api.mj.IMjConnection;
import buildcraft.api.mj.IMjMachineConsumer;
import buildcraft.lib.mj.helpers.task.IMjTask;

public class MjSimpleConsumer extends MjSimpleMachine implements IMjMachineConsumer {
    private final EnumMjPowerType powerType;
    private final List<IMjTask> tasks = new ArrayList<>();

    public MjSimpleConsumer(TileEntity tile, IConnectionLogic logic, EnumFacing[] faces, EnumMjPowerType powerType) {
        super(tile, logic, faces);
        this.powerType = powerType;
    }

    @Override
    public boolean onConnectionCreate(IMjConnection connection) {
        return false;
    }

    @Override
    public void onConnectionActivate(IMjConnection connection) {

    }

    @Override
    public void onConnectionBroken(IMjConnection connection) {

    }

    @Override
    public EnumMjPowerType getPowerType() {
        return powerType;
    }
}
