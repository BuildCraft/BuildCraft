/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockDistiller extends BlockBCTile_Neptune<TileDistiller_BC8> implements IBlockWithFacing, IBlockWithTickableTE<TileDistiller_BC8> {
    public BlockDistiller(String idBC, BlockBehaviour.Properties props) {
        super(idBC, props);
    }

    @Override
//    public TileBC_Neptune createTileEntity(Level worldIn, BlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return BCFactoryBlocks.distillerTile.get().create(pos, state);
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
//    public InteractionResult onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ)
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction facing = hitResult.getDirection();
        float hitX = hitResult.getBlockPos().getX();
        float hitY = hitResult.getBlockPos().getY();
        float hitZ = hitResult.getBlockPos().getZ();
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileDistiller_BC8) {
            return ((TileDistiller_BC8) tile).onActivated(player, hand, facing, hitX, hitY, hitZ);
        }
        return InteractionResult.PASS;
    }

    // 1.18.2: moved to BCFactory#registerRecipeSerializers
//    @Override
//    @SideOnly(Side.CLIENT)
//    public BlockRenderLayer getBlockLayer() {
//        return BlockRenderLayer.CUTOUT;
//    }
}
