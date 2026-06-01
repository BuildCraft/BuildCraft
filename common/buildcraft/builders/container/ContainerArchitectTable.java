/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import java.io.IOException;

import net.minecraft.entity.player.PlayerEntity;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.fabricmc.api.EnvType;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

public class ContainerArchitectTable extends ContainerBCTile<TileArchitectTable> {
    private static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("architect_table");
    private static final int ID_NAME = IDS.allocId("NAME");

    public ContainerArchitectTable(PlayerEntity player, TileArchitectTable tile) {
        super(player, tile);
        addFullPlayerInventory(88, 84);

        addSlot(new SlotBase(tile.invSnapshotIn, 0, 135, 35));
        addSlot(new SlotOutput(tile.invSnapshotOut, 0, 194, 35));
    }
    
    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public void sendNameToServer(String name) {
        sendMessage(ID_NAME, buffer -> buffer.writeString(name));
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void readMessage(int id, PacketBufferBC buffer, NetSide side, MessageContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == NetSide.SERVER) {
            if (id == ID_NAME) {
                tile.name = buffer.readString();
                tile.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
            }
        }
    }
}
