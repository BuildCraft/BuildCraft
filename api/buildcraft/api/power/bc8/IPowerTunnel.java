package buildcraft.api.power.bc8;

import java.util.List;
import java.util.Set;

import buildcraft.api.power.bc8.IPowerConnection.IPowerConsumer;
import buildcraft.api.power.bc8.IPowerConnection.IPowerRelay;
import buildcraft.api.power.bc8.IPowerConnection.IPowerSupplier;

/** A power tunnel that has a specific {@link IPowerConnection.IPowerSupplier} and an
 * {@link IPowerConnection.IPowerConsumer} that it is transfered around. */
public interface IPowerTunnel {
    /** @return The type of power that the units are in. */
    EnumPowerBar barType();

    /** @return {@link IPowerConsumer} that is taking this power */
    IPowerConsumer consumer();

    /** @return All of the relays that transfer the power. */
    List<IPowerRelay> relays();

    /** Attempts to use the power supplied by the {@link #supplier()}. This should only be called by the
     * {@link #consumer()}.
     * <p>
     * This is here to actually use up the power supplied by the supplier. If for some reason the supplier can no longer
     * produce the given power this bar should be disconnected by calling {@link #disconnect()} and a new
     * {@link IPowerTunnel} connection should be made.
     * <p>
     * This will generally notify all of the relays that the specified power was transfered (so they can update
     * themselves graphically)
     * 
     * @param rf The amount of RF to actually use. You are not limited by the maximum power that you have requested be
     *            transfered but it may refuse more than what you had requested (due to the maximum allowed to pass by
     *            {@link IPowerRelay#maxUnitsTransfered()})
     * 
     * @return The amount of power available that has been removed from the suppliers (and the consumer can consume) */
    int usePower(int rfMin, int rfMax);

    /** @return The amount of RF that the {@link #consumer()} has requested to be given to it each <b>second</b>. This
     *         will be in multiples of 50 RF as each {@link EnumPowerBar#units} is */
    default int requestedRf() {
        return requestedUnits() * 50;
    }

    /** @return The number of units that this tunnel can supply. */
    int requestedUnits();

    /** @return How many more ticks until this tunnel auto-disconnects itself from the connections if
     *         {@link #usePower(int)} has not returned true in a short enough time. Usually starts at 80 ticks. */
    int timeout();

    /** Calling this will immediately remove this tunnel and notify all {@link #relays()}, the {@link #supplier()} and
     * the {@link #consumer()} that this is not longer a valid tunnel. */
    void disconnect();

    /** Indicates that this tunnel does not have a single route but is made up of multiple {@link IPowerTunnelSingle}.
     * This might This will attempt to reconnect itself if any of the indervidual tunnels are broken. */
    public interface IPowerTunnelComposite extends IPowerTunnel {
        /** The returned set is a view of all the tunnels that make up this composite tunnel.
         * 
         * All of the single tunnels {@link #barType()} will return true from
         * {@link EnumPowerBar#supplies(EnumPowerBar)} given this {@link #barType()}. (That is to say they they may not
         * all be the same power type but you will always be able to accept the power type)
         * 
         * @return An (unmodifiable view) set that holds all of the indervidual tunnels that make the full composite
         *         tunnel. */
        Set<IPowerTunnelSingle> singleTunnels();
    }

    /** Created on 31 Dec 2015
     *
     * @author AlexIIL */
    public interface IPowerTunnelSingle extends IPowerTunnel {
        /** @return The {@link IPowerSupplier} that is suppling this power. */
        IPowerSupplier supplier();
    }
}
