package buildcraft.robotics;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IDockingStationProvider;
import buildcraft.api.transport.IPipeTile;

public final class RobotUtils {
	private RobotUtils() {

	}

	public static List<DockingStation> getStations(Object tile) {
		ArrayList<DockingStation> stations = new ArrayList<DockingStation>();

		if (tile instanceof IDockingStationProvider) {
			stations.add(((IDockingStationProvider) tile).getStation());
		}

		if (tile instanceof IPipeTile) {
			IPipeTile pipeTile = (IPipeTile) tile;
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
				if (pipeTile.getPipePluggable(d) instanceof IDockingStationProvider) {
					IDockingStationProvider pluggable = (IDockingStationProvider) pipeTile.getPipePluggable(d);
					stations.add(pluggable.getStation());
				}
			}
		}

		return stations;
	}
}
