package buildcraft.api.enums;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import buildcraft.api.core.BuildCraftProperties;

public enum EnumLaserTableType implements IStringSerializable {
	ASSEMBLY_TABLE,
	ADVANCED_CRAFTING_TABLE,
	INTEGRATION_TABLE,
	CHARGING_TABLE;

	public static EnumLaserTableType getType(IBlockState state) {
		return (EnumLaserTableType) state.getValue(BuildCraftProperties.LASER_TABLE_TYPE);
	}

	@Override
	public String getName() {
		return name();
	}
}