package buildcraft.robotics.path;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IPathfindingAlgorithm<K> {
    /** Iterates this algorithm once. The definition of "once" can vary between algorithms, so it is best to tune how
     * many times this is called on a per algorthm basis.
     * 
     * @return True if this pathfinding completed, false if not. */
    boolean iterate();

    /** Gets a list of the points that will move from the start position to the end position. This will be null if no
     * path has been found yet.
     * 
     * @return */
    List<K> nextPoints();

    K start();

    K destination();

    /** Returns an immutable list of the points that the agent must visit to get to its destination from its current
     * position. If no path has been found then this will return [current, destination]. */
    default ImmutableList<K> getPointsForAgent(IAgent<K> agent) {
        List<K> points = nextPoints();
        if (points == null || points.isEmpty()) return ImmutableList.of(agent.getCurrentPos(), agent.getDestination());
        if (points.get(0).equals(agent.getCurrentPos())) return ImmutableList.copyOf(points);
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).equals(agent.getCurrentPos())) {
                return ImmutableList.copyOf(points.subList(i, points.size()));
            }
        }
        return ImmutableList.copyOf(points);
    }

    /** Resets this algorithm, but only if the agents destination position has changed or it has deviated from the path
     * given by {@link #nextPoints()}. */
    default IPathfindingAlgorithm<K> resetIfNeeded(IAgent<K> agent) {
        if (!agent.getDestination().equals(destination())) {
            return reset(agent);
        }
        K point = agent.getCurrentPos();
        if (start().equals(point)) return this;
        List<K> points = nextPoints();
        if (points == null || points.isEmpty()) return this;
        if (points.contains(point)) return this;
        return reset(agent);
    }

    default IPathfindingAlgorithm<K> reset(IAgent<K> agent) {
        return factory().createNew(space(), agent);
    }

    IVirtualSpaceAccessor<K> space();

    /** @return A factory that can create new instances of this algorithm. */
    IAlgorthmFactory<K> factory();

    public interface IAlgorthmFactory<K> {
        IPathfindingAlgorithm<K> createNew(IVirtualSpaceAccessor<K> space, IAgent<K> agent);
    }
}
