/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.items.IMapLocation;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IRobotRegistry;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.statements.BCStatement;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.RobotUtils;
import buildcraft.robotics.ai.AIRobotGoAndLinkToDock;

public class ActionRobotGotoStation extends BCStatement implements IActionInternal {

	public ActionRobotGotoStation() {
		super("buildcraft:robot.goto_station");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.robot.goto_station");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraftrobotics:triggers/action_robot_goto_station");
	}

	@Override
	public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
		IRobotRegistry registry = RobotManager.registryProvider.getRegistry(container.getTile()
				.getWorldObj());

		List<DockingStation> stations = RobotUtils.getStations(container.getTile());

		for (DockingStation station : stations) {
			if (station.robotTaking() != null) {
				EntityRobot robot = (EntityRobot) station.robotTaking();
				AIRobot ai = robot.getOverridingAI();

				if (ai != null) {
					continue;
				}

				DockingStation newStation = station;

				if (parameters[0] != null) {
					newStation = getStation((StatementParameterItemStack) parameters[0], registry);
				}

				if (newStation != null) {
					robot.overrideAI(new AIRobotGoAndLinkToDock(robot, newStation));
				}
			}
		}
	}

	private DockingStation getStation(StatementParameterItemStack stackParam,
									  IRobotRegistry registry) {
		ItemStack item = stackParam.getItemStack();

		if (item != null && item.getItem() instanceof IMapLocation) {
			IMapLocation map = (IMapLocation) item.getItem();
			BlockIndex index = map.getPoint(item);

			if (index != null) {
				ForgeDirection side = map.getPointSide(item);
				DockingStation paramStation = registry.getStation(index.x, index.y, index.z, side);

				if (paramStation != null) {
					return paramStation;
				}
			}
		}
		return null;
	}

	@Override
	public int maxParameters() {
		return 1;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new StatementParameterItemStack();
	}

}
