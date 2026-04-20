/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.container;

import java.io.IOException;

import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import ct.buildcraft.lib.gui.ContainerPipe;
import ct.buildcraft.lib.gui.slot.SlotPhantom;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.transport.BCTransportGuis;
import ct.buildcraft.transport.pipe.Pipe;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond.FilterMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class ContainerDiamondWoodPipe extends ContainerPipe {
    public final PipeBehaviourWoodDiamond behaviour;
    
    public static ContainerDiamondWoodPipe create(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
    	ContainerLevelAccess access = CreateClientLevelAccess(buf);
    	return access.evaluate((level, pos) -> {
    		BlockEntity tile = level.getBlockEntity(pos);
    		if(tile instanceof IPipeHolder pipeHolder && pipeHolder.getPipe() != Pipe.EMPTY) {
    			if(pipeHolder.getPipe().getBehaviour() instanceof PipeBehaviourWoodDiamond woodDiamond)
    					return new ContainerDiamondWoodPipe(containerId, playerInventory, new ItemHandlerSimple(9), woodDiamond);
    		}
    		return null;
    	}, null);
    }

    public ContainerDiamondWoodPipe(int containerId, Inventory inventory, IItemHandlerAdv filterInv, PipeBehaviourWoodDiamond behaviour) {
        super(inventory, BCTransportGuis.MENU_PIPE_DIAMOND_WOOD.get(), containerId, behaviour.pipe.getHolder());
        this.behaviour = behaviour;
        behaviour.pipe.getHolder().onPlayerOpen(inventory.player);

        addFullPlayerInventory(79);

        for (int i = 0; i < 9; i++) {
            addSlot(new SlotPhantom(filterInv, i, 8 + i * 18, 18));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        behaviour.pipe.getHolder().onPlayerClose(player);
    }

    public void sendNewFilterMode(FilterMode newFilterMode) {
        this.sendMessage(NET_DATA, (buffer) -> buffer.writeEnum(newFilterMode));
    }

    @Override
    public void readMessage(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == LogicalSide.SERVER) {
            behaviour.filterMode = buffer.readEnum(FilterMode.class);
            behaviour.pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }
}
