/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import buildcraft.builders.blueprints.BlueprintBuilder.SchematicBuilder;
import buildcraft.builders.blueprints.IBlueprintBuilderAgent;
import buildcraft.builders.urbanism.TileUrbanist.FrameTask;
import buildcraft.core.robots.AIMoveAround;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.robots.IRobotTask;

public class TaskBuildSchematic implements IRobotTask {

	SchematicBuilder builder;
	boolean inBuild = false;
	FrameTask task;

	public TaskBuildSchematic (SchematicBuilder builder, FrameTask task) {
		this.builder = builder;
		this.task = task;
	}

	@Override
	public void setup(EntityRobot robot) {
		robot.currentAI = new AIMoveAround(robot, builder.getX(), builder.getY(), builder.getZ());
	}

	@Override
	public void update(EntityRobot robot) {
		if (!inBuild && robot.getDistance(builder.getX(), builder.getY(), builder.getZ()) <= 15) {
			inBuild = true;
			robot.setLaserDestination(builder.getX(), builder.getY(), builder.getZ());
			robot.showLaser();
		}

		if (inBuild) {
			if (builder.build((IBlueprintBuilderAgent) robot)) {
				task.taskDone();
				robot.hideLaser();
			}
		}
	}

	@Override
	public boolean done() {
		return builder.isComplete();
	}

}
