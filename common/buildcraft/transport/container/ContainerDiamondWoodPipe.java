/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.container;

import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.lib.gui.ContainerPipe;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond.FilterMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;

public class ContainerDiamondWoodPipe extends ContainerPipe {
    private final PipeBehaviourWoodDiamond behaviour;
    private final ItemHandlerSimple filterInv;

    public ContainerDiamondWoodPipe(MenuType menuType, int id, Player player, PipeBehaviourWoodDiamond behaviour) {
        super(menuType, id, player, behaviour.pipe.getHolder());
        this.behaviour = behaviour;
        this.filterInv = behaviour.filters;
        // Calen: moved to BCTransportGuis#openPipeGui and BCTransportMenuTypes
//        behaviour.pipe.getHolder().onPlayerOpen(player);

        addFullPlayerInventory(79);

        for (int i = 0; i < 9; i++) {
//            addSlotToContainer(new SlotPhantom(filterInv, i, 8 + i * 18, 18));
            addSlot(new SlotPhantom(filterInv, i, 8 + i * 18, 18));
        }
    }

    @Override
//    public void onContainerClosed(Player player)
    public void removed(Player player) {
//        super.onContainerClosed(player);
        super.removed(player);
        behaviour.pipe.getHolder().onPlayerClose(player);
    }

    public void sendNewFilterMode(FilterMode newFilterMode) {
        this.sendMessage(NET_DATA, (buffer) -> buffer.writeEnum(newFilterMode));
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_SERVER) {
            behaviour.filterMode = buffer.readEnum(FilterMode.class);
            behaviour.pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }
}
