package buildcraft.core;

public enum PowerMode {

	M2(20), M4(40), M8(80), M16(160), M32(320), M64(640), M128(1280);
	public static final PowerMode[] VALUES = values();
	public final int maxPower;

	PowerMode(int max) {
		this.maxPower = max;
	}

	public PowerMode getNext() {
		PowerMode next = VALUES[(ordinal() + 1) % VALUES.length];
		return next;
	}

	public PowerMode getPrevious() {
		PowerMode previous = VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
		return previous;
	}

	public static PowerMode fromId(int id) {
		if (id < 0 || id >= VALUES.length) {
			return M128;
		}
		return VALUES[id];
	}
}