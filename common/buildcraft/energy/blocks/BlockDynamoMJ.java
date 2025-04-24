package buildcraft.energy.blocks;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class BlockDynamoMJ extends BlockBCTile_Neptune<TileDynamoMJ> implements ICustomRotationHandler, IBlockWithTickableTE<TileDynamoMJ> {

    public BlockDynamoMJ(String idBC, Properties properties) {
        super(idBC, properties);
    }

    @Nullable
    @Override
//    public TileBC_Neptune createTileEntity(World worldIn, IBlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileDynamoMJ(pos, state);
    }

//    @Override
//    @Deprecated
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }

//    @Override
//    @Deprecated
//    public boolean isFullBlock(IBlockState state) {
//        return false;
//    }

//    @Override
//    @Deprecated
//    public boolean isFullCube(IBlockState state) {
//        return false;
//    }

//    @Override
//    @Deprecated
//    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
//        TileEntity tile = world.getTileEntity(pos);
//        if (tile instanceof TileDynamoMJ) {
//            TileDynamoMJ engine = (TileDynamoMJ) tile;
//            if (side == engine.getCurrentDirection().getOpposite()) {
//                return BlockFaceShape.SOLID;
//            } else {
//                return BlockFaceShape.UNDEFINED;
//            }
//        }
//        return BlockFaceShape.UNDEFINED;
//    }

//    @Override
//    @Deprecated
//    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
//        TileEntity tile = world.getTileEntity(pos);
//        if (tile instanceof TileDynamoMJ) {
//            TileDynamoMJ engine = (TileDynamoMJ) tile;
//            return side == engine.getCurrentDirection().getOpposite();
//        }
//        return false;
//    }

    // Calen
    private static final VoxelShape BASE_U = Block.box(0, 0, 0, 16, 4, 16);
    private static final VoxelShape TRUNK_U = Block.box(4, 4, 4, 12, 16, 12);
    private static final VoxelShape UP = Shapes.or(BASE_U, TRUNK_U);
    private static final VoxelShape BASE_D = Block.box(0, 12, 0, 16, 16, 16);
    private static final VoxelShape TRUNK_D = Block.box(4, 0, 4, 12, 12, 12);
    private static final VoxelShape DOWN = Shapes.or(BASE_D, TRUNK_D);
    private static final VoxelShape BASE_E = Block.box(0, 0, 0, 4, 16, 16);
    private static final VoxelShape TRUNK_E = Block.box(4, 4, 4, 16, 12, 12);
    private static final VoxelShape EAST = Shapes.or(BASE_E, TRUNK_E);
    private static final VoxelShape BASE_W = Block.box(12, 0, 0, 16, 16, 16);
    private static final VoxelShape TRUNK_W = Block.box(0, 4, 4, 12, 12, 12);
    private static final VoxelShape WEST = Shapes.or(BASE_W, TRUNK_W);
    private static final VoxelShape BASE_N = Block.box(0, 0, 12, 16, 16, 16);
    private static final VoxelShape TRUNK_N = Block.box(4, 4, 0, 12, 12, 12);
    private static final VoxelShape NORTH = Shapes.or(BASE_N, TRUNK_N);
    private static final VoxelShape BASE_S = Block.box(0, 0, 0, 16, 16, 4);
    private static final VoxelShape TRUNK_S = Block.box(4, 4, 4, 12, 12, 16);
    private static final VoxelShape SOUTH = Shapes.or(BASE_S, TRUNK_S);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (world.getBlockEntity(pos) instanceof TileDynamoMJ engine) {
            return switch (engine.getCurrentDirection()) {
                case DOWN -> DOWN;
                case UP -> UP;
                case WEST -> WEST;
                case EAST -> EAST;
                case SOUTH -> SOUTH;
                case NORTH -> NORTH;
            };
        }
        return Shapes.block();
    }

    @Override
    @Deprecated
    // public EnumBlockRenderType getRenderType(IBlockState state)
    public RenderShape getRenderShape(BlockState state) {
        // return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    // public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean p_60514_) {
        // super.neighborChanged(state, world, pos, block, fromPos);
        super.neighborChanged(state, world, pos, block, fromPos, p_60514_);
        if (world.isClientSide) return;
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            engine.rotateIfInvalid();
        }
    }
// ICustomRotationHandler

    @Override
    // public EnumActionResult attemptRotation(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched)
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            return engine.attemptRotation();
        }
        // return EnumActionResult.FAIL;
        return InteractionResult.FAIL;
    }
}
