package buildcraft.robotics.ai.path;

import java.util.List;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.AStarAlgorithm;
import buildcraft.robotics.path.BlockPosDestination;
import buildcraft.robotics.path.IPathfindingAlgorithm;
import buildcraft.robotics.path.MiniChunkGraph.MiniChunkNode;
import buildcraft.robotics.path.MiniChunkSpaceAccessor;

public class TaskFindPathLong implements Runnable {
    private static final int MAX_ITERATIONS = 1000;

    private List<MiniChunkNode> path = null;
    private volatile boolean done;
    private EntityRobotBase robot;
    private BlockPosDestination destination;
    private MiniChunkSpaceAccessor accessor;
    private IPathfindingAlgorithm<MiniChunkNode> algorithm;

    public TaskFindPathLong(EntityRobotBase robot, BlockPosDestination destination, MiniChunkSpaceAccessor accessor) {
        this.robot = robot;
        this.destination = destination;
        this.accessor = accessor;
    }

    @Override
    public void run() {
        RobotAgentMultiChunk agent = RobotAgentMultiChunk.create(robot.worldObj, accessor, robot.getPosition(), destination.min);
        algorithm = new AStarAlgorithm<>(accessor, agent);

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

    public List<MiniChunkNode> getPath() {
        return path;
    }
}
