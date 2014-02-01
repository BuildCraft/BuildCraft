package buildcraft.core;

import buildcraft.api.core.Position;

public class LaserData {
	public Position head = new Position (0, 0, 0), tail = new Position(0, 0, 0);
	public double renderSize = 0;
	public double angleY = 0;
	public double angleZ = 0;
	public boolean isVisible = true;

	public double wavePosition = 0;
	public int laserTexAnimation = 0;

	// Size of the wave, from 0 to 1
	public float waveSize = 1F;

	public void update () {
		double dx = head.x - tail.x;
		double dy = head.y - tail.y;
		double dz = head.z - tail.z;

		renderSize = Math.sqrt(dx * dx + dy * dy + dz * dz);
		angleZ = 360 - (Math.atan2(dz, dx) * 180.0 / Math.PI + 180.0);
		dx = Math.sqrt(renderSize * renderSize - dy * dy);
		angleY = -Math.atan2(dy, dx) * 180.0 / Math.PI;
	}

	public void iterateTexture () {
		laserTexAnimation = (laserTexAnimation + 1) % 40;
	}
}
