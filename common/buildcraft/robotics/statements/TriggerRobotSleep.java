/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import net.minecraft.client.renderer.texture.IIconRegister;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.RobotUtils;
import buildcraft.robotics.ai.AIRobotSleep;

public class TriggerRobotSleep extends BCStatement implements ITriggerInternal {

	public TriggerRobotSleep() {
		super("buildcraft:robot.sleep");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.robot.sleep");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraftrobotics:triggers/trigger_robot_sleep");
	}

	@Override
	public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
		DockingStationIterator iterator = new DockingStationIterator(container);

		while (iterator.hasNext()) {
			DockingStation station = iterator.next();
			if (station.robotTaking() != null) {
				EntityRobot robot = (EntityRobot) station.robotTaking();

				if (robot.isAsleep()) {
					return true;
				}
			}
		}

		return false;
	}
}
