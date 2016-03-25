package buildcraft.robotics.path;

import java.util.*;

import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualDestination;
import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualPoint;

/** Provides the A-Star pathfinding algorithm. */
public class AStarAlgorithm<K> implements IPathfindingAlgorithm<K> {
    public final IVirtualSpaceAccessor<K> accessor;

    private final IVirtualPoint<K> start;
    private final IVirtualDestination<K> destination;

    private final Set<IVirtualPoint<K>> closedSet = new HashSet<>();
    private final Set<IVirtualPoint<K>> openSet = new HashSet<>();
    private final Map<IVirtualPoint<K>, IVirtualPoint<K>> cameFrom = new HashMap<>();
    private final List<K> totalPath = new ArrayList<>();

    final Map<IVirtualPoint<K>, Double> gScore;
    final Map<IVirtualPoint<K>, Double> fScore;

    public AStarAlgorithm(IVirtualSpaceAccessor<K> accessor, IAgent<K> agent) {
        this.accessor = accessor;

        this.start = accessor.getPoint(agent.getCurrentPos());
        this.destination = agent.getDestination();

        openSet.add(start);

        gScore = new HashMapWithDefault<>(new Double(Double.POSITIVE_INFINITY));
        fScore = new HashMapWithDefault<>(new Double(Double.POSITIVE_INFINITY));

        gScore.put(start, 0.0);
        fScore.put(start, destination.heuristicCostToDestination(start));
    }

    @Override
    public K start() {
        return start.getPoint();
    }

    @Override
    public IVirtualDestination<K> destination() {
        return destination;
    }

    private IVirtualPoint<K> current = null;
    private Iterator<? extends IVirtualPoint<K>> currentNeighbours;

    @Override
    public boolean iterate() {
        if (current == null) {
            current = getLowestValue(openSet, fScore);
            if (current == null) return true;
            if (destination.isDestination(current)) {
                totalPath.clear();
                totalPath.addAll(reconstructPath(cameFrom, current));
                return true;
            }
            openSet.remove(current);
            closedSet.add(current);
            currentNeighbours = current.getConnected().iterator();
        }

        if (currentNeighbours.hasNext()) {
            IVirtualPoint<K> neighbour = currentNeighbours.next();
            if (closedSet.contains(neighbour)) {
                return false;
            }
            double tentativeGScore = gScore.get(current) + accessor.exactCostBetween(current, neighbour);
            if (!openSet.contains(neighbour)) {
                openSet.add(neighbour);
            } else if (tentativeGScore >= gScore.get(neighbour)) {
                return false;
            }

            cameFrom.put(neighbour, current);
            gScore.put(neighbour, tentativeGScore);
            fScore.put(neighbour, tentativeGScore + destination.heuristicCostToDestination(neighbour));
        } else {
            current = null;
        }
        return false;
    }

    private static <K> List<K> reconstructPath(Map<IVirtualPoint<K>, IVirtualPoint<K>> cameFrom, IVirtualPoint<K> current) {
        List<K> endToStart = new ArrayList<>();
        endToStart.add(current.getPoint());

        while (true) {
            current = cameFrom.get(current);
            if (current == null || endToStart.contains(current)) break;
            endToStart.add(current.getPoint());
        }

        List<K> startToEnd = new ArrayList<>();
        for (int i = endToStart.size() - 1; i >= 0; i--) {
            startToEnd.add(endToStart.get(i));
        }
        return startToEnd;
    }

    private static <K> IVirtualPoint<K> getLowestValue(Set<IVirtualPoint<K>> set, Map<IVirtualPoint<K>, Double> map) {
        IVirtualPoint<K> lowestPoint = null;
        double lowestValue = Double.POSITIVE_INFINITY;

        for (IVirtualPoint<K> point : set) {
            double val = map.get(point).doubleValue();
            if (map.get(point).doubleValue() < lowestValue) {
                lowestPoint = point;
                lowestValue = val;
            }
        }

        return lowestPoint;
    }

    @Override
    public List<K> nextPoints() {
        if (totalPath.isEmpty()) return null;
        return totalPath;
    }

    @Override
    public IVirtualSpaceAccessor<K> space() {
        return accessor;
    }
}
