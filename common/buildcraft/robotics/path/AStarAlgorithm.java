package buildcraft.robotics.path;

import java.util.*;

import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualPoint;

/** Provides the A-Star pathfinding algorithm. The agent is NOT kept, so you will need to construct a new object
 * whenever the */
public class AStarAlgorithm<K> implements IPathfindingAlgorithm<K> {
    public final IVirtualSpaceAccessor<K> accessor;

    private final IVirtualPoint<K> start, end;
    // TODO Public-> private
    public final Set<IVirtualPoint<K>> closedSet = new HashSet<>();
    public final Set<IVirtualPoint<K>> openSet = new HashSet<>();
    public final Map<IVirtualPoint<K>, IVirtualPoint<K>> cameFrom = new HashMap<>();
    private final List<K> totalPath = new ArrayList<>();

    final Map<IVirtualPoint<K>, Double> gScore;
    final Map<IVirtualPoint<K>, Double> fScore;

    public AStarAlgorithm(IVirtualSpaceAccessor<K> accessor, IAgent<K> agent) {
        this.accessor = accessor;

        this.start = accessor.getPoint(agent.getCurrentPos());
        this.end = accessor.getPoint(agent.getDestination());

        openSet.add(start);

        gScore = new HashMapWithDefault<>(new Double(Double.POSITIVE_INFINITY));
        fScore = new HashMapWithDefault<>(new Double(Double.POSITIVE_INFINITY));

        gScore.put(start, 0.0);
        fScore.put(start, accessor.heuristicCostBetween(start, end));
    }

    @Override
    public K start() {
        return start.getPoint();
    }

    @Override
    public K destination() {
        return end.getPoint();
    }

    @Override
    public boolean iterate() {
        IVirtualPoint<K> current = getLowestValue(openSet, fScore);
        if (current == null) return true;
        if (current == end) {
            totalPath.clear();
            totalPath.addAll(reconstructPath(cameFrom, end));
            return true;
        }
        openSet.remove(current);
        closedSet.add(current);
        for (IVirtualPoint<K> neighbour : current.getConnected()) {
            if (closedSet.contains(neighbour)) {
                continue;
            }
            double tentativeGScore = gScore.get(current) + accessor.exactCostBetween(current, neighbour);
            if (!openSet.contains(neighbour)) {
                openSet.add(neighbour);
            } else if (tentativeGScore >= gScore.get(neighbour)) {
                continue;
            }

            cameFrom.put(neighbour, current);
            gScore.put(neighbour, tentativeGScore);
            fScore.put(neighbour, tentativeGScore + accessor.heuristicCostBetween(neighbour, end));
        }
        return false;
    }

    private static <K> List<K> reconstructPath(Map<IVirtualPoint<K>, IVirtualPoint<K>> cameFrom, IVirtualPoint<K> current) {
        List<K> totalPath = new ArrayList<>();
        totalPath.add(current.getPoint());

        while (true) {
            current = cameFrom.get(current);
            if (current == null || totalPath.contains(current)) return totalPath;
            totalPath.add(current.getPoint());
        }
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
    public AStarAlgorithm<K> reset(IAgent<K> agent) {
        IVirtualPoint<K> start = accessor.getPoint(agent.getCurrentPos());
        IVirtualPoint<K> end = accessor.getPoint(agent.getDestination());

        if (start.equals(this.start) && end.equals(this.end)) {
            totalPath.clear();

            openSet.clear();
            openSet.add(start);

            gScore.clear();
            fScore.clear();

            gScore.put(start, 0.0);
            fScore.put(start, accessor.heuristicCostBetween(start, end));
            return this;
        }

        return new AStarAlgorithm<>(accessor, agent);
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

    @Override
    public IAlgorthmFactory<K> factory() {
        return AStarAlgorithm::new;
    }
}
