package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;

public class AIRobotGotoStationAndLoad extends AIRobot {
    private IStackFilter filter;
    private int quantity;

    public AIRobotGotoStationAndLoad(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotGotoStationAndLoad(EntityRobotBase robot, IStackFilter filter, int quantity) {
        this(robot);
        this.filter = filter;
        this.quantity = quantity;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotGotoStationToLoad(robot, filter, quantity));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationToLoad) {
            if (filter != null && ai.success()) {
                startDelegateAI(new AIRobotLoad(robot, filter, quantity));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotLoad) {
            setSuccess(ai.success());
            terminate();
        }
    }
}
