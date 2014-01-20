package buildcraft.builders.urbanism;

import buildcraft.api.core.SafeTimeTracker;

public class UrbanistTaskErase extends UrbanistTask {

	int x, y, z;
	boolean isDone = false;
	SafeTimeTracker tracker = null;

	public UrbanistTaskErase (TileUrbanist urbanist, int x, int y, int z) {
		super (urbanist);

		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setup(EntityRobotUrbanism robot) {
		robot.setDestinationAround(x, y, z);
	}

	public void work(EntityRobotUrbanism robot) {
		if (tracker == null && robot.getDistance(x, y, z) <= 10) {
			tracker = new SafeTimeTracker(500);
			tracker.markTime(robot.worldObj);
			robot.setLaserDestination(x + 0.5F, y + 0.5F, z + 0.5F);
			robot.showLaser();
		}

		if (tracker != null && tracker.markTimeIfDelay(robot.worldObj)) {
			robot.worldObj.setBlock(x, y, z, 0);
			isDone = true;
			tracker = null;
			robot.hideLaser();
		}
	}

	public boolean done() {
		return isDone;
	}

}
