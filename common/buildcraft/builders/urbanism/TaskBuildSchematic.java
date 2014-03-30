/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import buildcraft.builders.urbanism.TileUrbanist.FrameTask;
import buildcraft.core.blueprints.BuildingSlotBlock;
import buildcraft.core.robots.AIMoveAround;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.robots.IRobotTask;

public class TaskBuildSchematic implements IRobotTask {

	BuildingSlotBlock builder;
	boolean inBuild = false;
	FrameTask task;

	public TaskBuildSchematic (BuildingSlotBlock builder, FrameTask task) {
		this.builder = builder;
		this.task = task;
	}

	@Override
	public void setup(EntityRobot robot) {
		robot.currentAI = new AIMoveAround(robot, builder.x, builder.y, builder.z);
	}

	@Override
	public void update(EntityRobot robot) {
		if (!inBuild && robot.getDistance(builder.x, builder.y, builder.z) <= 15) {
			inBuild = true;
			robot.setLaserDestination(builder.x, builder.y, builder.z);
			robot.showLaser();
		}

		if (inBuild) {
			// TODO: need to migrate this to a system based on the new
			// blueprints.
			/*if (builder.build((IBlueprintBuilderAgent) robot)) {
				task.taskDone();
				robot.hideLaser();
			}*/
		}
	}

	@Override
	public boolean done() {
		// TODO: need to migrate this to a system based on the new
		// blueprints.
		//return builder.isComplete();

		return true;
	}

}
