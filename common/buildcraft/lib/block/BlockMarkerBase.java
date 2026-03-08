/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.tile.TileMarker;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockVoxelShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;

public abstract class BlockMarkerBase extends BlockBCTile_Neptune<TileMarker> implements ICustomRotationHandler {
    private static final Map<Direction, VoxelShape> BOUNDING_BOXES = new EnumMap<>(Direction.class);

    private static final double halfWidth = 0.1;
    private static final double h = 0.65;
    // Little variables to make reading a *bit* more sane
    private static final double nw = 0.5 - halfWidth;
    private static final double pw = 0.5 + halfWidth;
    private static final double ih = 1 - h;
    private static final VoxelShape BOUNDING_BOX_DOWN = VoxelShapes.box(nw, ih, nw, pw, 1, pw);
    private static final VoxelShape BOUNDING_BOX_UP = VoxelShapes.box(nw, 0, nw, pw, h, pw);
    private static final VoxelShape BOUNDING_BOX_SOUTH = VoxelShapes.box(nw, nw, 0, pw, pw, h);
    private static final VoxelShape BOUNDING_BOX_NORTH = VoxelShapes.box(nw, nw, ih, pw, pw, 1);
    private static final VoxelShape BOUNDING_BOX_EAST = VoxelShapes.box(0, nw, nw, h, pw, pw);
    private static final VoxelShape BOUNDING_BOX_WEST = VoxelShapes.box(ih, nw, nw, 1, pw, pw);

    static {
        BOUNDING_BOXES.put(Direction.DOWN, BOUNDING_BOX_DOWN);
        BOUNDING_BOXES.put(Direction.UP, BOUNDING_BOX_UP);
        BOUNDING_BOXES.put(Direction.SOUTH, BOUNDING_BOX_SOUTH);
        BOUNDING_BOXES.put(Direction.NORTH, BOUNDING_BOX_NORTH);
        BOUNDING_BOXES.put(Direction.EAST, BOUNDING_BOX_EAST);
        BOUNDING_BOXES.put(Direction.WEST, BOUNDING_BOX_WEST);
    }

    public BlockMarkerBase(String idBC, AbstractBlock.Properties props) {
        super(idBC, props);
//        setHardness(0.25f);

        this.registerDefaultState(this.getStateDefinition().any()
                        .setValue(BuildCraftProperties.BLOCK_FACING_6, Direction.UP)
//                        .setValue(BuildCraftProperties.ACTIVE, false) // Calen: only changes, never used
        );
    }

    @Override
//    protected BlockStateContainer createBlockState()
    protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BuildCraftProperties.BLOCK_FACING_6);
//        builder.add(BuildCraftProperties.ACTIVE); // Calen: only changes, never used
    }

//    @Override
//    public int getMetaFromState(IBlockState state) {
//        return state.getValue(BuildCraftProperties.BLOCK_FACING_6).getIndex();
//    }

//    @Override
//    public IBlockState getStateFromMeta(int meta) {
//        return getDefaultState().withProperty(BuildCraftProperties.BLOCK_FACING_6, EnumFacing.getFront(meta));
//    }

    // Calen: ACTIVE only changes, but never used
//    @Override
//    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
//        TileEntity tile = world.getTileEntity(pos);
//        if (tile instanceof TileMarker) {
//            TileMarker<?> marker = (TileMarker<?>) tile;
//            state = state.withProperty(BuildCraftProperties.ACTIVE, marker.isActiveForRender());
//        }
//        return state;
//    }

    // Calen: 1.18.2 moved to FMLClientSetupEvent: RenderTypeLookup.setRenderLayer(BCCoreBlocks.MARKER_VOLUME.get(), RenderType.cutout());
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public RenderType getBlockLayer() { return RenderType.cutout(); }

//    @Override
//    public boolean isFullCube(BlockState state) {
//        return false;
//    }

//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//         return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, IBlockReader world, BlockPos pos) {
        return 1.0F;
    }

    @Override
//    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
//    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext context) {
        return BOUNDING_BOXES.get(state.getValue(BuildCraftProperties.BLOCK_FACING_6));
    }

    @Override
//    public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, Hand hand)
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction facing = context.getClickedFace();
        BlockState state = defaultBlockState();
        state = state.setValue(BuildCraftProperties.BLOCK_FACING_6, facing);
        return state;
    }

    @Override
//    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side)
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        Direction facing = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
        Direction sideOn = facing.getOpposite();
        BlockPos otherPos = pos.relative(sideOn);
        BlockState otherState = world.getBlockState(otherPos);
//        return world.isSideSolid(pos.offset(side.getOpposite()), side);
        return otherState.isFaceSturdy(world, otherPos, facing, BlockVoxelShape.CENTER);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_60514_) {
        if (state.getBlock() != this) {
            return;
        }
//        Direction sideOn = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
//        if (!canPlaceBlockOnSide(world, pos, sideOn))
        if (!canSurvive(state, world, pos)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public ActionResultType attemptRotation(World world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof BlockMarkerBase) {// Just check to make sure we have the right block...
            Property<Direction> prop = BuildCraftProperties.BLOCK_FACING_6;
            return VanillaRotationHandlers.rotateEnumFacing(world, pos, state, prop, VanillaRotationHandlers.ROTATE_FACING);
        } else {
            return ActionResultType.PASS;
        }
    }
}
