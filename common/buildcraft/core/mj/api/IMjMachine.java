package buildcraft.core.mj.api;

import java.util.Set;

/** Base interface for all of the machines. Do NOT extend this yourself as its hardcoded everywhere that you must be one
 * of the given subinterfaces of this. */
public interface IMjMachine {
    IConnectionLogic getConnectionLogic();

    /** @return A set of identifiers that will find this machine in the world. This is used as a key to find this
     *         machine so you should return the same key every time, and */
    Set<MjMachineIdentifier> getIdentifiers();

    /** Creates a connection that will deliver power either to, through or from you. This connection isn't transferring
     * power to the destination yet, but it should be flowing out from the producer. This is after a path for power has
     * been found, but before it is setup.
     * 
     * @param connection
     * @return True to allow the connection to setup and build, false if you don't want to allow power to flow from any
     *         of the connections. */
    boolean onConnectionCreate(IMjConnection connection);

    /** Provides a notification that the connection has been completed and now all of the input power is flowing into
     * the consumer.
     * 
     * @param connection */
    void onConnectionActivate(IMjConnection connection);

    /** Provides a notification that the connection has been broken, for whatever reason.
     * 
     * @param connection */
    void onConnectionBroken(IMjConnection connection);
}
