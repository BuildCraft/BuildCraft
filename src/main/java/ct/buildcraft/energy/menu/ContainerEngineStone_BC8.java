/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.energy.menu;

import ct.buildcraft.energy.BCEnergyGuis;
import ct.buildcraft.energy.tile.TileEngineStone_BC8;
import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ContainerEngineStone_BC8 extends ContainerBCTile<TileEngineStone_BC8> {
	
	public ContainerEngineStone_BC8(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(containerId, playerInventory, new ItemHandlerSimple(1), CreateClientLevelAccess(buf));
	}
	
    public ContainerEngineStone_BC8(int containerId, Inventory playerInventory, IItemHandlerAdv invFuel, ContainerLevelAccess access) {
        super(BCEnergyGuis.MENU_STONE.get(), playerInventory, containerId, access);

        addFullPlayerInventory(84);
        addSlot(new SlotBase(invFuel, 0, 80, 41));
    }
}
