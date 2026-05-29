/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.gui;

import net.minecraft.entity.player.PlayerEntity;

import buildcraft.lib.tile.TileBC_Neptune;

public abstract class ContainerBCTile<T extends TileBC_Neptune> extends ContainerBC_Neptune {
    public final T tile;

    public ContainerBCTile(PlayerEntity player, int syncId, T tile) {
        super(player, syncId);
        this.tile = tile;
        if (!tile.getWorld().isClient) {
            tile.onPlayerOpen(player);
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        tile.onPlayerClose(player);
    }

    @Override
    public final boolean canUse(PlayerEntity player) {
        return tile.canInteractWith(player);
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
        tile.sendNetworkGuiTick(this.player);
    }
}
