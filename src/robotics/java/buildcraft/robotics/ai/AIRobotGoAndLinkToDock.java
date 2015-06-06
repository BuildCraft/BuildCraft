/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotGoAndLinkToDock extends AIRobot {

    private DockingStation station;

    public AIRobotGoAndLinkToDock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGoAndLinkToDock(EntityRobotBase iRobot, DockingStation iStation) {
        this(iRobot);

        station = iStation;
    }

    @Override
    public void start() {
        if (station == robot.getLinkedStation() && station == robot.getDockingStation()) {
            terminate();
        } else {
            if (station.takeAsMain(robot)) {
                startDelegateAI(new AIRobotGotoBlock(robot, station.x() + station.side().offsetX * 2, station.y() + station.side().offsetY * 2,
                    station.z() + station.side().offsetZ * 2));
            } else {
                setSuccess(false);
                terminate();
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            if (ai.success()) {
                startDelegateAI(new AIRobotStraightMoveTo(robot, station.x() + 0.5F + station.side().offsetX * 0.5F, station.y() + 0.5F
                    + station.side().offsetY * 0.5F, station.z() + 0.5F + station.side().offsetZ * 0.5F));
            } else {
                terminate();
            }
        } else if (ai instanceof AIRobotStraightMoveTo) {
            if (ai.success()) {
                robot.dock(station);
            }
            terminate();
        }
    }
}
