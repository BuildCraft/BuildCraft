package buildcraft.core;

import buildcraft.BuildCraftCore;
public enum PowerMode {

	M2(BuildCraftCore.mainConfigManager.get("power.cobblestoneKinesisPipe").getInt()),
	M4(BuildCraftCore.mainConfigManager.get("power.stoneKinesisPipe").getInt()),
	M8(BuildCraftCore.mainConfigManager.get("power.sandstoneKinesisPipe").getInt()),
	M16(BuildCraftCore.mainConfigManager.get("power.quartzKinesisPipe").getInt()),
	M32(BuildCraftCore.mainConfigManager.get("power.ironKinesisPipe").getInt()),
	M64(BuildCraftCore.mainConfigManager.get("power.goldKinesisPipe").getInt()),
	M128(BuildCraftCore.mainConfigManager.get("power.diamondKinesisPipe").getInt());
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