/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// TODO(R.Chen): blocked by lib.block.BlockMarkerBase (not yet migrated to Fabric 1.20.1)
package buildcraft.core.block;

import java.util.Random;

import net.minecraft.block.Block;
import buildcraft.lib.compat.MaterialBC;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockMarkerBase;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.core.tile.TileMarkerVolume;

public class BlockMarkerVolume extends BlockMarkerBase {
    public BlockMarkerVolume(AbstractBlock.Settings material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World worldIn, BlockState state) {
        return new TileMarkerVolume();
    }

    // @Override removed (R.Chen): no longer overrides — Phase 10
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        checkSignalState(world, pos);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void updateTick(World world, BlockPos pos, BlockState state, Random rand) {
        checkSignalState(world, pos);
    }

    private static void checkSignalState(World world, BlockPos pos) {
        if (world.isClient) {
            return;
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMarkerVolume) {
            TileMarkerVolume volume = (TileMarkerVolume) tile;

            boolean powered = world.isBlockPowered(pos);

            if (volume.isShowingSignals() != powered) {
                volume.switchSignals();
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
        Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isClient) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileMarkerVolume) {
                TileMarkerVolume volume = (TileMarkerVolume) tile;

                volume.onManualConnectionAttempt(player);
            }
        }
        return true;
    }
}
