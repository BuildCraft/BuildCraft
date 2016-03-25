package buildcraft.robotics.ai.path;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.BlockPosDestination;

public class AIRobotFindAndFollowPath extends AIRobot {
    private BlockPosDestination dest;

    public AIRobotFindAndFollowPath(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotFindAndFollowPath(EntityRobotBase robot, BlockPosDestination dest) {
        super(robot);
        this.dest = dest;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotFindPathShort(robot, dest));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotFindPathShort) {
            if (ai.success()) {
                AIRobotFindPathShort search = (AIRobotFindPathShort) ai;
                startDelegateAI(new AIRobotFollowPath(robot, search.getPath()));
            } else {
                // FIXME: Use the full destination rather than just the min!
                startDelegateAI(new AIRobotFindPathLong(robot, dest.min));
            }
        } else if (ai instanceof AIRobotFindPathLong) {
            if (ai.success()) {
                AIRobotFindPathLong search = (AIRobotFindPathLong) ai;
                startDelegateAI(new AIRobotFollowPathIncremental(robot, search.getPath(), dest));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotFollowPath) {
            setSuccess(ai.success());
            terminate();
        }
    }
}
