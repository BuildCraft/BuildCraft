package buildcraft.robots;

import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.IPipeTile;

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
