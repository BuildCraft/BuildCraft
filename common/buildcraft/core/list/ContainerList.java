/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.list;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;
import buildcraft.api.lists.ListMatchHandler;
import buildcraft.api.lists.ListMatchHandler.Type;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.widget.WidgetPhantomSlot;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.net.MessageContainer;

public class ContainerList extends ContainerBC_Neptune {
    private static final int PLAYER_INV_START = 103;

    private static final int NET_ID_LABEL = 0;
    private static final int NET_ID_BUTTON = 1;

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

        addFullPlayerInventory(PLAYER_INV_START);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    public ItemStack getListItemStack() {
        ItemStack toTry = player.getHeldItemMainhand();
        if (toTry != null && toTry.getItem() instanceof ItemList_BC8) {
            return toTry;
        }

        toTry = player.getHeldItemOffhand();
        if (toTry != null && toTry.getItem() instanceof ItemList_BC8) {
            return toTry;
        }
        return null;
    }

    private void setStack(final int lineIndex, final int slotIndex, final ItemStack stack) {
        lines[lineIndex].setStack(slotIndex, stack);
        ListHandler.saveLines(getListItemStack(), lines);
    }

    public void switchButton(final int lineIndex, final int button) {
        lines[lineIndex].toggleOption(button);

        if (player.worldObj.isRemote) {
            BCMessageHandler.netWrapper.sendToServer(new MessageContainer(this, (buffer) -> {
                buffer.writeByte(NET_ID_BUTTON);
                buffer.writeByte(lineIndex);
                buffer.writeByte(button);
            }));
        } else if (button == 1 || button == 2) {
            ListMatchHandler.Type type = lines[lineIndex].getSortingType();
            if (type == Type.MATERIAL || type == Type.TYPE) {
                WidgetListSlot[] widgetSlots = slots[lineIndex];
                for (int i = 1; i < widgetSlots.length; i++) {
                    widgetSlots[i].setStack(null, true);
                }
            }
        }

        ListHandler.saveLines(getListItemStack(), lines);
    }

    public void setLabel(final String text) {
        BCCoreItems.list.setName(getListItemStack(), text);

        if (player.worldObj.isRemote) {
            BCMessageHandler.netWrapper.sendToServer(new MessageContainer(this, (buffer) -> {
                buffer.writeByte(NET_ID_LABEL);
                buffer.writeString(text);
            }));
        }
    }

    @Override
    public void handleMessage(MessageContext ctx, PacketBuffer payload, Side side) {
        if (side == Side.SERVER) {
            int id = payload.readUnsignedByte();
            switch (id) {
                case NET_ID_LABEL: {
                    setLabel(payload.readStringFromBuffer(1024));
                    return;
                }
                case NET_ID_BUTTON: {
                    int lineIndex = payload.readUnsignedByte();
                    int button = payload.readUnsignedByte();
                    switchButton(lineIndex, button);
                    return;
                }
                default: {
                    BCLog.logger.warn("[core.gui.list] Unknown message id " + id);
                }
            }
        }
    }
}
