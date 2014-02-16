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
import buildcraft.builders.urbanism.TileUrbanist.FrameTask;

public class UrbanistTaskBuildSchematic extends UrbanistTask {

	SchematicBuilder builder;
	boolean inBuild = false;
	FrameTask task;

	public UrbanistTaskBuildSchematic (TileUrbanist urbanist, SchematicBuilder builder, FrameTask task) {
		super (urbanist);

		this.builder = builder;
		this.task = task;
	}

	@Override
	public void setup(EntityRobotUrbanism robot) {
		//robot.setDestinationAround(builder.getX(), builder.getY(), builder.getZ());
	}

	@Override
	public void work(EntityRobotUrbanism robot) {
		if (!inBuild && robot.getDistance(builder.getX(), builder.getY(), builder.getZ()) <= 10) {
			inBuild = true;
		}

		if (inBuild) {
			if (builder.build(robot)) {
				task.taskDone();
			}
		}
	}

	@Override
	public boolean done() {
		return builder.isComplete();
	}

}
