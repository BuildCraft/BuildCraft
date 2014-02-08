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

	public void setup(EntityRobotUrbanism robot) {
		//robot.setDestinationAround(builder.getX(), builder.getY(), builder.getZ());
	}

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

	public boolean done() {
		return builder.isComplete();
	}

}
