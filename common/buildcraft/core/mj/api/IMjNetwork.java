package buildcraft.core.mj.api;

/** @date Created on 2 Apr 2016 by AlexIIL */
public interface IMjNetwork {
    /** Removes the machine from the network. This will break all connections that rely on this machine automatically.
     * 
     * @param machine */
    void removeMachine(IMjMachine machine);

    /** Lets the network know that something changed about this machine which changes how it connects to other
     * machines. */
    void refreshMachine(IMjMachine machine);

    /** Checks to see if the given connection is active with the network. Most of the time you won't need this (broken
     * connections will call you to say that they have been broken), however this can be useful for making sure you are
     * setup properly after loading from disk. */
    boolean connectionExists(IMjConnection connection);

    /** Checks to see if the given request exists in the network.
     * 
     * @param request
     * @return */
    boolean requestExists(IMjRequest request);

    /** @param milliWatts Asks the network for some power, in milliwatts.
     * @param requester the consumer that wants the power.
     * @return */
    IMjRequest makeRequest(int milliWatts, IMjMachineConsumer requester);
}
