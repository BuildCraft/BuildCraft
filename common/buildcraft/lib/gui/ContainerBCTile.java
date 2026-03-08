/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;

//public abstract class ContainerBCTile<T extends TileBC_Neptune> extends ContainerBC_Neptune<T>
public abstract class ContainerBCTile<T extends TileBC_Neptune & IBCTileMenuProvider> extends ContainerBC_Neptune<T> {
    public final T tile;

    public ContainerBCTile(ContainerType menuType, int id, PlayerEntity player, T tile) {
        super(menuType, id, player);
        this.tile = tile;
        // Calen: Moved to MessageUtil#serverOpenTileGui:MessageUpdateTile msg = tile.onServerPlayerOpenNoSend(player);
        // to ensure the message received before GUI opened in client
        // Not called in Client. We just handle the MessageUpdateTile in Client
//        if (!tile.getLevel().isClientSide) {
//            tile.onPlayerOpen(player);
//        }
    }

    @Override
//    public void onContainerClosed(PlayerEntity player)
    public void removed(PlayerEntity player) {
//        super.onContainerClosed(player);
        super.removed(player);
        tile.onPlayerClose(player);
    }

    @Override
//    public final boolean canInteractWith(PlayerEntity player)
    public final boolean stillValid(PlayerEntity player) {
        return tile.canInteractWith(player);
    }

    @Override
//    public void detectAndSendChanges()
    public void broadcastChanges() {
//        super.detectAndSendChanges();
        super.broadcastChanges();
        if (this.player instanceof ServerPlayerEntity) {
            tile.sendNetworkGuiTick((ServerPlayerEntity) this.player);
        }
    }
}
