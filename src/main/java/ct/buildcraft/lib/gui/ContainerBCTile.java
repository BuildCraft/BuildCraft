/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui;

import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

public abstract class ContainerBCTile<T extends TileBC_Neptune> extends MenuBC_Neptune {
	public final T tile;
    protected final ContainerLevelAccess access;

    @SuppressWarnings({ "unchecked" })
	public ContainerBCTile(MenuType<?> type, Inventory playerInventory, int id, ContainerLevelAccess access) {
        super(playerInventory, type, id);
        this.access = access;
        this.tile = (T)(access.evaluate((level, pos) -> {
        	TileBC_Neptune b = (TileBC_Neptune) level.getBlockEntity(pos);
            if (!level.isClientSide) 
                b.onPlayerOpen(playerInventory.player);
        	return b;
        }, null));

    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if(tile != null)
        	tile.onPlayerClose(player);
    }

    @Override
    public  boolean stillValid(Player player) {
    	return tile != null ? tile.canInteractWith(player) : false;
    }

	@Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if(tile != null)
        	tile.sendNetworkGuiTick(playerInventory.player);
    }
}
