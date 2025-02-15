/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.factory.tile.TileChute;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class BlockChute extends BlockBCTile_Neptune<TileChute> implements IBlockWithFacing, IBlockWithTickableTE<TileChute> {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public BlockChute(String idBC, BlockBehaviour.Properties props) {
        super(idBC, props);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(BuildCraftProperties.CONNECTED_DOWN, false)
                .setValue(BuildCraftProperties.CONNECTED_UP, false)
                .setValue(BuildCraftProperties.CONNECTED_EAST, false)
                .setValue(BuildCraftProperties.CONNECTED_WEST, false)
                .setValue(BuildCraftProperties.CONNECTED_NORTH, false)
                .setValue(BuildCraftProperties.CONNECTED_SOUTH, false)
        );
    }

    @Override
//    public TileBC_Neptune createTileEntity(World world, IBlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileChute(pos, state);
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ)
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!world.isClientSide) {
//            BCFactoryGuis.CHUTE.openGUI(player, pos);
            if (world.getBlockEntity(pos) instanceof TileChute tile) {
                MessageUtil.serverOpenTileGui(player, tile);
            }
        }
//        return true;
        return InteractionResult.SUCCESS;
    }

//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }

//    @Override
//    public boolean isFullCube(IBlockState state) {
//        return false;
//    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    // Calen: not found a similar method like getActualState of 1.12.2
    @Override
    public BlockState getActualState(BlockState state, LevelAccessor world, BlockPos pos, BlockEntity tile) {
        for (Direction side : Direction.VALUES) {
            state = state.setValue(CONNECTED_MAP.get(side), side != state.getValue(getFacingProperty())
                    && TileChute.hasInventoryAtPosition(world, pos.relative(side), side));
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, world, pos, facingPos);
        return getActualState(state, world, pos, null);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return getActualState(state, world, pos, null);
    }

    // UP
    private static final VoxelShape topBoxU = Block.box(0, 9, 0, 16, 16, 16);
    private static final VoxelShape middleBox0U = Block.box(1, 8, 1, 15, 9, 15);
    private static final VoxelShape middleBox1U = Block.box(2, 7, 2, 14, 8, 14);
    private static final VoxelShape middleBox2U = Block.box(3, 6, 3, 13, 7, 13);
    private static final VoxelShape middleBox3U = Block.box(4, 5, 4, 12, 6, 12);
    private static final VoxelShape middleBox4U = Block.box(5, 4, 5, 11, 5, 11);
    private static final VoxelShape middleBox5U = Block.box(6, 3, 6, 10, 4, 10);
    private static final VoxelShape U = Shapes.or(topBoxU, middleBox0U, middleBox1U, middleBox2U, middleBox3U, middleBox4U, middleBox5U);
    // DOWN
    private static final VoxelShape topBoxD = Block.box(0, 0, 0, 16, 7, 16);
    private static final VoxelShape middleBox0D = Block.box(1, 7, 1, 15, 8, 15);
    private static final VoxelShape middleBox1D = Block.box(2, 8, 2, 14, 9, 14);
    private static final VoxelShape middleBox2D = Block.box(3, 9, 3, 13, 10, 13);
    private static final VoxelShape middleBox3D = Block.box(4, 10, 4, 12, 11, 12);
    private static final VoxelShape middleBox4D = Block.box(5, 11, 5, 11, 12, 11);
    private static final VoxelShape middleBox5D = Block.box(6, 12, 6, 10, 13, 10);
    private static final VoxelShape D = Shapes.or(topBoxD, middleBox0D, middleBox1D, middleBox2D, middleBox3D, middleBox4D, middleBox5D);
    // EAST
    private static final VoxelShape topBoxE = Block.box(9, 0, 0, 16, 16, 16);
    private static final VoxelShape middleBox0E = Block.box(8, 1, 1, 9, 15, 15);
    private static final VoxelShape middleBox1E = Block.box(7, 2, 2, 8, 14, 14);
    private static final VoxelShape middleBox2E = Block.box(6, 3, 3, 7, 13, 13);
    private static final VoxelShape middleBox3E = Block.box(5, 4, 4, 5, 12, 12);
    private static final VoxelShape middleBox4E = Block.box(4, 5, 5, 6, 11, 11);
    private static final VoxelShape middleBox5E = Block.box(3, 6, 6, 4, 10, 10);
    private static final VoxelShape E = Shapes.or(topBoxE, middleBox0E, middleBox1E, middleBox2E, middleBox3E, middleBox4E, middleBox5E);
    // WEST
    private static final VoxelShape topBoxW = Block.box(0, 0, 0, 7, 16, 16);
    private static final VoxelShape middleBox0W = Block.box(7, 1, 1, 8, 15, 15);
    private static final VoxelShape middleBox1W = Block.box(8, 2, 2, 9, 14, 14);
    private static final VoxelShape middleBox2W = Block.box(9, 3, 3, 10, 13, 13);
    private static final VoxelShape middleBox3W = Block.box(10, 4, 4, 11, 12, 12);
    private static final VoxelShape middleBox4W = Block.box(11, 5, 5, 12, 11, 11);
    private static final VoxelShape middleBox5W = Block.box(12, 6, 6, 13, 10, 10);
    private static final VoxelShape W = Shapes.or(topBoxW, middleBox0W, middleBox1W, middleBox2W, middleBox3W, middleBox4W, middleBox5W);
    // NORTH
    private static final VoxelShape topBoxN = Block.box(0, 0, 0, 16, 16, 7);
    private static final VoxelShape middleBox0N = Block.box(1, 1, 7, 15, 15, 8);
    private static final VoxelShape middleBox1N = Block.box(2, 2, 8, 14, 14, 9);
    private static final VoxelShape middleBox2N = Block.box(3, 3, 9, 13, 13, 10);
    private static final VoxelShape middleBox3N = Block.box(4, 4, 10, 12, 12, 11);
    private static final VoxelShape middleBox4N = Block.box(5, 5, 11, 11, 11, 12);
    private static final VoxelShape middleBox5N = Block.box(6, 6, 12, 10, 10, 13);
    private static final VoxelShape N = Shapes.or(topBoxN, middleBox0N, middleBox1N, middleBox2N, middleBox3N, middleBox4N, middleBox5N);
    // SOUTH
    private static final VoxelShape topBoxS = Block.box(0, 0, 9, 16, 16, 16);
    private static final VoxelShape middleBox0S = Block.box(1, 1, 8, 15, 15, 9);
    private static final VoxelShape middleBox1S = Block.box(2, 2, 7, 14, 14, 8);
    private static final VoxelShape middleBox2S = Block.box(3, 3, 6, 13, 13, 7);
    private static final VoxelShape middleBox3S = Block.box(4, 4, 5, 12, 12, 6);
    private static final VoxelShape middleBox4S = Block.box(5, 5, 4, 11, 11, 5);
    private static final VoxelShape middleBox5S = Block.box(6, 6, 3, 10, 10, 4);
    private static final VoxelShape S = Shapes.or(topBoxS, middleBox0S, middleBox1S, middleBox2S, middleBox3S, middleBox4S, middleBox5S);

    // connection
    private static final VoxelShape CONNECT_D = Block.box(5, 0, 5, 11, 3, 11);
    private static final VoxelShape CONNECT_U = Block.box(5, 13, 5, 11, 16, 11);
    private static final VoxelShape CONNECT_W = Block.box(0, 5, 5, 3, 11, 11);
    private static final VoxelShape CONNECT_E = Block.box(13, 5, 5, 16, 11, 11);
    private static final VoxelShape CONNECT_S = Block.box(5, 5, 13, 11, 11, 16);
    private static final VoxelShape CONNECT_N = Block.box(5, 5, 0, 11, 11, 3);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        VoxelShape shape = switch (state.getValue(getFacingProperty())) {
            case UP -> U;
            case DOWN -> D;
            case EAST -> E;
            case WEST -> W;
            case NORTH -> N;
            case SOUTH -> S;
        };
        if (state.getValue(BuildCraftProperties.CONNECTED_SOUTH)) {
            shape = Shapes.or(shape, CONNECT_S);
        }
        if (state.getValue(BuildCraftProperties.CONNECTED_NORTH)) {
            shape = Shapes.or(shape, CONNECT_N);
        }
        if (state.getValue(BuildCraftProperties.CONNECTED_EAST)) {
            shape = Shapes.or(shape, CONNECT_E);
        }
        if (state.getValue(BuildCraftProperties.CONNECTED_WEST)) {
            shape = Shapes.or(shape, CONNECT_W);
        }
        if (state.getValue(BuildCraftProperties.CONNECTED_UP)) {
            shape = Shapes.or(shape, CONNECT_U);
        }
        if (state.getValue(BuildCraftProperties.CONNECTED_DOWN)) {
            shape = Shapes.or(shape, CONNECT_D);
        }
        return shape;
    }


    // IBlockWithFacing

    @Override
    public boolean canFaceVertically() {
        return true;
    }
}
