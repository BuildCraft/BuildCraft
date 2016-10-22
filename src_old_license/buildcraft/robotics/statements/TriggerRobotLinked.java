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
import buildcraft.robotics.RobotUtils;

public class TriggerRobotLinked extends BCStatement implements ITriggerInternal {
    private final boolean reserved;

    public TriggerRobotLinked(boolean reserved) {
        super("buildcraft:robot." + (reserved ? "reserved" : "linked"));
        setBuildCraftLocation("robotics", "triggers/trigger_robot_" + (reserved ? "reserved" : "linked"));
        this.reserved = reserved;
    }

    @Override
    public String getDescription() {
        return StringUtilBC.localize("gate.trigger.robot." + (reserved ? "reserved" : "linked"));
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        List<DockingStation> stations = RobotUtils.getStations(container.getTile());

        for (DockingStation station : stations) {
            if (station.isTaken() && (reserved || station.isMainStation())) {
                return true;
            }
        }

        return false;
    }
}
