package buildcraft.builders.urbanism;

import buildcraft.api.core.IBox;
import buildcraft.builders.blueprints.Blueprint;
import buildcraft.builders.filler.pattern.FillerPattern;

public class UrbanistTaskFill extends UrbanistTask {

	Blueprint bpt;

	public UrbanistTaskFill (TileUrbanist urbanist, IBox box, FillerPattern pattern) {
		super (urbanist);

		bpt = pattern.getBlueprint(box);
	}

	public void setup(EntityRobotUrbanism robot) {

	}

	public void work(EntityRobotUrbanism robot) {

	}

	public boolean done() {
		return true;
	}
}
