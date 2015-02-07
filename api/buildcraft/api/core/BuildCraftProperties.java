package buildcraft.api.core;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.EnumFacing;
import buildcraft.core.BlockSpring.EnumSpring;
import buildcraft.energy.BlockEngine.EngineType;
import buildcraft.silicon.BlockLaserTable.LaserTableType;

public final class BuildCraftProperties {

	public static final PropertyDirection BLOCK_FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyDirection BLOCK_FACING_6 = PropertyDirection.create("facing");

	public static final PropertyEnum BLOCK_COLOR = PropertyEnum.create("color", EnumColor.class, EnumColor.VALUES);

	public static final PropertyEnum SPRING_TYPE = PropertyEnum.create("type", EnumSpring.class);

	public static final PropertyEnum ENGINE_TYPE = PropertyEnum.create("type", EngineType.class);
	
	public static final PropertyEnum LASER_TABLE_TYPE = PropertyEnum.create("type", LaserTableType.class);
	
	public static final PropertyInteger PIPE_DATA = PropertyInteger.create("data", 0, 15);
}
