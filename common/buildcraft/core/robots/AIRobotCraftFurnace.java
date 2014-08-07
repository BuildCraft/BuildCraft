/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import java.util.ArrayList;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.ArrayStackFilter;

public class AIRobotCraftFurnace extends AIRobotCraftGeneric {

	public AIRobotCraftFurnace(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	protected ArrayList<ArrayStackFilter> tryCraft(boolean doRemove) {
		// TODO Auto-generated method stub
		return null;
	}

	// How to operate furnaces
	// [1] identify a furnace
	// [2] verify that proper item is in. If empty, and slot out empty or
	// contains order get proper item, otherwise skip
	// [3] bring proper item and put in
	// [4] as soon as output contains expected item, get it and place it
	// somewhere

	// How to operate assembly tables

}
