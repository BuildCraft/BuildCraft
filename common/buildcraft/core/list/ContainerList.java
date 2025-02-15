/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;

import buildcraft.api.lists.ListMatchHandler.Type;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.widget.WidgetPhantomSlot;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ContainerList extends ContainerBC_Neptune<ItemList_BC8> {
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

    // final WidgetListSlot[][] slots;
    public final WidgetListSlot[][] slots;

    // class WidgetListSlot extends WidgetPhantomSlot
    public class WidgetListSlot extends WidgetPhantomSlot {
        // final int lineIndex, slotIndex;
        public final int lineIndex, slotIndex;

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

    public ContainerList(MenuType menuType, int id, Player iPlayer) {
        super(menuType, id, iPlayer);

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
//    public boolean canInteractWith(EntityPlayer player)
    public boolean stillValid(Player player) {
        return !getListItemStack().isEmpty();
    }

    @Nonnull
    public ItemStack getListItemStack() {
        ItemStack toTry = player.getMainHandItem();
        if (!toTry.isEmpty() && toTry.getItem() instanceof ItemList_BC8) {
            return toTry;
        }

        toTry = player.getOffhandItem();
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

        if (player.level().isClientSide) {
            sendMessage(ID_BUTTON, (buffer) ->
            {
                buffer.writeByte(lineIndex);
                buffer.writeByte(button);
            });
        } else if (button == 1 || button == 2) {
            Type type = lines[lineIndex].getSortingType();
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
        BCCoreItems.list.get().setName(getListItemStack(), text);

        if (player.level().isClientSide) {
            sendMessage(ID_LABEL, (buffer) -> buffer.writeUtf(text));
        }
    }

    @Override
//    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException
    public void readMessage(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_SERVER) {
            if (id == ID_BUTTON) {
                int lineIndex = buffer.readUnsignedByte();
                int button = buffer.readUnsignedByte();
                switchButton(lineIndex, button);
            } else if (id == ID_LABEL) {
                setLabel(buffer.readUtf(1024));
            }
        }
    }
}
