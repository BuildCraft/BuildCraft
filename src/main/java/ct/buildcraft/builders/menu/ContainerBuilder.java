/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.menu;

import java.util.List;
import java.util.stream.Collectors;

import ct.buildcraft.builders.BCBuildersGuis;
import ct.buildcraft.builders.tile.TileBuilder;
import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.gui.slot.SlotDisplay;
import ct.buildcraft.lib.gui.widget.WidgetFluidTank;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.items.IItemHandler;

public class ContainerBuilder extends ContainerBCTile<TileBuilder> {
    public final List<WidgetFluidTank> widgetTanks;
   
    
	public ContainerBuilder(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(containerId, playerInventory, new ItemHandlerSimple(1), new ItemHandlerSimple(27),
				new ItemHandlerSimple(24), DataSlot.standalone(), CreateClientLevelAccess(buf));
	}


    public ContainerBuilder(int containerId, Inventory playerInventory, IItemHandlerAdv invSnapshot, IItemHandlerAdv invResources, 
    		IItemHandler invRequire, DataSlot setting, ContainerLevelAccess access) {
    	super(BCBuildersGuis.MENU_BUILDER.get(), playerInventory, containerId, access);

        addFullPlayerInventory(140);

        addSlot(new SlotBase(invSnapshot, 0, 80, 27));

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
            	addSlot(new SlotBase(invResources, sx + sy * 9, 8 + sx * 18, 72 + sy * 18));
            }
        }
        widgetTanks = tile.getTankManager().stream()
                .map(tank -> new WidgetFluidTank(this, tank))
                .map(this::addWidget)
                .collect(Collectors.toList());
        
		addDataSlot(setting);

		setting.set((setting.get()&0b110) | (playerInventory.player.isCreative() ? 0b0 : 0b1));
        for(int y = 0; y < 6; y++) {
            for(int x = 0; x < 4; x++) {
            	addSlot(new SlotDisplay(invRequire, x + y * 4, 179 + x * 18, 18 + y * 18));
            }
        }
    }
}
