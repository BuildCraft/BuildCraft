package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;

public class AIRobotGotoStationAndUnloadFluids extends AIRobot {
    private boolean requireAcceptAction;

    public AIRobotGotoStationAndUnloadFluids(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotGotoStationAndUnloadFluids(EntityRobotBase robot, boolean requireAcceptAction) {
        this(robot);
        this.requireAcceptAction = requireAcceptAction;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotGotoStationToUnloadFluids(robot, requireAcceptAction));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationToUnloadFluids) {
            if (ai.success()) {
                startDelegateAI(new AIRobotUnloadFluids(robot, requireAcceptAction));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotUnloadFluids) {
            setSuccess(ai.success());
            terminate();
        }
    }
}
