/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerFiltered;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.container.ContainerFilteredBuffer_BC8;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileFilteredBuffer extends TileBC_Neptune implements IBCTileMenuProvider {
    public final ItemHandlerSimple invFilter;
    public final ItemHandlerFiltered invMain;

    public TileFilteredBuffer(BlockPos pos, BlockState blockState) {
        super(BCTransportBlocks.filteredBufferTile.get(), pos, blockState);

        invFilter = itemManager.addInvHandler("filter", 9, EnumAccess.PHANTOM);
        invFilter.setLimitedInsertor(1);

        invMain = new ItemHandlerFiltered(invFilter, false);
        itemManager.addInvHandler("main", invMain, EnumAccess.BOTH, EnumPipePart.VALUES);
    }

    // MenuProvider

    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerFilteredBuffer_BC8(BCTransportMenuTypes.FILTERED_BUFFER, id, player, this);
    }
}
