/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.tile.TileBC_Neptune;

public abstract class ContainerBCTile<T extends TileBC_Neptune> extends ContainerBC_Neptune {
    public final T tile;

    public ContainerBCTile(EntityPlayer player, T tile) {
        super(player);
        this.tile = tile;
        if (!tile.getWorld().isRemote) {
            tile.onPlayerOpen(player);
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        tile.onPlayerClose(player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.canInteractWith(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        tile.sendNetworkGuiTick(this.player);
    }
}
