/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.menu;

import java.io.IOException;

import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.BCBuildersGuis;
import ct.buildcraft.builders.tile.TileArchitectTable;
import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.MenuBC_Neptune;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.gui.slot.SlotOutput;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class ContainerArchitectTable extends ContainerBCTile<TileArchitectTable> {
    private static final IdAllocator IDS = MenuBC_Neptune.IDS.makeChild("architect_table");
    private static final int ID_NAME = IDS.allocId("NAME");
    public String name = "";
    public final DataSlot setting;
   // public final ContainerData deltaProgress;
    
	public ContainerArchitectTable(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(containerId, playerInventory, new ItemHandlerSimple(1), new ItemHandlerSimple(1), DataSlot.standalone(), CreateClientLevelAccess(buf));
	}

    public ContainerArchitectTable(int containerId, Inventory playerInventory, IItemHandlerAdv in, IItemHandlerAdv out, DataSlot setting, ContainerLevelAccess access) {
		super(BCBuildersGuis.MENU_ARCHITECT_TABLE.get(), playerInventory, containerId, access);
        addFullPlayerInventory(88, 84);

        addSlot(new SlotBase(in, 0, 135, 35));
        addSlot(new SlotOutput(out, 0, 194, 35));
        
        this.setting = setting;
        addDataSlot(setting);
        
//        this.deltaProgress = containerData;
       // addDataSlots(containerData);
        
    }
    
    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public void sendNameToServer(String name) {
    	if(!this.name.equals(name)) {
    		sendMessage(ID_NAME, buffer -> buffer.writeUtf(name));
    		this.name = name;
    	}
    }

    @Override
    public void readMessage(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == LogicalSide.SERVER) {
            if (id == ID_NAME) {
                tile.name = buffer.readUtf();
                tile.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
            }
        }
 /*       if (side == LogicalSide.CLIENT) {
            if (id == ID_NAME) {
                name = buffer.readUtf();
            }
        }*/
    }

	@Override
	public boolean stillValid(Player player) {
		return super.stillValid(access, player, BCBuildersBlocks.ARCHITECT.get());
	}

/*	@Override
	public void clientInit(FriendlyByteBuf data) {
		name = data.readUtf();
	}*/
}
