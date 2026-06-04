/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.container;

import java.io.IOException;

import net.minecraft.entity.player.PlayerEntity;

import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;

import buildcraft.lib.gui.ContainerPipe;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond.FilterMode;

public class ContainerDiamondWoodPipe extends ContainerPipe {

    private final PipeBehaviourWoodDiamond behaviour;
    private final ItemHandlerSimple filterInv;

    public ContainerDiamondWoodPipe(PlayerEntity player, int syncId, PipeBehaviourWoodDiamond behaviour) {
        super(player, syncId, behaviour.pipe.getHolder());
        this.behaviour = behaviour;
        this.filterInv = behaviour.filters;
        behaviour.pipe.getHolder().onPlayerOpen(player);

        addFullPlayerInventory(79);

        for (int i = 0; i < 9; i++) {
            addSlot(new SlotPhantom(filterInv, i, 8 + i * 18, 18));
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        behaviour.pipe.getHolder().onPlayerClose(player);
    }

    public void sendNewFilterMode(FilterMode newFilterMode) {
        this.sendMessage(NET_DATA, (buffer) -> buffer.writeEnumValue(newFilterMode));
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, Object ctx) throws IOException {
        super.readMessage(id, buffer, isClient, ctx);
        if (!isClient) {
            behaviour.filterMode = buffer.readEnumValue(FilterMode.class);
            behaviour.pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }
}
