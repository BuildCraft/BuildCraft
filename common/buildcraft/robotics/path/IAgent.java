package buildcraft.robotics.path;

public interface IAgent<K> {
    /** Gets the agents current position. */
    K getCurrentPos();

    /** Gets the agents intended destination. This should never return null, but you can return {@link #getCurrentPos()}
     * instead. */
    K getDestination();
}
