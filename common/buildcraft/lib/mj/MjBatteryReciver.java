package buildcraft.lib.mj;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjBattery;

public class MjBatteryReciver implements IMjReceiver, IMjReadable {
    private final MjBattery battery;

    public MjBatteryReciver(MjBattery battery) {
        this.battery = battery;
    }

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return battery.getCapacity() - battery.getStored();
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        if (simulate) {
            if (battery.isFull()) {
                return microJoules;
            } else {
                return 0;
            }
        }
        return battery.addPowerChecking(microJoules);
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
