package buildcraft.lib.mj;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjBattery;

import javax.annotation.Nonnull;

public class MjBatteryReciver implements IMjReceiver, IMjReadable {
    private final MjBattery battery;

    public MjBatteryReciver(MjBattery battery) {
        this.battery = battery;
    }

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return battery.getCapacity() - battery.getStored();
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return battery.addPowerChecking(microJoules, simulate);
    }

    @Override
    public long getStored() {
        return battery.getStored();
    }

    @Override
    public long getCapacity() {
        return battery.getCapacity();
    }
}
