package buildcraft.builders.tile;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.capabilities.Capability;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.mj.types.MachineType;
import buildcraft.lib.mj.MjReciverBatteryWrapper;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileQuarry extends TileBC_Neptune implements ITickable {
    private final MjBattery mjBattery;
    private final MjCapabilityHelper mjCapHelper;

    public TileQuarry() {
        mjBattery = new MjBattery(1600L * MjAPI.MJ);
        mjCapHelper = new MjCapabilityHelper(new MjReciverBatteryWrapper(mjBattery, MachineType.QUARRY));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (mjCapHelper.hasCapability(capability, facing)) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (mjCapHelper.hasCapability(capability, facing)) {
            return mjCapHelper.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void update() {

    }

    // TODO: Tiny robot for building
}
