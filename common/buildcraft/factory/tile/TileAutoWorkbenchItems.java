/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.tile;

import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.container.ContainerAutoCraftItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class TileAutoWorkbenchItems extends TileAutoWorkbenchBase implements IBCTileMenuProvider {
    public TileAutoWorkbenchItems() {
        super(BCFactoryBlocks.autoWorkbenchItemsTile.get(), 3, 3);
    }

    // INamedContainerProvider

    @Override
    public ITextComponent getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public ContainerAutoCraftItems createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new ContainerAutoCraftItems(BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS, id, player, this);
    }
}
