package buildcraft.robotics.ai.path;

import java.util.List;

import net.minecraft.util.BlockPos;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.BCWorkerThreads;
import buildcraft.robotics.path.BlockPosDestination;
import buildcraft.robotics.path.MiniChunkGraph.MiniChunkNode;
import buildcraft.robotics.path.MiniChunkSpaceAccessor;

public class AIRobotFindPathLong extends AIRobot {
    private BlockPosDestination destination;
    private MiniChunkSpaceAccessor accessor;
    private List<MiniChunkNode> path = null;
    private TaskFindPathLong task = null;

    public AIRobotFindPathLong(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotFindPathLong(EntityRobotBase robot, BlockPos destination) {
        super(robot);
        this.destination = new BlockPosDestination(destination, destination);
    }

    // public AIRobotFindPathLong(EntityRobotBase robot, BlockPosDestination destination) {
    // super(robot);
    // accessor = new MiniChunkSpaceAccessor(robot.getEntityWorld());
    // robotAgent = RobotAgentMultiChunk.create(robot.getPosition(), destination);
    // }

    @Override
    public void update() {
        if (accessor == null) {
            accessor = new MiniChunkSpaceAccessor(robot.getEntityWorld());
        }
        if (task == null) {
            task = new TaskFindPathLong(robot, destination, accessor);
            BCWorkerThreads.executeDependantTask(task);
        }
        if (task.isDone()) {
            path = task.getPath();
            setSuccess(path != null && path.size() > 0);
            terminate();
        }
    }

    public List<MiniChunkNode> getPath() {
        return path;
    }
}
