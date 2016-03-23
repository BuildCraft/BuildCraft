package buildcraft.robotics.path;

import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualDestination;

public interface IAgent<K> {
    /** Gets the agents current position. */
    K getCurrentPos();

    /** Gets the agents intended destination. This should never return null, but you can return {@link #getCurrentPos()}
     * wrapped in a singlePointDestination */
    IVirtualDestination<K> getDestination();
}
