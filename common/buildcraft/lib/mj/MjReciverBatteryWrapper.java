package buildcraft.lib.mj;

import javax.annotation.Nonnull;

import buildcraft.api.mj.*;

public class MjReciverBatteryWrapper implements IMjReceiver, IMjReadable {
    private final MjBattery battery;

    @Nonnull
    private final IMjConnectorType type;

    public MjReciverBatteryWrapper(MjBattery battery, @Nonnull IMjConnectorType type) {
        this.battery = battery;
        this.type = type;
    }

    @Override
    public boolean canConnect(IMjConnector other) {
        return type.getSimpleType().canReceiveFrom(other.getType().getSimpleType());
    }

    @Override
    public IMjConnectorType getType() {
        return type;
    }

    @Override
    public long getPowerRequested() {
        return battery.getCapacity() - battery.getStored();
    }

    @Override
    public boolean receivePower(long microJoules, boolean simulate) {
        if (simulate) {
            return !battery.isFull();
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
