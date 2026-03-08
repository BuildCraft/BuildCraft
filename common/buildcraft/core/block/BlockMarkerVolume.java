/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.block.BlockMarkerBase;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class BlockMarkerVolume extends BlockMarkerBase {
    public BlockMarkerVolume(String idBC, AbstractBlock.Properties properties) {
        super(idBC, properties);
    }

    @Override
//    public TileBC_Neptune createTileEntity(World worldIn, BlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return BCCoreBlocks.markerVolumeTile.get().create();
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_60514_) {
        checkSignalState(world, pos);
    }

    @Override
//    public void updateTick(World world, BlockPos pos, BlockState state, Random rand)
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        checkSignalState(world, pos);
    }

    public static void checkSignalState(World world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMarkerVolume) {
            TileMarkerVolume volume = (TileMarkerVolume) tile;
            boolean powered = world.hasNeighborSignal(pos);

            if (volume.isShowingSignals() != powered) {
                volume.switchSignals();
            }
        }
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        if (!world.isClientSide) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileMarkerVolume) {
                TileMarkerVolume volume = (TileMarkerVolume) tile;

                volume.onManualConnectionAttempt(player);
            }
        }
        return ActionResultType.SUCCESS;
    }
}
