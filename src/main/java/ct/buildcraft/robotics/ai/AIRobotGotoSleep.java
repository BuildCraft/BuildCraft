package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.IRobotRegistry;
import ct.buildcraft.api.robots.EntityRobotBase;

public class AIRobotGotoSleep extends AIRobot {
    public AIRobotGotoSleep(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public void start() {
        IRobotRegistry registry = robot.getRegistry();
        if (registry != null) {
            registry.releaseResources(robot);
        }

        DockingStation linkedStation = robot.getLinkedStation();
        if (linkedStation == null) {
            setSuccess(false);
            terminate();
            return;
        }
        startDelegateAI(new AIRobotGotoStation(robot, linkedStation));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStation) {
            if (ai.success()) {
                startDelegateAI(new AIRobotSleep(robot));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotSleep) {
            terminate();
        }
    }
}
