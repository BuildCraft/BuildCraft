/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.container;

import ct.buildcraft.factory.BCFactoryGuis;
import ct.buildcraft.factory.tile.TileChute;
import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ContainerChute extends ContainerBCTile<TileChute> {
	
	public ContainerChute(int containerId, Inventory playInventory, FriendlyByteBuf buf) {
		this(containerId, playInventory, new ItemHandlerSimple(containerId), CreateClientLevelAccess(buf));
	}
	
    public ContainerChute(int containerId, Inventory playInventory, IItemHandlerAdv inv, ContainerLevelAccess access) {
        super(BCFactoryGuis.MENU_CHUTE.get(), playInventory, containerId, access);
        addFullPlayerInventory(71);

        addSlot(new SlotBase(tile.inv, 0, 62, 18));
        addSlot(new SlotBase(tile.inv, 1, 80, 18));
        addSlot(new SlotBase(tile.inv, 2, 98, 18));
        addSlot(new SlotBase(tile.inv, 3, 80, 36));
    }
}
