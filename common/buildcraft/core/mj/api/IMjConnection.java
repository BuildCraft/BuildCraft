package buildcraft.core.mj.api;

import com.google.common.collect.ImmutableList;

public interface IMjConnection {
    IMjMachineProducer getProducer();

    /** Gets all of the conductors that form this connection, in order from producer-> consumer. They will either be
     * {@link IMjMachineTransporter} or {@link IMjMachineConverter}. They will NEVER be null */
    ImmutableList<IMjMachine> getConductors();

    IMjMachineConsumer getConsumer();

    /** Breaks this connection. Immediately notifies the producer, consumer and all conductors that this is no longer
     * active, and power stops being transfered. */
    void breakConnection();

    /** @return the amount of milliwatts that goes into this connection from the producer. */
    int milliWattsIn();

    /** @return the amount of milliwatts that comes out of this connection into the consumer */
    int milliWattsOut();

    /** @return True if this connection has completed its setup phase */
    boolean isComplete();
}
