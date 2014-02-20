/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import buildcraft.core.robots.EntityRobot;
import buildcraft.core.robots.IRobotTask;

public class UrbanistTask implements IRobotTask {

	protected TileUrbanist urbanist;

	public UrbanistTask (TileUrbanist urbanist) {
		this.urbanist = urbanist;
	}

	@Override
	public void setup(EntityRobot robot) {

	}

	@Override
	public void update(EntityRobot robot) {

	}

	@Override
	public boolean done() {
		return true;
	}
}
