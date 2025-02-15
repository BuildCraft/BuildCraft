/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.tile;

import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.container.ContainerAutoCraftItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileAutoWorkbenchItems extends TileAutoWorkbenchBase implements IBCTileMenuProvider {
    public TileAutoWorkbenchItems(BlockPos pos, BlockState blockState) {
        super(BCFactoryBlocks.autoWorkbenchItemsTile.get(), 3, 3, pos, blockState);
    }

    // MenuProvider

    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public ContainerAutoCraftItems createMenu(int id, Inventory inventory, Player player) {
        return new ContainerAutoCraftItems(BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS, id, player, this);
    }
}
