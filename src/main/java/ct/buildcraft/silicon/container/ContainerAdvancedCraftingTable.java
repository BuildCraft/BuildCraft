/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.container;

import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.gui.slot.SlotDisplay;
import ct.buildcraft.lib.gui.slot.SlotOutput;
import ct.buildcraft.lib.gui.slot.SlotPhantom;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.silicon.BCSiliconGuis;
import ct.buildcraft.silicon.tile.TileAdvancedCraftingTable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.items.IItemHandler;

public class ContainerAdvancedCraftingTable extends ContainerBCTile<TileAdvancedCraftingTable> {
	
	public ContainerAdvancedCraftingTable(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(containerId, playerInventory, new ItemHandlerSimple(15), new ItemHandlerSimple(9), new ItemHandlerSimple(9), new ItemHandlerSimple(1), CreateClientLevelAccess(buf));
	}
	
    public ContainerAdvancedCraftingTable(int containerId, Inventory playerInventory, IItemHandlerAdv invMaterials, 
    		IItemHandlerAdv invResults, IItemHandlerAdv invBlueprint, IItemHandler clientResult, ContainerLevelAccess access) {
        super(BCSiliconGuis.MENU_AD_CRAFTING_TABLE.get(), playerInventory, containerId, access);

        addSlot(new SlotDisplay(clientResult, 0, 127, 33));

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
            	addSlot(new SlotPhantom(invBlueprint, x + y * 3, 33 + x * 18, 16 + y * 18, false));
            }
        }
        
        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 5; x++) {
            	addSlot(new SlotBase(invMaterials, x + y * 5, 15 + x * 18, 85 + y * 18));
            }
        }

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
            	addSlot(new SlotOutput(invResults, x + y * 3, 109 + x * 18, 85 + y * 18));
            }
        }
        addFullPlayerInventory(153);
    }
}
