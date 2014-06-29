/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;

import net.minecraftforge.common.util.ForgeDirection;

public final class DockingStationRegistry {

	private static HashMap<StationIndex, IDockingStation> stations = new HashMap<StationIndex, IDockingStation>();

	private DockingStationRegistry() {

	}

	private static class StationIndex {
		public int x, y, z;
		public ForgeDirection side;

		public StationIndex(int ix, int iy, int iz, ForgeDirection iSide) {
			// TODO: should probably consider dimension id here too
			x = ix;
			y = iy;
			z = iz;
			side = iSide;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof StationIndex) {
				StationIndex d = (StationIndex) obj;

				return d.x == x && d.y == y && d.z == z && d.side == side;
			}

			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return ((x * 37 + y) * 37 + z * 37) + side.ordinal();
		}

		@Override
		public String toString() {
			return "{" + x + ", " + y + ", " + z + ", " + side + "}";
		}
	}

	public static IDockingStation getStation(int x, int y, int z, ForgeDirection side) {
		StationIndex index = new StationIndex(x, y, z, side);

		if (stations.containsKey(index)) {
			return stations.get(index);
		} else {
			return null;
		}
	}

	public static Collection<IDockingStation> getStations() {
		return stations.values();
	}

	public static void registerStation(IDockingStation station) {
		StationIndex index = toIndex(station);

		if (stations.containsKey(index)) {
			throw new InvalidParameterException("Station " + index + " already registerd");
		} else {
			stations.put(index, station);
		}
	}

	public static void removeStation(IDockingStation station) {
		StationIndex index = toIndex(station);

		if (stations.containsKey(index)) {
			if (station.linked() != null) {
				station.linked().setDead();
			}

			if (station.reserved() != null) {
				station.reserved().reserveStation(null);
			}

			stations.remove(index);
		}
	}

	private static StationIndex toIndex(IDockingStation station) {
		return new StationIndex(station.x(), station.y(), station.z(), station.side());
	}

	public static void clear() {
		stations.clear();
	}
}
