package buildcraft.robotics.path;

import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualDestination;
import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualPoint;

public class SinglePointDestination<K> implements IVirtualDestination<K> {
    private final IVirtualSpaceAccessor<K> space;
    private final IVirtualPoint<K> end;

    public SinglePointDestination(IVirtualSpaceAccessor<K> space, IVirtualPoint<K> end) {
        this.space = space;
        this.end = end;
    }

    @Override
    public boolean isDestination(IVirtualPoint<K> point) {
        return point.equals(end);
    }

    @Override
    public double heuristicCostToDestination(IVirtualPoint<K> from) {
        return space.heuristicCostBetween(from, end);
    }
}
