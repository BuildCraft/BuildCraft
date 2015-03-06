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
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.items.IMapLocation;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.RobotRegistry;
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
		icon = iconRegister.registerIcon("buildcraft:triggers/action_robot_goto_station");
	}

	@Override
	public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
		if (!(container.getTile() instanceof IPipeTile)) {
			return;
		}

		IPipeTile tile = (IPipeTile) container.getTile();
		RobotRegistry registry = (RobotRegistry) RobotManager.registryProvider.getRegistry(tile.getWorld());

		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
			DockingStation station = RobotUtils.getStation(tile, d);

			if (station != null && station.robotTaking() != null) {
				EntityRobot robot = (EntityRobot) station.robotTaking();
				AIRobot ai = robot.getOverridingAI();

				if (ai != null) {
					continue;
				}

				DockingStation newStation = station;

				if (parameters[0] != null) {
					StatementParameterItemStack stackParam = (StatementParameterItemStack) parameters[0];
					ItemStack item = stackParam.getItemStack();

					if (item != null && item.getItem() instanceof IMapLocation) {
						IMapLocation map = (IMapLocation) item.getItem();
						BlockIndex index = map.getPoint(item);

						if (index != null) {
							ForgeDirection side = map.getPointSide(item);
							DockingStation paramStation = (DockingStation)
									registry.getStation(index.x,
									index.y, index.z, side);

							if (paramStation != null) {
								newStation = paramStation;
							}
						}
					}
				}

				robot.overrideAI(new AIRobotGoAndLinkToDock(robot, newStation));
			}
		}
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
