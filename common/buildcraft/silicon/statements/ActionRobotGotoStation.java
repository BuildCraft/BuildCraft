/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;


import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.robots.AIRobotGoAndLinkToDock;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.robots.RobotRegistry;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Gate;
import buildcraft.transport.Pipe;

public class ActionRobotGotoStation extends BCStatement implements IActionInternal {

	public ActionRobotGotoStation() {
		super("buildcraft:robot.goto_station");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.robot.goto_station");
	}

	/*@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_robot_goto_station");
	}*/

	@Override
	public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
		Pipe<?> pipe = ((Gate) container).pipe;
		RobotRegistry registry = RobotRegistry.getRegistry(pipe.getWorld());

		for (EnumFacing d : EnumFacing.values()) {
			DockingStation station = pipe.container.getStation(d);

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

					if (item != null && item.getItem() instanceof ItemMapLocation) {
						BlockPos index = ItemMapLocation.getBlockPos(item);

						if (index != null) {
							EnumFacing side = ItemMapLocation.getSide(item);
							DockingStation paramStation = (DockingStation)
									registry.getStation(index, side);

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

	@Override
	public int getSheetLocation() {
		// TODO Auto-generated method stub
		return 28;
	}

}
