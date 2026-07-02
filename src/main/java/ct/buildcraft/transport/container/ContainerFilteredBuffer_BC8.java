/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.transport.container;

import com.mojang.datafixers.util.Pair;

import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.gui.slot.SlotPhantom;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.transport.BCTransportGuis;
import ct.buildcraft.transport.BCTransportSprites;
import ct.buildcraft.transport.tile.TileFilteredBuffer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;

public class ContainerFilteredBuffer_BC8 extends ContainerBCTile<TileFilteredBuffer> {
	
	public ContainerFilteredBuffer_BC8(int containerId, Inventory inv, FriendlyByteBuf buf) {
		this(containerId, inv, new ItemHandlerSimple(9), new ItemHandlerSimple(9), CreateClientLevelAccess(buf));
	}
	
    public ContainerFilteredBuffer_BC8(int containerId, Inventory inventory, IItemHandlerAdv invFilter, IItemHandlerAdv invMain, ContainerLevelAccess access) {
        super(BCTransportGuis.MENU_FILTERED_BUFFER.get(), inventory, containerId, access);
        addFullPlayerInventory(86);

        for (int i = 0; i < 9; i++) {
            // Filtered Buffer filter slots
            addSlot(new SlotPhantom(invFilter, i, 8 + i * 18, 27) {
                @Override
				public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
					return Pair.of(InventoryMenu.BLOCK_ATLAS, BCTransportSprites.FILTERED_BUFFER_EMPTY_SLOT_GUI);
				}


				@Override
                public boolean canAdjustCount() {
                    return false;
                }
            });
            // Filtered Buffer inventory slots
            addSlot(new SlotBase(invMain, i, 8 + i * 18, 61));
        }
    }
}
