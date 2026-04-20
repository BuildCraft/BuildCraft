/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.container;

import java.io.IOException;
import java.util.EnumMap;

import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import ct.buildcraft.lib.gui.ContainerPipe;
import ct.buildcraft.lib.gui.Widget_Neptune;
import ct.buildcraft.lib.gui.slot.SlotPhantom;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.transport.BCTransportGuis;
import ct.buildcraft.transport.pipe.Pipe;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class ContainerEmzuliPipe_BC8 extends ContainerPipe {
    public final PipeBehaviourEmzuli behaviour;
    public final EnumMap<SlotIndex, PaintWidget> paintWidgets = new EnumMap<>(SlotIndex.class);

    public static ContainerEmzuliPipe_BC8 create(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
    	ContainerLevelAccess access = CreateClientLevelAccess(buf);
    	return access.evaluate((level, pos) -> {
    		BlockEntity tile = level.getBlockEntity(pos);
    		if(tile instanceof IPipeHolder pipeHolder && pipeHolder.getPipe() != Pipe.EMPTY) {
    			if(pipeHolder.getPipe().getBehaviour() instanceof PipeBehaviourEmzuli emzuli)
    			return new ContainerEmzuliPipe_BC8(containerId, playerInventory, new ItemHandlerSimple(4), emzuli);
    		}
    		return null;
    	}, null);
    }
    
    public ContainerEmzuliPipe_BC8(int containerId, Inventory inventory, IItemHandlerAdv filterInv, PipeBehaviourEmzuli behaviour) {
        super(inventory, BCTransportGuis.MENU_PIPE_EMZULI.get(), containerId, behaviour.pipe.getHolder());
        this.behaviour = behaviour;
        behaviour.pipe.getHolder().onPlayerOpen(inventory.player);

        addFullPlayerInventory(84);

        addSlot(new SlotPhantom(filterInv, 0, 25, 21));
        addSlot(new SlotPhantom(filterInv, 1, 25, 49));
        addSlot(new SlotPhantom(filterInv, 2, 134, 21));
        addSlot(new SlotPhantom(filterInv, 3, 134, 49));

        for (SlotIndex index : SlotIndex.VALUES) {
            createPaintWidget(index);
        }
    }

    private void createPaintWidget(SlotIndex index) {
        PaintWidget widget = new PaintWidget(this, index);
        addWidget(widget);
        paintWidgets.put(index, widget);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        behaviour.pipe.getHolder().onPlayerClose(player);
    }

    public static class PaintWidget extends Widget_Neptune<ContainerEmzuliPipe_BC8> {
        public final SlotIndex index;

        public PaintWidget(ContainerEmzuliPipe_BC8 container, SlotIndex index) {
            super(container);
            this.index = index;
        }

        public void setColour(DyeColor colour) {
            sendWidgetData((buffer) -> MessageUtil.writeEnumOrNull(buffer, colour));
        }

        @Override
        public void handleWidgetDataServer(NetworkEvent.Context ctx, FriendlyByteBuf buffer) throws IOException {
            DyeColor colour = MessageUtil.readEnumOrNull(buffer, DyeColor.class);
            if (colour == null) {
                container.behaviour.slotColours.remove(index);
            } else {
                container.behaviour.slotColours.put(index, colour);
            }
            container.behaviour.pipe.getHolder().scheduleNetworkGuiUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }
}
