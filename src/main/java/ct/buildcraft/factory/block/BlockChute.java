/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.block;

import java.util.Map;

import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.factory.tile.TileChute;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.block.IBlockWithFacing;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockChute extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final Map<Direction, BooleanProperty> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public BlockChute() {
        super(Properties.of(Material.METAL)
            .sound(SoundType.METAL)
            .strength(5.0f)
            .explosionResistance(10.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion());
        this.registerDefaultState(withConnections(
            this.stateDefinition.any().setValue(getFacingProperty(), Direction.DOWN),
            null,
            null
        ));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTED_MAP.values().toArray(new BooleanProperty[0]));
    }

    @Override
    public boolean canFaceVertically() {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            state = defaultBlockState();
        }
        return withConnections(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
        LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {
        return withConnection(state, world, pos, direction);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
        boolean isMoving) {
        BlockState connected = withConnections(state, level, pos);
        if (connected != state) {
            level.setBlock(pos, connected, Block.UPDATE_ALL);
            state = connected;
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    private static BlockState withConnections(BlockState state, BlockGetter world, BlockPos pos) {
        for (Direction side : Direction.values()) {
            state = withConnection(state, world, pos, side);
        }
        return state;
    }

    private static BlockState withConnection(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        BooleanProperty property = CONNECTED_MAP.get(side);
        if (property == null) {
            return state;
        }
        boolean connected = false;
        if (world != null && pos != null) {
            Direction facing = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
            connected = side != facing && TileChute.hasInventoryAtPosition(world, pos.relative(side), side);
        }
        return state.setValue(property, connected);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileChute(pos, state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof TileChute chute) {
                chute.onRemove(true);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
