/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.menu;

import java.io.IOException;

import ct.buildcraft.builders.BCBuildersGuis;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.tile.TileElectronicLibrary;
import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.MenuBC_Neptune;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.gui.slot.SlotOutput;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class ContainerElectronicLibrary extends ContainerBCTile<TileElectronicLibrary> {
    private static final IdAllocator IDS = MenuBC_Neptune.IDS.makeChild("electronic_library");
    private static final int ID_SELECTED = IDS.allocId("SELECTED");
    
	public ContainerElectronicLibrary(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(containerId, playerInventory, new ItemHandlerSimple(1), new ItemHandlerSimple(1), 
				new ItemHandlerSimple(1), new ItemHandlerSimple(1), CreateClientLevelAccess(buf));
	}

    public ContainerElectronicLibrary(int containerId, Inventory playerInventory, ItemHandlerSimple invDownOut,
    		ItemHandlerSimple invDownIn, ItemHandlerSimple invUpIn, ItemHandlerSimple invUpOut, ContainerLevelAccess access) {
        super(BCBuildersGuis.MENU_ELIBRARY.get(), playerInventory, containerId, access);
        addFullPlayerInventory(138);

        addSlot(new SlotOutput(invDownOut, 0, 175, 57));
        addSlot(new SlotBase(invDownIn, 0, 219, 57));

        addSlot(new SlotBase(invUpIn, 0, 175, 79));
        addSlot(new SlotOutput(invUpOut, 0, 219, 79));
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public void sendSelectedToServer(Snapshot.Key selected) {
        sendMessage(ID_SELECTED, buffer -> {
            buffer.writeBoolean(selected != null);
            if (selected != null) {
                selected.writeToByteBuf(buffer);
            }
        });
    }

    @Override
    public void readMessage(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == LogicalSide.SERVER) {
            if (id == ID_SELECTED) {
                if (buffer.readBoolean()) {
                    tile.selected = new Snapshot.Key(buffer);
                } else {
                    tile.selected = null;
                }
                tile.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
            }
        }
    }

    
    
}
