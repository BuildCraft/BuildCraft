package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.IZone;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.IStationFilter;

public class AIRobotSearchAndGotoStation extends AIRobot {
    private IStationFilter filter;
    private IZone zone;

    public AIRobotSearchAndGotoStation(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotSearchAndGotoStation(EntityRobotBase robot, IStationFilter filter, IZone zone) {
        this(robot);
        this.filter = filter;
        this.zone = zone;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotSearchStation(robot, filter, zone));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchStation search) {
            if (ai.success()) {
                startDelegateAI(new AIRobotGotoStation(robot, search.targetStation));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotGotoStation) {
            setSuccess(ai.success());
            terminate();
        }
    }
}
