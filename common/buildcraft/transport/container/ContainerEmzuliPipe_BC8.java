package buildcraft.transport.container;

import java.io.IOException;
import java.util.EnumMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.Widget_Neptune;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

public class ContainerEmzuliPipe_BC8 extends ContainerBC_Neptune {
    public final PipeBehaviourEmzuli behaviour;
    public final EnumMap<SlotIndex, PaintWidget> paintWidgets = new EnumMap<>(SlotIndex.class);
    private final ItemHandlerSimple filterInv;

    public ContainerEmzuliPipe_BC8(EntityPlayer player, PipeBehaviourEmzuli behaviour) {
        super(player);
        this.behaviour = behaviour;
        this.filterInv = behaviour.invFilters;
        behaviour.pipe.getHolder().onPlayerOpen(player);

        addFullPlayerInventory(84);

        addSlotToContainer(new SlotPhantom(filterInv, 0, 25, 21));
        addSlotToContainer(new SlotPhantom(filterInv, 1, 25, 49));
        addSlotToContainer(new SlotPhantom(filterInv, 2, 134, 21));
        addSlotToContainer(new SlotPhantom(filterInv, 3, 134, 49));

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
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        behaviour.pipe.getHolder().onPlayerClose(player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return true;// FIXME!
    }

    public static class PaintWidget extends Widget_Neptune<ContainerEmzuliPipe_BC8> {
        public final SlotIndex index;

        public PaintWidget(ContainerEmzuliPipe_BC8 container, SlotIndex index) {
            super(container);
            this.index = index;
        }

        public void setColour(EnumDyeColor colour) {
            sendWidgetData((buffer) -> {
                MessageUtil.writeEnumOrNull(buffer, colour);
            });
        }

        @Override
        public IMessage handleWidgetDataServer(MessageContext ctx, PacketBufferBC buffer) throws IOException {
            EnumDyeColor colour = MessageUtil.readEnumOrNull(buffer, EnumDyeColor.class);
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
