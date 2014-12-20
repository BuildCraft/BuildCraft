/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import net.minecraft.util.EnumFacing;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.robots.AIRobotSleep;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;

public class TriggerRobotSleep extends BCStatement implements ITriggerInternal {

	public TriggerRobotSleep() {
		super("buildcraft:robot.sleep");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.robot.sleep");
	}

	/*@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/trigger_robot_sleep");
	}*/

	@Override
	public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
		if (!(container instanceof IGate)) {
			return false;
		}
		
		Pipe<?> pipe = (Pipe<?>) ((IGate) container).getPipe();
		TileGenericPipe tile = pipe.container;

		for (EnumFacing d : EnumFacing.values()) {
			DockingStation station = tile.getStation(d);

			if (station != null && station.robotTaking() != null) {
				EntityRobot robot = (EntityRobot) station.robotTaking();

				if (robot.mainAI.getActiveAI() instanceof AIRobotSleep) {
					return true;
				}
			}
		}

		return false;
	}
}
