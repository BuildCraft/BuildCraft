/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.transport.BCTransportGuis;
import buildcraft.transport.tile.TileFilteredBuffer;

// STUB(R.Chen): BlockFilteredBuffer — was BlockBCTile_Neptune (Forge); migrated as
// BlockBCBase_Neptune + BlockEntityProvider. TileFilteredBuffer.TYPE assigned at
// BCTransportInitializer registration time (Phase 4F).
public class BlockFilteredBuffer extends BlockBCBase_Neptune implements BlockEntityProvider {

    public BlockFilteredBuffer(AbstractBlock.Settings settings, String id) {
        super(settings, id);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        // STUB(R.Chen): requires TileFilteredBuffer.TYPE (registered in Phase 4F).
        return null;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
        BlockHitResult hit) {
        if (!world.isClient) {
            BCTransportGuis.FILTERED_BUFFER.openGui(player, pos);
        }
        return ActionResult.SUCCESS;
    }
}
