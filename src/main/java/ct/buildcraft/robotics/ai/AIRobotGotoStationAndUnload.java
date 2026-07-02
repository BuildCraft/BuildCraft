package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;

public class AIRobotGotoStationAndUnload extends AIRobot {
    private DockingStation station;
    private boolean requireAcceptAction;

    public AIRobotGotoStationAndUnload(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotGotoStationAndUnload(EntityRobotBase robot, boolean requireAcceptAction) {
        this(robot);
        this.requireAcceptAction = requireAcceptAction;
    }

    public AIRobotGotoStationAndUnload(EntityRobotBase robot, DockingStation station) {
        super(robot);
        this.station = station;
    }

    public AIRobotGotoStationAndUnload(EntityRobotBase robot, DockingStation station, boolean requireAcceptAction) {
        this(robot, station);
        this.requireAcceptAction = requireAcceptAction;
    }

    @Override
    public void start() {
        if (station == null) {
            startDelegateAI(new AIRobotGotoStationToUnload(robot, requireAcceptAction));
        } else {
            startDelegateAI(new AIRobotGotoStation(robot, station));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationToUnload || ai instanceof AIRobotGotoStation) {
            if (ai.success()) {
                startDelegateAI(new AIRobotUnload(robot, requireAcceptAction));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotUnload) {
            setSuccess(ai.success());
            terminate();
        }
    }
}
