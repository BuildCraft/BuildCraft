package buildcraft.lib.mj.net;

import com.google.common.collect.ImmutableList;

import buildcraft.api.mj.IMjConnection;
import buildcraft.api.mj.IMjMachine;
import buildcraft.api.mj.IMjMachineConsumer;
import buildcraft.api.mj.IMjMachineProducer;

public class MjConnection implements IMjConnection {
    private final IMjMachineProducer producer;
    private final ImmutableList<IMjMachine> transporters;
    private final IMjMachineConsumer consumer;
    private final int milliWattsIn, milliWattsOut;
    final int dimension;
    boolean isComplete = false;

    public MjConnection(IMjMachineProducer producer, ImmutableList<IMjMachine> transporters, IMjMachineConsumer consumer, int milliWattsIn, int milliWattsOut) {
        this.producer = producer;
        this.transporters = transporters;
        this.consumer = consumer;
        this.milliWattsIn = milliWattsIn;
        this.milliWattsOut = milliWattsOut;
        // Only allow a single dimension
        dimension = producer.getIdentifiers().iterator().next().dimension;
    }

    @Override
    public IMjMachineProducer getProducer() {
        return producer;
    }

    @Override
    public ImmutableList<IMjMachine> getConductors() {
        return transporters;
    }

    @Override
    public IMjMachineConsumer getConsumer() {
        return consumer;
    }

    @Override
    public void breakConnection() {
        MjNetwork.INSTANCE.breakConnection(this);
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
