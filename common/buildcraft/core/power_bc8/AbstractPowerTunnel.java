package buildcraft.core.power_bc8;

import java.util.List;

import buildcraft.api.power.bc8.EnumPowerBar;
import buildcraft.api.power.bc8.IPowerConnection.IPowerConsumer;
import buildcraft.api.power.bc8.IPowerConnection.IPowerRelay;
import buildcraft.api.power.bc8.IPowerTunnel;

public abstract class AbstractPowerTunnel implements IPowerTunnel {
    private final EnumPowerBar powerType;
    private final IPowerConsumer consumer;
    private final List<IPowerRelay> relays;
    private final int requestedUnits;
    protected int timeout = 80;

    public AbstractPowerTunnel(EnumPowerBar powerType, IPowerConsumer consumer, List<IPowerRelay> relays, int requestedUnits) {
        this.powerType = powerType;
        this.consumer = consumer;
        this.relays = relays;
        this.requestedUnits = requestedUnits;
    }

    @Override
    public EnumPowerBar powerType() {
        return powerType;
    }

    @Override
    public IPowerConsumer consumer() {
        return consumer;
    }

    @Override
    public List<IPowerRelay> relays() {
        return relays;
    }

    @Override
    public abstract int usePower(int rfMin, int rfMax);

    @Override
    public int requestedUnits() {
        return requestedUnits;
    }

    @Override
    public int timeout() {
        return timeout;
    }

    @Override
    public void disconnect() {
        consumer().notifyTunnelDisconnected(this);
        relays().forEach(r -> r.notifyTunnelDisconnected(this));
    }
}
