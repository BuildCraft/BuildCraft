package buildcraft.core.mj.net;

import com.google.common.collect.ImmutableList;

import buildcraft.core.mj.api.IMjConnection;
import buildcraft.core.mj.api.IMjMachine;
import buildcraft.core.mj.api.IMjMachineConsumer;
import buildcraft.core.mj.api.IMjMachineProducer;

public class MjConnection implements IMjConnection {
    private final IMjMachineProducer producer;
    private final ImmutableList<IMjMachine> transporters;
    private final IMjMachineConsumer consumer;
    private final int milliWattsIn, milliWattsOut;
    boolean isComplete = false;

    public MjConnection(IMjMachineProducer producer, ImmutableList<IMjMachine> transporters, IMjMachineConsumer consumer, int milliWattsIn, int milliWattsOut) {
        this.producer = producer;
        this.transporters = transporters;
        this.consumer = consumer;
        this.milliWattsIn = milliWattsIn;
        this.milliWattsOut = milliWattsOut;
    }

    @Override
    public IMjMachineProducer getProducer() {
        return null;
    }

    @Override
    public ImmutableList<IMjMachine> getConductors() {
        return null;
    }

    @Override
    public IMjMachineConsumer getConsumer() {
        return null;
    }

    @Override
    public void breakConnection() {

    }

    @Override
    public int milliWattsIn() {
        return milliWattsIn;
    }

    @Override
    public int milliWattsOut() {
        return milliWattsOut;
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }
}
