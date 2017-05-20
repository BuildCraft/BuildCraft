/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.list;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.api.lists.ListMatchHandler.Type;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.widget.WidgetPhantomSlot;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;

public class ContainerList extends ContainerBC_Neptune {
    // Network ID's

    protected static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("list");
    private static final int ID_LABEL = IDS.allocId("LABEL");
    private static final int ID_BUTTON = IDS.allocId("BUTTON");

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    // Main container list

    public ListHandler.Line[] lines;

    final WidgetListSlot[][] slots;

    class WidgetListSlot extends WidgetPhantomSlot {
        final int lineIndex, slotIndex;

        public WidgetListSlot(int lineIndex, int slotIndex) {
            super(ContainerList.this);
            this.lineIndex = lineIndex;
            this.slotIndex = slotIndex;
        }

        @Override
        protected void onSetStack() {
            ContainerList.this.setStack(lineIndex, slotIndex, getStack());
        }
    }

    public ContainerList(EntityPlayer iPlayer) {
        super(iPlayer);

        lines = ListHandler.getLines(getListItemStack());

        slots = new WidgetListSlot[lines.length][ListHandler.WIDTH];
        for (int line = 0; line < lines.length; line++) {
            for (int slot = 0; slot < ListHandler.WIDTH; slot++) {
                WidgetListSlot widget = new WidgetListSlot(line, slot);
                slots[line][slot] = addWidget(widget);
                widget.setStack(lines[line].getStack(slot), false);
            }
        }

        addFullPlayerInventory(103);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Nonnull
    public ItemStack getListItemStack() {
        ItemStack toTry = player.getHeldItemMainhand();
        if (!toTry.isEmpty() && toTry.getItem() instanceof ItemList_BC8) {
            return toTry;
        }

        toTry = player.getHeldItemOffhand();
        if (!toTry.isEmpty() && toTry.getItem() instanceof ItemList_BC8) {
            return toTry;
        }
        return StackUtil.EMPTY;
    }

    void setStack(final int lineIndex, final int slotIndex, @Nonnull final ItemStack stack) {
        lines[lineIndex].setStack(slotIndex, stack);
        ListHandler.saveLines(getListItemStack(), lines);
    }

    public void switchButton(final int lineIndex, final int button) {
        lines[lineIndex].toggleOption(button);

        if (player.world.isRemote) {
            sendMessage(ID_BUTTON, (buffer) -> {
                buffer.writeByte(lineIndex);
                buffer.writeByte(button);
            });
        } else if (button == 1 || button == 2) {
            ListMatchHandler.Type type = lines[lineIndex].getSortingType();
            if (type == Type.MATERIAL || type == Type.TYPE) {
                WidgetListSlot[] widgetSlots = slots[lineIndex];
                for (int i = 1; i < widgetSlots.length; i++) {
                    widgetSlots[i].setStack(StackUtil.EMPTY, true);
                }
            }
        }

        ListHandler.saveLines(getListItemStack(), lines);
    }

    public void setLabel(final String text) {
        BCCoreItems.list.setName(getListItemStack(), text);

        if (player.world.isRemote) {
            sendMessage(ID_LABEL, (buffer) -> buffer.writeString(text));
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == Side.SERVER) {
            if (id == ID_BUTTON) {
                int lineIndex = buffer.readUnsignedByte();
                int button = buffer.readUnsignedByte();
                switchButton(lineIndex, button);
            } else if (id == ID_LABEL) {
                setLabel(buffer.readString(1024));
            }
        }
    }
}
