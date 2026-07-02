package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.IFluidFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;

public class AIRobotGotoStationAndLoadFluids extends AIRobot {
    private IFluidFilter filter;

    public AIRobotGotoStationAndLoadFluids(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotGotoStationAndLoadFluids(EntityRobotBase robot, IFluidFilter filter) {
        this(robot);
        this.filter = filter;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotGotoStationToLoadFluids(robot, filter));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationToLoadFluids) {
            if (filter != null && ai.success()) {
                startDelegateAI(new AIRobotLoadFluids(robot, filter));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotLoadFluids) {
            setSuccess(ai.success());
            terminate();
        }
    }
}
