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

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.ArrayStackFilter;

public abstract class AIRobotCraftGeneric extends AIRobot {

	protected boolean crafted = false;
	private ArrayList<ArrayStackFilter> requirements = new ArrayList<ArrayStackFilter>();

	public AIRobotCraftGeneric(EntityRobotBase iRobot) {
		super(iRobot);
	}

	protected abstract ArrayList<ArrayStackFilter> tryCraft(boolean doRemove);

	@Override
	public void end() {
		robot.releaseResources();
	}

	@Override
	public boolean success() {
		return crafted;
	}

	@Override
	public double getEnergyCost() {
		return 3;
	}

}
