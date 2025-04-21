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
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockDistiller extends BlockBCTile_Neptune<TileDistiller_BC8> implements IBlockWithFacing, IBlockWithTickableTE<TileDistiller_BC8> {
    public BlockDistiller(String idBC, AbstractBlock.Properties props) {
        super(idBC, props);
    }

    @Override
//    public TileBC_Neptune createTileEntity(World worldIn, BlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return BCFactoryBlocks.distillerTile.get().create();
    }

//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_49928_, IBlockReader p_49929_, BlockPos p_49930_) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, IBlockReader world, BlockPos pos) {
        return 1.0F;
    }

//    @Override
//    public boolean isFullCube(IBlockState state) {
//        return false;
//    }

    @Override
//    public ActionResultType onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        Direction facing = hitResult.getDirection();
        float hitX = hitResult.getBlockPos().getX();
        float hitY = hitResult.getBlockPos().getY();
        float hitZ = hitResult.getBlockPos().getZ();
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileDistiller_BC8) {
            return ((TileDistiller_BC8) tile).onActivated(player, hand, facing, hitX, hitY, hitZ);
        }
        return ActionResultType.PASS;
    }

    // 1.18.2: moved to BCFactory#registerRecipeSerializers
//    @Override
//    @SideOnly(Side.CLIENT)
//    public BlockRenderLayer getBlockLayer() {
//        return BlockRenderLayer.CUTOUT;
//    }
}
