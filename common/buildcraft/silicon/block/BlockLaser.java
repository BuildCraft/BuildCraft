/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.block;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.tile.TileLaser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockLaser extends BlockBCTile_Neptune<TileLaser> implements IBlockWithFacing, IBlockWithTickableTE<TileLaser> {
    public BlockLaser(String idBC, BlockBehaviour.Properties props) {
        super(idBC, props);
    }

    @Override
//    public TileBC_Neptune createTileEntity(Level world, BlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return BCSiliconBlocks.laserTile.get().create(pos, state);
    }

    @Override
    public boolean canFaceVertically() {
        return true;
    }

//    @Override
//    public boolean isFullCube(IBlockState state) {
//        return false;
//    }

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

    // UP
    private static final VoxelShape UP_BOTTOM = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    private static final VoxelShape UP_CENTER = Block.box(5.0D, 4.0D, 5.0D, 11.0D, 13.0D, 11.0D);
    private static final VoxelShape UP = Shapes.or(UP_BOTTOM, UP_CENTER);
    // DOWN
    private static final VoxelShape DOWN_BOTTOM = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape DOWN_CENTER = Block.box(5.0D, 3.0D, 5.0D, 11.0D, 12.0D, 11.0D);
    private static final VoxelShape DOWN = Shapes.or(DOWN_BOTTOM, DOWN_CENTER);
    // EAST
    private static final VoxelShape EAST_BOTTOM = Block.box(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_CENTER = Block.box(4.0D, 5.0D, 5.0D, 13.0D, 11.0D, 11.0D);
    private static final VoxelShape EAST = Shapes.or(EAST_BOTTOM, EAST_CENTER);
    // WEST
    private static final VoxelShape WEST_BOTTOM = Block.box(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_CENTER = Block.box(3.0D, 5.0D, 5.0D, 12.0D, 11.0D, 11.0D);
    private static final VoxelShape WEST = Shapes.or(WEST_BOTTOM, WEST_CENTER);
    // NORTH
    private static final VoxelShape NORTH_BOTTOM = Block.box(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape NORTH_CENTER = Block.box(5.0D, 5.0D, 3.0D, 11.0D, 11.0D, 12.0D);
    private static final VoxelShape NORTH = Shapes.or(NORTH_BOTTOM, NORTH_CENTER);
    // SOUTH
    private static final VoxelShape SOUTH_BOTTOM = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
    private static final VoxelShape SOUTH_CENTER = Block.box(5.0D, 5.0D, 4.0D, 11.0D, 11.0D, 13.0D);
    private static final VoxelShape SOUTH = Shapes.or(SOUTH_BOTTOM, SOUTH_CENTER);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(BlockBCBase_Neptune.BLOCK_FACING_6)) {
            case UP -> UP;
            case DOWN -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
        };
    }
}
