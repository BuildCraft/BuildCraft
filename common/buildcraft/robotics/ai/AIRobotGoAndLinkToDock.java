/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.Utils;

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
                startDelegateAI(new AIRobotGotoBlock(robot, station.getPos().offset(station.side(), 2)));
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
                startDelegateAI(new AIRobotStraightMoveTo(robot, Utils.convertMiddle(station.getPos()).add(Utils.convert(station.side(), 0.5))));
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
