package buildcraft.robotics.ai.path;

import java.util.List;

import net.minecraft.util.BlockPos;

import buildcraft.robotics.path.AStarAlgorithm;
import buildcraft.robotics.path.IAgent;
import buildcraft.robotics.path.IPathfindingAlgorithm;
import buildcraft.robotics.path.IVirtualSpaceAccessor;

public class TaskFindPathSmall implements Runnable {
    private static final int MAX_ITERATIONS = 1000;

    private List<BlockPos> path = null;
    private volatile boolean done;
    private IPathfindingAlgorithm<BlockPos> algorithm;

    public TaskFindPathSmall(IAgent<BlockPos> agent, IVirtualSpaceAccessor<BlockPos> accessor) {
        algorithm = new AStarAlgorithm<>(accessor, agent);
    }

    @Override
    public void run() {
        int times = 0;
        while (!algorithm.iterate()) {
            times++;
            if (times >= MAX_ITERATIONS) break;
        }
        path = algorithm.nextPoints();
        done = true;
    }

    public boolean isDone() {
        return done;
    }

    public List<BlockPos> getPath() {
        return path;
    }
}
