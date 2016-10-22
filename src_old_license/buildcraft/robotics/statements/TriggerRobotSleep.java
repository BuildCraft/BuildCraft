/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import java.util.List;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;

import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.RobotUtils;

public class TriggerRobotSleep extends BCStatement implements ITriggerInternal {

    public TriggerRobotSleep() {
        super("buildcraft:robot.sleep");
        setBuildCraftLocation("robotics", "triggers/trigger_robot_sleep");
    }

    @Override
    public String getDescription() {
        return StringUtilBC.localize("gate.trigger.robot.sleep");
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        List<DockingStation> stations = RobotUtils.getStations(container.getTile());

        for (DockingStation station : stations) {
            if (station.robotTaking() != null) {
                EntityRobot robot = (EntityRobot) station.robotTaking();

                if (robot.isActive()) {
                    return true;
                }
            }
        }

        return false;
    }
}
