package buildcraft.energy.blocks;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDynamoMJ extends BlockBCTile_Neptune<TileDynamoMJ> implements ICustomRotationHandler, IBlockWithTickableTE<TileDynamoMJ> {

    public BlockDynamoMJ(String idBC, Properties properties) {
        super(idBC, properties);
    }

    @Nullable
    @Override
//    public TileBC_Neptune createTileEntity(World worldIn, IBlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return new TileDynamoMJ();
    }

//    @Override
//    @Deprecated
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, IBlockReader world, BlockPos pos) {
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
    private static final VoxelShape UP = VoxelShapes.or(BASE_U, TRUNK_U);
    private static final VoxelShape BASE_D = Block.box(0, 12, 0, 16, 16, 16);
    private static final VoxelShape TRUNK_D = Block.box(4, 0, 4, 12, 12, 12);
    private static final VoxelShape DOWN = VoxelShapes.or(BASE_D, TRUNK_D);
    private static final VoxelShape BASE_E = Block.box(0, 0, 0, 4, 16, 16);
    private static final VoxelShape TRUNK_E = Block.box(4, 4, 4, 16, 12, 12);
    private static final VoxelShape EAST = VoxelShapes.or(BASE_E, TRUNK_E);
    private static final VoxelShape BASE_W = Block.box(12, 0, 0, 16, 16, 16);
    private static final VoxelShape TRUNK_W = Block.box(0, 4, 4, 12, 12, 12);
    private static final VoxelShape WEST = VoxelShapes.or(BASE_W, TRUNK_W);
    private static final VoxelShape BASE_N = Block.box(0, 0, 12, 16, 16, 16);
    private static final VoxelShape TRUNK_N = Block.box(4, 4, 0, 12, 12, 12);
    private static final VoxelShape NORTH = VoxelShapes.or(BASE_N, TRUNK_N);
    private static final VoxelShape BASE_S = Block.box(0, 0, 0, 16, 16, 4);
    private static final VoxelShape TRUNK_S = Block.box(4, 4, 4, 12, 12, 16);
    private static final VoxelShape SOUTH = VoxelShapes.or(BASE_S, TRUNK_S);

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) te;
            switch (engine.getCurrentDirection()) {
                case DOWN:
                    return DOWN;
                case UP:
                    return UP;
                case WEST:
                    return WEST;
                case EAST:
                    return EAST;
                case SOUTH:
                    return SOUTH;
                case NORTH:
                    return NORTH;
            }
        }
        return VoxelShapes.block();
    }

    @Override
    @Deprecated
    // public EnumBlockRenderType getRenderType(IBlockState state)
    public BlockRenderType getRenderShape(BlockState state) {
        // return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    // public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean p_60514_) {
        // super.neighborChanged(state, world, pos, block, fromPos);
        super.neighborChanged(state, world, pos, block, fromPos, p_60514_);
        if (world.isClientSide) return;
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            engine.rotateIfInvalid();
        }
    }
// ICustomRotationHandler

    @Override
    // public EnumActionResult attemptRotation(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched)
    public ActionResultType attemptRotation(World world, BlockPos pos, BlockState state, Direction sideWrenched) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            return engine.attemptRotation();
        }
        // return EnumActionResult.FAIL;
        return ActionResultType.FAIL;
    }
}
