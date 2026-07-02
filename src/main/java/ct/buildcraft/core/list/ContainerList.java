/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.list;

import java.io.IOException;

import javax.annotation.Nonnull;

import ct.buildcraft.api.lists.ListMatchHandler;
import ct.buildcraft.core.BCCore;
import ct.buildcraft.core.BCCoreItems;
import ct.buildcraft.core.item.ItemList_BC8;
import ct.buildcraft.lib.gui.MenuBC_Neptune;
import ct.buildcraft.lib.gui.widget.WidgetPhantomSlot;
import ct.buildcraft.lib.list.ListHandler;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.lib.misc.data.IdAllocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class ContainerList extends MenuBC_Neptune {
    // Network ID's

    protected static final IdAllocator IDS = MenuBC_Neptune.IDS.makeChild("list");
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
    
    final ContainerLevelAccess access; 
    
	public ContainerList(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, ContainerLevelAccess.NULL);
	}

    public ContainerList(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(playerInventory, BCCore.LIST_MENU.get(), containerId);
        this.access = access;

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
	public boolean stillValid(Player player) {
        return !getListItemStack().isEmpty();
    }

    @Nonnull
    public ItemStack getListItemStack() {
        ItemStack toTry = playerInventory.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!toTry.isEmpty() && toTry.getItem() instanceof ItemList_BC8) {
            return toTry;
        }

        toTry = playerInventory.player.getItemInHand(InteractionHand.OFF_HAND);
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

        if (access == ContainerLevelAccess.NULL) {
            sendMessage(ID_BUTTON, (buffer) -> {
                buffer.writeByte(lineIndex);
                buffer.writeByte(button);
            });
        } else if (button == 1 || button == 2) {
            ListMatchHandler.Type type = lines[lineIndex].getSortingType();
            if (type == ListMatchHandler.Type.MATERIAL || type == ListMatchHandler.Type.TYPE) {
                WidgetListSlot[] widgetSlots = slots[lineIndex];
                for (int i = 1; i < widgetSlots.length; i++) {
                    widgetSlots[i].setStack(StackUtil.EMPTY, true);
                }
            }
        }

        ListHandler.saveLines(getListItemStack(), lines);
    }

    public void setLabel(final String text) {
        BCCoreItems.LIST.get().setLabelName(getListItemStack(), text);

        if (access == ContainerLevelAccess.NULL) {
            sendMessage(ID_LABEL, (buffer) -> buffer.writeUtf(text));
        }
    }

    @Override
    public void readMessage(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == LogicalSide.SERVER) {
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
