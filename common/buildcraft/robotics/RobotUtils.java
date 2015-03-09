package buildcraft.robotics;

import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.IPipeTile;
import buildcraft.robotics.ai.AIRobotSleep;

/**
 * Created by asie on 1/24/15.
 */
public final class RobotUtils {
	private RobotUtils() {

	}

	public static DockingStation getStation(IPipeTile tile, ForgeDirection d) {
		if (tile.getPipePluggable(d) instanceof RobotStationPluggable) {
			RobotStationPluggable pluggable = (RobotStationPluggable) tile.getPipePluggable(d);
			return pluggable.getStation();
		}
		return null;
	}
}
