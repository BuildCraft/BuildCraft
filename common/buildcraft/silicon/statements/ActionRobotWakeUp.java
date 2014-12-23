/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.utils.StringUtils;

public class ActionRobotWakeUp extends BCStatement implements IActionInternal {

	public ActionRobotWakeUp() {
		super("buildcraft:robot.wakeup");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.robot.wakeup");
	}

	/*@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_robot_wakeup");
	}*/

	@Override
	public void actionActivate(IStatementContainer source,
			IStatementParameter[] parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSheetLocation() {
		// TODO Auto-generated method stub
		return 52;
	}
}
