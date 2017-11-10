/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.filters.AggregateFilter;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.statements.ActionRobotFilterTool;

public class AIRobotFetchAndEquipItemStack extends AIRobot {

	private IStackFilter filter;
	private int delay = 0;

	public AIRobotFetchAndEquipItemStack(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotFetchAndEquipItemStack(EntityRobotBase iRobot, IStackFilter iFilter) {
		this(iRobot);

		filter = new AggregateFilter(ActionRobotFilterTool.getGateFilter(iRobot.getLinkedStation()), iFilter);
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotGotoStationToLoad(robot, filter, 1));
	}

	@Override
	public void update() {
		if (robot.getDockingStation() == null) {
			setSuccess(false);
			terminate();
		}

		if (delay++ > 40) {
			if (equipItemStack()) {
				terminate();
			} else {
				delay = 0;
				startDelegateAI(new AIRobotGotoStationToLoad(robot, filter, 1));
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoad) {
			if (filter == null) {
				// filter can't be retreived, usually because of a load operation.
				// Force a hard abort, preventing parent AI to continue normal
				// sequence of actions and possibly re-starting this.
				abort();
				return;
			}
			if (!ai.success()) {
				setSuccess(false);
				terminate();
			}
		}
	}

	private boolean equipItemStack() {
		IInventory tileInventory = robot.getDockingStation().getItemInput();
		if (tileInventory == null) {
			return false;
		}

		ItemStack possible = AIRobotLoad.takeSingle(robot.getDockingStation(), filter, true);
		if (possible == null) {
		    return false;
		}
		robot.setItemInUse(possible);
		return true;
	}
}
