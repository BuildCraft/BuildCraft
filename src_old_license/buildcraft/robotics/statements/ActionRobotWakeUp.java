/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.core.statements.BCStatement;

public class ActionRobotWakeUp extends BCStatement implements IActionInternal {

    public ActionRobotWakeUp() {
        super("buildcraft:robot.wakeup");
        setBuildCraftLocation("robotics", "triggers/action_robot_wakeup");
    }

    @Override
    public String getDescription() {
        return BCStringUtils.localize("gate.action.robot.wakeup");
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}
}
