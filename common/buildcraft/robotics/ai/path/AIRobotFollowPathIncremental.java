package buildcraft.robotics.ai.path;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.util.BlockPos;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.BCWorkerThreads;
import buildcraft.robotics.path.BlockPosDestination;
import buildcraft.robotics.path.BlockSpaceAccessor;
import buildcraft.robotics.path.MiniChunkGraph.MiniChunkNode;

public class AIRobotFollowPathIncremental extends AIRobot {
    private final LinkedList<MiniChunkNode> list = new LinkedList<>();
    private BlockPosDestination destination;
    private MiniChunkNode current;
    private TaskFindPathSmall findPath;

    public AIRobotFollowPathIncremental(EntityRobotBase robot, List<MiniChunkNode> list, BlockPosDestination destination) {
        super(robot);
        this.list.addAll(list);
        this.destination = destination;
    }

    @Override
    public void update() {
        if (list.size() < 2) {
            startDelegateAI(new AIRobotFindAndFollowPath(robot, destination));
        } else if (current == null) {
            current = list.getFirst();
            if (current.contains(robot.getPosition())) {
                MiniChunkNode next = list.get(1);
                BlockPosDestination incrementalDest = getConnectingParts(next);
                findPath = new TaskFindPathSmall(new RobotAgent(robot.getPosition(), incrementalDest), new BlockSpaceAccessor(current));
                BCWorkerThreads.executeWorkTask(findPath);
            } else {
                throw new IllegalStateException("Unknown/wrong position!");
            }
        } else if (findPath.isDone()) {
            List<BlockPos> path = findPath.getPath();
            if (path == null || path.isEmpty()) {
                setSuccess(false);
                terminate();
            } else {
                startDelegateAI(new AIRobotFollowPath(robot, findPath.getPath()));
            }
        }
    }

    private static BlockPosDestination getConnectingParts(MiniChunkNode to) {
        return new BlockPosDestination(to.getParent().min, to.getParent().min.add(15, 15, 15));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotFindAndFollowPath) {
            setSuccess(ai.success());
            terminate();
        } else if (ai instanceof AIRobotFollowPath) {
            list.removeFirst();
            current = null;
        }
    }
}
