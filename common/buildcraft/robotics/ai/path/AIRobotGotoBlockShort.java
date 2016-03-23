package buildcraft.robotics.ai.path;

import net.minecraft.util.BlockPos;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotGoto;
import buildcraft.robotics.path.BlockPosDestination;

public class AIRobotGotoBlockShort extends AIRobotGoto {
    public BlockPos from;
    public BlockPosDestination to;
    public int maxDistance;

    public AIRobotGotoBlockShort(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGotoBlockShort(EntityRobotBase iRobot, BlockPosDestination to, int maxDistance) {
        super(iRobot);
        this.from = iRobot.getPosition();
        this.to = to;
        this.maxDistance = maxDistance;
    }
}
