package buildcraft.api.core;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.EnumFacing;

import buildcraft.api.enums.EnumColor;

public final class BuildCraftProperties {

	public static final PropertyDirection BLOCK_FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyDirection BLOCK_FACING_6 = PropertyDirection.create("facing");

	public static final PropertyEnum BLOCK_COLOR = PropertyEnum.create("color", EnumColor.class, EnumColor.VALUES);

//	public static final PropertyEnum SPRING_TYPE = PropertyEnum.create("type", EnumSpring.class);

//	public static final PropertyEnum ENGINE_TYPE = PropertyEnum.create("type", EnumEngineType.class);

//	public static final PropertyEnum LASER_TABLE_TYPE = PropertyEnum.create("type", EnumLaserTableType.class);

	public static final PropertyInteger PIPE_DATA = PropertyInteger.create("data", 0, 15);

//	public static final PropertyEnum MACHINE_STATE = PropertyEnum.create("state", EnumMachineState.class);

	public static final PropertyBool JOINED_BELOW = PropertyBool.create("joined_below");

	// Unlisted properties
//	public static final PropertyDouble FLUID_HEIGHT_NE = new PropertyDouble("height_ne", 0, 1);
//	public static final PropertyDouble FLUID_HEIGHT_NW = new PropertyDouble("height_nw", 0, 1);
//	public static final PropertyDouble FLUID_HEIGHT_SE = new PropertyDouble("height_se", 0, 1);
//	public static final PropertyDouble FLUID_HEIGHT_SW = new PropertyDouble("height_sw", 0, 1);
//	public static final PropertyDouble FLUID_FLOW_DIRECTION = new PropertyDouble("direction", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//	public static final PropertyUnlistedEnum<EnumFillerPattern> FILLER_PATTERN = new PropertyUnlistedEnum<EnumFillerPattern>("pattern", EnumFillerPattern.class);

	// Block state setting flags -these are used by World.markAndNotifyBlock and World.setBlockState. These flags can be
	// added together to pass the additions
	public static final int UPDATE_NONE = 0;
	/** This updates the neighbouring blocks that the new block is set. It also updates the comparator output of this
	 * block. */
	public static final int UPDATE_NEIGHBOURS = 1;
	/** This will mark the block for an update next tick, as well as send an update to the client (if this is a server
	 * world). */
	public static final int MARK_BLOCK_FOR_UPDATE = 2;
	/** This will mark the block for an update, even if this is a client world. It is useless to use this if
	 * world.isRemote returns false. */
	public static final int UPDATE_EVEN_CLIENT = 4 + MARK_BLOCK_FOR_UPDATE; // 6

	// Pre-added flags- pass these as-is to the World.markAndNotifyBlock and World.setBlockState methods.
	/** This will do what both {@link #UPDATE_NEIGHBOURS} and {@link #MARK_BLOCK_FOR_UPDATE} do. */
	public static final int MARK_THIS_AND_NEIGHBOURS = UPDATE_NEIGHBOURS + MARK_BLOCK_FOR_UPDATE;
	/** This will update everything about this block. */
	public static final int UPDATE_ALL = UPDATE_NEIGHBOURS + MARK_BLOCK_FOR_UPDATE + UPDATE_EVEN_CLIENT;

	/** Deactivate constructor */
	private BuildCraftProperties() {}
}
