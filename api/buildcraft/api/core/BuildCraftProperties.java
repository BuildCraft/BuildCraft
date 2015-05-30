package buildcraft.api.core;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.EnumFacing;
import buildcraft.api.enums.EnumColor;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.api.enums.EnumMachineState;
import buildcraft.api.enums.EnumSpring;

public final class BuildCraftProperties {

	public static final PropertyDirection BLOCK_FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyDirection BLOCK_FACING_6 = PropertyDirection.create("facing");

	public static final PropertyEnum BLOCK_COLOR = PropertyEnum.create("color", EnumColor.class, EnumColor.VALUES);

	public static final PropertyEnum SPRING_TYPE = PropertyEnum.create("type", EnumSpring.class);

	public static final PropertyEnum ENGINE_TYPE = PropertyEnum.create("type", EnumEngineType.class);

	public static final PropertyEnum LASER_TABLE_TYPE = PropertyEnum.create("type", EnumLaserTableType.class);

	public static final PropertyInteger PIPE_DATA = PropertyInteger.create("data", 0, 15);

	public static final PropertyEnum MACHINE_STATE = PropertyEnum.create("state", EnumMachineState.class);

	// Unlisted properties
	public static final PropertyDouble FLUID_HEIGHT_NE = new PropertyDouble("height_ne", 0, 1);
	public static final PropertyDouble FLUID_HEIGHT_NW = new PropertyDouble("height_nw", 0, 1);
	public static final PropertyDouble FLUID_HEIGHT_SE = new PropertyDouble("height_se", 0, 1);
	public static final PropertyDouble FLUID_HEIGHT_SW = new PropertyDouble("height_sw", 0, 1);
	public static final PropertyDouble FLUID_FLOW_DIRECTION = new PropertyDouble("direction", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

	/** Deactivate constructor */
	private BuildCraftProperties() {}
}
