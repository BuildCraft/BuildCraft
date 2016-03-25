package buildcraft.robotics.ai.path;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.BCWorkerThreads;
import buildcraft.robotics.path.BlockPosDestination;
import buildcraft.robotics.path.WorldAccessor;

public class AIRobotFindPathShort extends AIRobot {
    private WorldAccessor accessor;
    private RobotAgent robotAgent;
    private List<BlockPos> path = null;
    private TaskFindPathSmall task = null;

    public AIRobotFindPathShort(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotFindPathShort(EntityRobotBase robot, BlockPos destination) {
        super(robot);
        accessor = new WorldAccessor(robot.getEntityWorld());
        robotAgent = new RobotAgent(robot.getPosition(), destination);
    }

    public AIRobotFindPathShort(EntityRobotBase robot, BlockPosDestination destination) {
        super(robot);
        accessor = new WorldAccessor(robot.getEntityWorld());
        robotAgent = new RobotAgent(robot.getPosition(), destination);
    }

    @Override
    public void update() {
        if (accessor == null) {
            accessor = new WorldAccessor(robot.getEntityWorld());
        }
        if (task == null) {
            task = new TaskFindPathSmall(robotAgent, accessor);
            BCWorkerThreads.executeWorkTask(task);
        }
        if (task.isDone()) {
            path = task.getPath();
            setSuccess(path != null && path.size() > 0);
            terminate();
        }
    }

    public List<BlockPos> getPath() {
        return path;
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        robotAgent = RobotAgent.loadFromNBT(nbt.getCompoundTag("robotAgent"));
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        nbt.setTag("robotAgent", robotAgent.writeToNBT());
    }
}
