/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.container;

import buildcraft.api.net.IMessage;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.lib.gui.ContainerPipe;
import buildcraft.lib.gui.Widget_Neptune;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.DyeColor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.EnumMap;

public class ContainerEmzuliPipe_BC8 extends ContainerPipe {
    public final PipeBehaviourEmzuli behaviour;
    public final EnumMap<SlotIndex, PaintWidget> paintWidgets = new EnumMap<>(SlotIndex.class);
    private final ItemHandlerSimple filterInv;

    public ContainerEmzuliPipe_BC8(ContainerType menuType, int id, PlayerEntity player, PipeBehaviourEmzuli behaviour) {
        super(menuType, id, player, behaviour.pipe.getHolder());
        this.behaviour = behaviour;
        this.filterInv = behaviour.invFilters;
        // Calen: moved to BCTransportGuis#openPipeGui and BCTransportMenuTypes
//        behaviour.pipe.getHolder().onPlayerOpen(player);

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
//    public void onContainerClosed(PlayerEntity player)
    public void removed(PlayerEntity player) {
//        super.onContainerClosed(player);
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
        public IMessage handleWidgetDataServer(NetworkEvent.Context ctx, PacketBufferBC buffer) throws IOException {
            DyeColor colour = MessageUtil.readEnumOrNull(buffer, DyeColor.class);
            if (colour == null) {
                container.behaviour.slotColours.remove(index);
            } else {
                container.behaviour.slotColours.put(index, colour);
            }
            container.behaviour.pipe.getHolder().scheduleNetworkGuiUpdate(PipeMessageReceiver.BEHAVIOUR);
            return null;
        }
    }
}
