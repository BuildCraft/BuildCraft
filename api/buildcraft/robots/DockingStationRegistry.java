/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots;

import java.util.Collection;
import java.util.HashMap;

import net.minecraftforge.common.util.ForgeDirection;

public final class DockingStationRegistry {

	private static HashMap<StationIndex, DockingStation> stations = new HashMap<StationIndex, DockingStation>();

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

	public static DockingStation getStation(int x, int y, int z, ForgeDirection side) {
		StationIndex index = new StationIndex(x, y, z, side);

		if (stations.containsKey(index)) {
			return stations.get(index);
		} else {
			return null;
		}
	}

	public static Collection<DockingStation> getStations() {
		return stations.values();
	}

	public static void registerStation(DockingStation station) {
		stations.put(toIndex(station), station);
	}

	public static void removeStation(DockingStation station) {
		StationIndex index = toIndex(station);

		if (stations.containsKey(index)) {
			stations.remove(index);
		}
	}

	private static StationIndex toIndex(DockingStation station) {
		return new StationIndex(station.pipe.xCoord, station.pipe.yCoord, station.pipe.zCoord, station.side);
	}
}
