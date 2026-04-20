/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.factory.menu;

import ct.buildcraft.factory.BCFactoryGuis;
import ct.buildcraft.factory.tile.TileAutoWorkbenchItems;
import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.gui.slot.SlotDisplay;
import ct.buildcraft.lib.gui.slot.SlotOutput;
import ct.buildcraft.lib.gui.slot.SlotPhantom;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.items.IItemHandler;

public class ContainerAutoCraftItems extends ContainerBCTile<TileAutoWorkbenchItems> {

    public final SlotBase[] materialSlots;
    public final SlotBase[] filtterSlots;;
    
	public static ContainerAutoCraftItems create(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		return new ContainerAutoCraftItems(containerId, playerInventory, buf.readInt(), CreateClientLevelAccess(buf));
	}
	
	ContainerAutoCraftItems(int containerId, Inventory playerInventory, int size, ContainerLevelAccess access){
		this(containerId, playerInventory, new ItemHandlerSimple(1), new ItemHandlerSimple(size), 
				new ItemHandlerSimple(size),new ItemHandlerSimple(size), new ItemHandlerSimple(1), access);
	}

    public ContainerAutoCraftItems(int containerId, Inventory player, IItemHandlerAdv invResult, IItemHandlerAdv invBlueprint, 
    		IItemHandlerAdv invMaterialFilter, IItemHandlerAdv invMaterials, IItemHandler resultClient, ContainerLevelAccess access) {
        super(BCFactoryGuis.MENU_AUTOWORK_BENCH_ITEM.get(), player, containerId, access);

        addSlot(new SlotOutput(invResult, 0, 124, 35));
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
            	addSlot(new SlotPhantom(invBlueprint, x + y * 3, 30 + x * 18, 17 + y * 18, false));
            }
        }
        materialSlots = new SlotBase[9];
        filtterSlots = new SlotBase[9];
        for (int x = 0; x < 9; x++) {
            // hide the filter slots, but still sync them
        	addSlot(filtterSlots[x] = new SlotPhantom(invMaterialFilter, x, -1000000, -1000000));
        	addSlot(materialSlots[x] = new SlotBase(invMaterials, x, 8 + x * 18, 84));
        }
        addSlot(new SlotDisplay(resultClient, 0, 93, 27));

        addFullPlayerInventory(115);
    }
}
