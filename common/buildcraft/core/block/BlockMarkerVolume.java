/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.block.BlockMarkerBase;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockMarkerVolume extends BlockMarkerBase {
    public BlockMarkerVolume(String idBC, BlockBehaviour.Properties properties) {
        super(idBC, properties);
    }

    @Override
//    public TileBC_Neptune createTileEntity(Level worldIn, BlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return BCCoreBlocks.markerVolumeTile.get().create(pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_60514_) {
        checkSignalState(world, pos);
    }

    @Override
//    public void updateTick(Level world, BlockPos pos, BlockState state, Random rand)
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        checkSignalState(world, pos);
    }

    public static void checkSignalState(Level world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMarkerVolume volume) {
            boolean powered = world.hasNeighborSignal(pos);

            if (volume.isShowingSignals() != powered) {
                volume.switchSignals();
            }
        }
    }

    @Override
//    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ)
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!world.isClientSide) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileMarkerVolume) {
                TileMarkerVolume volume = (TileMarkerVolume) tile;

                volume.onManualConnectionAttempt(player);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
