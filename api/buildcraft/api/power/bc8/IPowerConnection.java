package buildcraft.api.power.bc8;

import java.util.List;
import java.util.Map;

import buildcraft.api.core.Identifier;
import buildcraft.api.power.bc8.PowerAPI_BC8.IPowerNetwork;

// TODO: RETHINK THIS :P

/** Indicates that this tile entity can connect to the power network. */
public interface IPowerConnection {

    /** Gets a delegate object that will be used instead of this object for all power calls. Useful if you want to
     * abstract away the implementation or are a multi-block tile with a single tile that controls everything.
     * <p>
     * It is <b>highly</b> recommended that you call {@link #delegatesDelegate(IPowerConnection)} instead of this
     * function directly if you are not inside of this {@link #delegate()} function */
    default IPowerConnection delegate() {
        return this;
    }

    /** @return The true delegate for the given connection. Useful if your delegates follow strange rules and may or may
     *         not have their own delegates */
    static IPowerConnection delegatesDelegate(IPowerConnection connection) {
        IPowerConnection delegate = connection.delegate();
        while (delegate != connection) {
            connection = delegate;
            delegate = delegate.delegate();
        }
        return delegate;
    }

    /** @return A type of {@link Identifier} that describes how to load this connection from NBT. This should probably
     *         be a cached value from {@link Identifier#getFor(Object)} */
    Identifier itentifier();

    /** @return All of the OTHER {@link IPowerConnection} that connect to this {@link IPowerConnection} */
    Map<Identifier, IPowerConnection> connections();

    /** Provides a notification the the given {@link IPowerTunnel} is using this relay to tunnel through. This will be
     * called when the power network is loaded from disk (even if nothing changed in the world) to let you add the
     * tunnel to your own data structures (if you use any for display etc) */
    void notifyTunnelConnected(IPowerTunnel tunnel);

    /** Provides a notification that the given {@link IPowerTunnel} is no longer using this relay, or has been broken in
     * some other way. */
    void notifyTunnelDisconnected(IPowerTunnel tunnel);

    // Useful types- you almost never want to ONLY implement IPowerConnection

    /** Indicates that this connection can relay power over itself. */
    public interface IPowerRelay extends IPowerConnection {
        /** Notifies this connection that the amount of RF transfered has changed from the previous value. This will
         * generally only be called from the concrete implementation of {@link IPowerTunnel}.
         * 
         * @param tunnel The tunnel that changed.
         * @param oldRf The old amount of RF that was flowing though the tunnel
         * @param newRf The new amount of RF that is now flowing through the tunnel
         * @param delta The difference between the old and new RF values. Positive means there is more RF flowing than
         *            before, negative means less than before (delta = newRF - oldRF) */
        void notifyDeltaChange(IPowerTunnel tunnel, int oldRf, int newRf, int delta);

        /** @return The maximum number of units that are allowed to flow through this relay. This will be implemented
         *         by */
        int maxUnitsTransfered();
    }

    /** Indicates that this connection supplies power to consumers. It also allows suppling power to the power
     * network. */
    public interface IPowerSupplier extends IPowerConnection {}

    /** Indicates that this connection requires power to operate. It also allows requesting power from the power
     * network.
     * <p>
     * The consumer MUST be the one to save the {@link IPowerTunnel} with the {@link IPowerNetwork} */
    public interface IPowerConsumer extends IPowerConnection {}

    // Composite/Special case types

    /** Indicates that this relay must be given a constant flow of power to relay more power over itself. */
    public interface IPowerCostlyRelay extends IPowerRelay, IPowerConsumer {
        /** @return True if the power requirements are satisfied for this relay to operate properly. Note that you can
         *         return 0 from {@link #traversalCost(IPowerConnection)} and return false from this if you have a
         *         different way of being activated */
        boolean satisfied();

        /** @param to The connection that is being attempted to traverse to. The connection will always be one returned
         *            from {@link #connections()}.
         * @param reverseRoute The route from the consumer to the supplier. This won't actually contain the supplier
         *            (unless you ARE a supplier) but will contain the cheapest (first priority), shortest route to the
         *            consumer from you.
         * @return The cost (in power flow) for traversing over this relay to a specific {@link IPowerConnection}. This
         *         value will be pushed DOWN (So if you return 0.1 and their are 9 of your {@link IPowerRelay} that add
         *         up to 0.9 units it will cost 0 units total to traverse) */
        double traversalCost(IPowerConnection to, List<IPowerConnection> reverseRoute);

        /** @return The type of cost that it takes to traverse to the given {@link IPowerConnection}. This will only be
         *         called if {@link #traversalCost(IPowerConnection)} returns an integer greater than 0. */
        default EnumPowerBar traversalCostType(IPowerConnection to) {
            return EnumPowerBar.FULL;
        }
    }
}
