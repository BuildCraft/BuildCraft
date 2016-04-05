package buildcraft.api.mj;

/** @date Created on 2 Apr 2016 by AlexIIL */
public interface IMjNetwork {
    /** Adds an "odd" machine -one that has connections to blocks further than 1 block away, and those block *might* not
     * also make a connection back to it. For example, engines can carry power forward and that is done by making a
     * connection to a block 1, 2 or 3 blocks infront of it. Due to the way the graph traversal algorithm works this is
     * not required for consumers. */
    void addOddMachine(IMjMachine machine);

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

    /** Checks to see if the given request still exists in the network. (eitehr its searching for
     * 
     * @param request
     * @return */
    boolean requestExists(IMjRequest request);

    /** @param milliWatts Asks the network for some power, in milliwatts.
     * @param requester the consumer that wants the power.
     * @return */
    IMjRequest makeRequest(int milliWatts, IMjMachineConsumer requester);

    /** Cancels this request, if a producer has not already been found.
     * 
     * @param request */
    void cancelRequest(IMjRequest request);
}
