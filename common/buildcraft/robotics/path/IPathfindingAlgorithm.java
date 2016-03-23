package buildcraft.robotics.path;

import java.util.List;

import buildcraft.core.lib.utils.IIterableAlgorithm;
import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualDestination;

public interface IPathfindingAlgorithm<K> extends IIterableAlgorithm {
    /** Iterates this algorithm once. The definition of "once" can vary between algorithms, so it is best to tune how
     * many times this is called on a per algorthm basis.
     * 
     * @return True if this pathfinding completed, false if not. */
    @Override
    boolean iterate();

    /** Gets a list of the points that will move from the start position to the end position. This will be null if no
     * path has been found yet.
     * 
     * @return */
    List<K> nextPoints();

    K start();

    IVirtualDestination<K> destination();

    IVirtualSpaceAccessor<K> space();
}
