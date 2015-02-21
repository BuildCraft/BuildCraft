package buildcraft.api.enums;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import buildcraft.api.core.BuildCraftProperties;

public enum EnumEngineType implements IStringSerializable {
	WOOD, STONE, IRON, CREATIVE;

	public static EnumEngineType getType(IBlockState state) {
		return (EnumEngineType) state.getValue(BuildCraftProperties.ENGINE_TYPE);
	}

	@Override
	public String getName() {
		return name();
	}
}