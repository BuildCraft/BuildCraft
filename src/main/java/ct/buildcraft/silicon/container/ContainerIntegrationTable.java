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
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.silicon.BCSiliconGuis;
import ct.buildcraft.silicon.tile.TileIntegrationTable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.items.IItemHandler;

public class ContainerIntegrationTable extends ContainerBCTile<TileIntegrationTable> {
	
	public ContainerIntegrationTable(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(containerId, playerInventory, new ItemHandlerSimple(27), new ItemHandlerSimple(27),
				new ItemHandlerSimple(27), new ItemHandlerSimple(27), CreateClientLevelAccess(buf));
	}
	
    public ContainerIntegrationTable(int containerId, Inventory playerInventory, IItemHandlerAdv invTarget, 
    		IItemHandlerAdv invToIntegrate, IItemHandler invOutput, IItemHandlerAdv invResult, ContainerLevelAccess access) {
        super(BCSiliconGuis.MENU_INTEGRATION_TABLE.get(), playerInventory, containerId, access);
        addFullPlayerInventory(109);

        int[] indexes = {0, 1, 2, 3, 0, 4, 5, 6, 7};

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                addSlot(new SlotBase((x == 1 && y == 1) ? invTarget : invToIntegrate, indexes[x + y * 3], 19 + x * 25, 24 + y * 25));
            }
        }

        addSlot(new SlotDisplay(invOutput, 0, 101, 36));

        addSlot(new SlotOutput(invResult, 0, 138, 49));
    }
}
