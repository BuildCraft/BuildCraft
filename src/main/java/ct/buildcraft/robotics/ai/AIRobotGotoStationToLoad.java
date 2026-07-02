package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.IStationFilter;

public class AIRobotGotoStationToLoad extends AIRobot {
    private IStackFilter filter;
    private int quantity;

    public AIRobotGotoStationToLoad(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotGotoStationToLoad(EntityRobotBase robot, IStackFilter filter, int quantity) {
        this(robot);
        this.filter = filter;
        this.quantity = quantity;
    }

    @Override
    public void update() {
        startDelegateAI(new AIRobotSearchAndGotoStation(robot, new StationFilter(), robot.getZoneToLoadUnload()));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoStation) {
            setSuccess(ai.success());
            terminate();
        }
    }

    private class StationFilter implements IStationFilter {
        @Override
        public boolean matches(DockingStation station) {
            return AIRobotLoad.load(robot, station, filter, quantity, false);
        }
    }
}
