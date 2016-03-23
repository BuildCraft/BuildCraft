package buildcraft.robotics.path;

import java.util.Set;

public interface IVirtualSpaceAccessor<K> {
    IVirtualPoint<K> getPoint(K key);

    /** Should be a VERY FAST ESTIMATE of the distance squared between the two points. Generally this should be the
     * lowest-possible-cost to go between the search point and a given point. */
    double heuristicCostBetween(IVirtualPoint<K> a, IVirtualPoint<K> b);

    /** Can be an expensive search of the underlying space to find the expense between two points. The two points MUST
     * be connected to each other, or you will get strange results. */
    double exactCostBetween(IVirtualPoint<K> a, IVirtualPoint<K> b);

    public interface IVirtualPoint<K> {
        Set<? extends IVirtualPoint<K>> getConnected();

        IVirtualSpaceAccessor<K> getSpace();

        K getPoint();
    }

    public interface IVirtualDestination<K> {
        boolean isDestination(IVirtualPoint<K> point);

        double heuristicCostToDestination(IVirtualPoint<K> from);
    }
}
