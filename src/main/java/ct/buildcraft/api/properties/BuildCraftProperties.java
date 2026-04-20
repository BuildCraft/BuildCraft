package ct.buildcraft.api.properties;


import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import ct.buildcraft.api.enums.EnumDecoratedBlock;
import ct.buildcraft.api.enums.EnumEngineType;
import ct.buildcraft.api.enums.EnumLaserTableType;
import ct.buildcraft.api.enums.EnumMachineState;
import ct.buildcraft.api.enums.EnumOptionalSnapshotType;
import ct.buildcraft.api.enums.EnumPowerStage;
import ct.buildcraft.api.enums.EnumSpring;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class BuildCraftProperties {
    public static final DirectionProperty BLOCK_FACING_6 = BlockStateProperties.FACING;
    public static final DirectionProperty BLOCK_FACING = BlockStateProperties.HORIZONTAL_FACING;

/*    public static final EnumPropetry<DyeColor> BLOC K_COLOR = PropertyEnum.create("color", DyeColor.class);*/
    public static final EnumProperty<EnumSpring> SPRING_TYPE = EnumProperty.create("type", EnumSpring.class);
    public static final EnumProperty<EnumEngineType> ENGINE_TYPE = EnumProperty.create("type", EnumEngineType.class);
    public static final EnumProperty<EnumLaserTableType> LASER_TABLE_TYPE = EnumProperty.create("type", EnumLaserTableType.class);
    public static final EnumProperty<EnumMachineState> MACHINE_STATE = EnumProperty.create("state", EnumMachineState.class);

	public static final EnumProperty<EnumPowerStage> ENERGY_STAGE = EnumProperty.create("stage", EnumPowerStage.class);

    public static final EnumProperty<EnumOptionalSnapshotType> SNAPSHOT_TYPE = EnumProperty.create("snapshot_type", EnumOptionalSnapshotType.class);
    public static final EnumProperty<EnumDecoratedBlock> DECORATED_BLOCK = EnumProperty.create("decoration_type", EnumDecoratedBlock.class);

    public static final IntegerProperty GENERIC_PIPE_DATA = IntegerProperty.create("pipe_data", 0, 15);
    public static final IntegerProperty LED_POWER = IntegerProperty.create("led_power", 0, 3);

    public static final BooleanProperty JOINED_BELOW = BooleanProperty.create("joined_below");
    public static final BooleanProperty WORK_STATE = BooleanProperty.create("work_state");
    public static final BooleanProperty IS_BOTTOM = BooleanProperty.create("is_bottom");
	public static final BooleanProperty IS_PUMP = BooleanProperty.create("is_pump");
    public static final BooleanProperty MOVING = BooleanProperty.create("moving");
    public static final BooleanProperty LED_DONE = BooleanProperty.create("led_done");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty VALID = BooleanProperty.create("valid");

    public static final BooleanProperty CONNECTED_UP = BooleanProperty.create("connected_up");
    public static final BooleanProperty CONNECTED_DOWN = BooleanProperty.create("connected_down");
    public static final BooleanProperty CONNECTED_EAST = BooleanProperty.create("connected_east");
    public static final BooleanProperty CONNECTED_WEST = BooleanProperty.create("connected_west");
    public static final BooleanProperty CONNECTED_NORTH = BooleanProperty.create("connected_north");
    public static final BooleanProperty CONNECTED_SOUTH = BooleanProperty.create("connected_south");

    public static final ImmutableMap<Direction, BooleanProperty> CONNECTED_MAP ;

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
     * level.isClientSide returns false. */
    public static final int UPDATE_EVEN_CLIENT = 4 + MARK_BLOCK_FOR_UPDATE; // 6

    // Pre-added flags- pass these as-is to the World.markAndNotifyBlock and World.setBlockState methods.
    /** This will do what both {@link #UPDATE_NEIGHBOURS} and {@link #MARK_BLOCK_FOR_UPDATE} do. */
    public static final int MARK_THIS_AND_NEIGHBOURS = UPDATE_NEIGHBOURS + MARK_BLOCK_FOR_UPDATE;
    /** This will update everything about this block. */
    public static final int UPDATE_ALL = UPDATE_NEIGHBOURS + MARK_BLOCK_FOR_UPDATE + UPDATE_EVEN_CLIENT;


    static {
        Map<Direction, BooleanProperty> map = Maps.newEnumMap(Direction.class);
        map.put(Direction.DOWN, CONNECTED_DOWN);
        map.put(Direction.UP, CONNECTED_UP);
        map.put(Direction.EAST, CONNECTED_EAST);
        map.put(Direction.WEST, CONNECTED_WEST);
        map.put(Direction.NORTH, CONNECTED_NORTH);
        map.put(Direction.SOUTH, CONNECTED_SOUTH);
        CONNECTED_MAP = Maps.immutableEnumMap(map);
    }
    /*
    /** Deactivate constructor */
    private BuildCraftProperties() {}
    
}
