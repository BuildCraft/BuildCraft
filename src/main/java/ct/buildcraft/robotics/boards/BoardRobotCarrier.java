package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.ai.AIRobotGotoSleep;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import ct.buildcraft.robotics.ai.AIRobotLoad;
import ct.buildcraft.robotics.statements.ActionRobotFilter;

public class BoardRobotCarrier extends RedstoneBoardRobot {
    public BoardRobotCarrier(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("carrier").nbt();
    }

    @Override
    public void update() {
        if (!robot.containsItems()) {
            IStackFilter filter = ActionRobotFilter.getGateFilter(robot.getLinkedStation());
            startDelegateAI(new AIRobotGotoStationAndLoad(robot, filter, AIRobotLoad.ANY_QUANTITY));
        } else {
            startDelegateAI(new AIRobotGotoStationAndUnload(robot, true));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndLoad) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoStationAndUnload) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoSleep) {
            terminate();
        }
    }
}
