package buildcraft.robotics.map;

public final class MapUtils {
	private MapUtils() {

	}

	public static long getIDFromCoords(int x, int z) {
		return ((x & 0xFFFFFF) << 24) | (z & 0xFFFFFF);
	}

	public static int getXFromID(long id) {
		return (int) (id >> 24);
	}

	public static int getZFromID(long id) {
		int z = (int) (id & 0xFFFFFF);
		if (z >= 0x800000) {
			return -(z ^ 0xFFFFFF);
		} else {
			return z;
		}
	}

}
