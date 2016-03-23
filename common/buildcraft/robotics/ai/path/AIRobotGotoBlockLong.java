package buildcraft.robotics.ai.path;

import net.minecraft.util.BlockPos;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotGoto;

public class AIRobotGotoBlockLong extends AIRobotGoto {
    public final BlockPos from;
    public final BlockPos to;
    public final int maxDistance;

    public AIRobotGotoBlockLong(EntityRobotBase iRobot, BlockPos to, int maxDistance) {
        super(iRobot);
        this.from = iRobot.getPosition();
        this.to = to;
        this.maxDistance = maxDistance;
    }
}
