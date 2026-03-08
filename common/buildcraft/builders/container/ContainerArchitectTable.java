/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;

public class ContainerArchitectTable extends ContainerBCTile<TileArchitectTable> {
    private static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("architect_table");
    private static final int ID_NAME = IDS.allocId("NAME");

    public ContainerArchitectTable(ContainerType menuType, int id, PlayerEntity player, TileArchitectTable tile) {
        super(menuType, id, player, tile);
        addFullPlayerInventory(88, 84);

//        addSlotToContainer(new SlotBase(tile.invSnapshotIn, 0, 135, 35));
        addSlot(new SlotBase(tile.invSnapshotIn, 0, 135, 35));
//        addSlotToContainer(new SlotOutput(tile.invSnapshotOut, 0, 194, 35));
        addSlot(new SlotOutput(tile.invSnapshotOut, 0, 194, 35));
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public void sendNameToServer(String name) {
        sendMessage(ID_NAME, buffer -> buffer.writeUtf(name));
    }

    @Override
//    public void readMessage(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readMessage(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_SERVER) {
            if (id == ID_NAME) {
                tile.name = buffer.readString();
                tile.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
            }
        }
    }
}
