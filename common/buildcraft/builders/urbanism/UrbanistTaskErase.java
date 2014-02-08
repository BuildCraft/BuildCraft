package buildcraft.builders.urbanism;

public class UrbanistTaskErase extends UrbanistTask {

	int x, y, z;
	boolean isDone = false;
	boolean inBreak = false;

	public UrbanistTaskErase (TileUrbanist urbanist, int x, int y, int z) {
		super (urbanist);

		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setup(EntityRobotUrbanism robot) {
		//robot.setDestinationAround(x, y, z);
	}

	public void work(EntityRobotUrbanism robot) {
		if (!inBreak && robot.getDistance(x, y, z) <= 10) {
			inBreak = true;
			robot.setLaserDestination(x + 0.5F, y + 0.5F, z + 0.5F);
			robot.showLaser();
		}

		if (inBreak) {
			if (robot.breakBlock(x, y, z)) {
				isDone = true;
				robot.hideLaser();
			}
		}
	}

	public boolean done() {
		return isDone;
	}

}
