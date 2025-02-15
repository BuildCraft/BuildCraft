/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.lib.block.BlockBCBase_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockTube extends BlockBCBase_Neptune {
    // private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 16 / 16D, 12 / 16D);
    private static final VoxelShape BOUNDING_BOX = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public BlockTube(String idBC, Properties props) {
        super(idBC, props);
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

    // Calen: This will cause fake block without update to client. Moved to BCFactoryEventDist
//    @Override
//    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
//        BlockPos currentPos = pos;
//        // noinspection StatementWithEmptyBody
//        while (world.getBlockState(currentPos = currentPos.up()).getBlock() == this) {
//        }
//        if (!(world.getTileEntity(currentPos) instanceof TileMiner)) {
//            return super.removedByPlayer(state, world, pos, player, willHarvest);
//        } else {
//            return false;
//        }
//    }

    @Override
//    public AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos)
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return BOUNDING_BOX;
    }
}
