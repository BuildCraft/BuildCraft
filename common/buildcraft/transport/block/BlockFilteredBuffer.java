/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.block;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.tile.TileFilteredBuffer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;


public class BlockFilteredBuffer extends BlockBCTile_Neptune<TileFilteredBuffer> {
    public BlockFilteredBuffer(String idBC, AbstractBlock.Properties props) {
        super(idBC, props);
    }

    @Override
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return BCTransportBlocks.filteredBufferTile.get().create();
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        if (!world.isClientSide) {
//            BCTransportGuis.FILTERED_BUFFER.openGui(player, pos);
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof TileFilteredBuffer) {
                TileFilteredBuffer tile = (TileFilteredBuffer) te;
                MessageUtil.serverOpenTileGui(player, tile);
            }
        }
        return ActionResultType.SUCCESS;
    }
}
