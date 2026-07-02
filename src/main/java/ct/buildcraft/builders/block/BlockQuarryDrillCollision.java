package ct.buildcraft.builders.block;

import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.tile.TileQuarryDrillCollision;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockQuarryDrillCollision extends Block implements EntityBlock {
    public static final int MASK_X_BEAM = 1;
    public static final int MASK_Z_BEAM = 2;
    public static final int MASK_DRILL = 4;
    public static final IntegerProperty MASK = IntegerProperty.create("mask", 1, 7);

    private static final VoxelShape SHAPE_X_BEAM = Block.box(0, 4, 4, 16, 12, 12);
    private static final VoxelShape SHAPE_Z_BEAM = Block.box(4, 4, 0, 12, 12, 16);
    private static final VoxelShape SHAPE_DRILL = Block.box(4, 0, 4, 12, 16, 12);
    private static final VoxelShape[] SHAPES = new VoxelShape[8];

    static {
        SHAPES[0] = Shapes.empty();
        for (int mask = 1; mask < SHAPES.length; mask++) {
            VoxelShape shape = Shapes.empty();
            if ((mask & MASK_X_BEAM) != 0) {
                shape = Shapes.or(shape, SHAPE_X_BEAM);
            }
            if ((mask & MASK_Z_BEAM) != 0) {
                shape = Shapes.or(shape, SHAPE_Z_BEAM);
            }
            if ((mask & MASK_DRILL) != 0) {
                shape = Shapes.or(shape, SHAPE_DRILL);
            }
            SHAPES[mask] = shape;
        }
    }

    public BlockQuarryDrillCollision() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(-1.0F, 3600000.0F).noLootTable().noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(MASK, MASK_DRILL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MASK);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(MASK)];
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileQuarryDrillCollision(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != BCBuildersBlocks.QUARRY_DRILL_COLLISION_TILE_BC8.get()) {
            return null;
        }
        return (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof TileQuarryDrillCollision tile) {
                tile.update();
            }
        };
    }
}
