/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.gui.slot.IPhantomSlot;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;

public abstract class ContainerBC_Neptune extends ScreenHandler {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.container");

    @Override
    public boolean canUse(net.minecraft.entity.player.PlayerEntity player) { return true; }

    protected static final IdAllocator IDS = new IdAllocator("container");
    public static final int NET_DATA = IDS.allocId("DATA");
    public static final int NET_WIDGET = IDS.allocId("WIDGET");
    public static final int NET_SET_PHANTOM = IDS.allocId("SET_PHANTOM");
    public static final int NET_SET_PHANTOM_MULTI = IDS.allocId("NET_SET_PHANTOM_MULTI");

    public final PlayerEntity player;
    private final List<Widget_Neptune<?>> widgets = new ArrayList<>();

    public ContainerBC_Neptune(PlayerEntity player, int syncId) {
        super(null, syncId);
        this.player = player;
    }

    public IdAllocator getIdAllocator() {
        return IDS;
    }

    protected void addFullPlayerInventory(int startX, int startY) {
        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlot(new Slot(player.getInventory(), sx + sy * 9 + 9, startX + sx * 18, startY + sy * 18));
            }
        }
        for (int sx = 0; sx < 9; sx++) {
            addSlot(new Slot(player.getInventory(), sx, startX + sx * 18, startY + 58));
        }
    }

    protected void addFullPlayerInventory(int startY) {
        addFullPlayerInventory(8, startY);
    }

    protected <W extends Widget_Neptune<?>> W addWidget(W widget) {
        if (widget == null) throw new NullPointerException("widget");
        widgets.add(widget);
        return widget;
    }

    public ImmutableList<Widget_Neptune<?>> getWidgets() {
        return ImmutableList.copyOf(widgets);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        Slot slot = slotIndex < 0 ? null : this.slots.get(slotIndex);
        if (slot instanceof IPhantomSlot) {
            IPhantomSlot phantom = (IPhantomSlot) slot;
            ItemStack playerStack = this.getCursorStack();
            if (playerStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else if (!ItemStack.canCombine(playerStack, slot.getStack())) {
                ItemStack copy = playerStack.copy();
                copy.setCount(1);
                slot.setStack(copy);
            } else if (phantom.canAdjustCount()) {
                ItemStack stack = slot.getStack();
                if (stack.getCount() < stack.getMaxCount()) {
                    stack.increment(1);
                    slot.setStack(stack);
                }
            }
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        Slot firstSlot = this.slots.get(0);
        int playerInventorySize = 36;
        boolean playerInventoryFirst = firstSlot.inventory instanceof PlayerInventory;

        if (slot != null && slot.hasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (slots.size() == playerInventorySize) return ItemStack.EMPTY;
            if (playerInventoryFirst) {
                if (index < playerInventorySize) {
                    if (!this.insertItem(itemstack1, playerInventorySize, this.slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(itemstack1, 0, playerInventorySize, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (index < this.slots.size() - playerInventorySize) {
                    if (!this.insertItem(itemstack1, this.slots.size() - playerInventorySize, this.slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(itemstack1, 0, this.slots.size() - playerInventorySize, true)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return itemstack;
    }

    public static ItemStack safeCopy(ItemStack in) {
        return in == null ? null : in.copy();
    }

    void sendWidgetData(Widget_Neptune<?> widget, IPayloadWriter writer) {
        int widgetId = widgets.indexOf(widget);
        if (widgetId == -1) {
            if (DEBUG) {
                throw new IllegalArgumentException(
                    "Invalid Widget Request! (" + (widget == null ? "null" : widget.getClass()) + ")");
            } else {
                BCLog.logger.warn("[lib.container] Received an invalid widget sending request!");
                BCLog.logger.warn("[lib.container]   Widget {id = " + widgetId + ", class = " + widget.getClass() + "}");
                BCLog.logger.warn("[lib.container]   Container {class = " + getClass() + "}");
                BCLog.logger.warn("[lib.container]   Player {class = " + player.getClass()
                    + ", name = " + player.getName().getString() + "}");
            }
        } else {
            sendMessage(NET_WIDGET, (buffer) -> {
                buffer.writeShort(widgetId);
                writer.write(buffer);
            });
        }
    }

    public final void sendMessage(int id) {
        boolean isClient = player.getWorld().isClient;
        sendMessage(id, (buffer) -> writeMessage(id, buffer, isClient));
    }

    // STUB(R.Chen): container networking deferred — MessageContainer not yet migrated to FabricPacket.
    public final void sendMessage(int id, IPayloadWriter writer) {
    }

    public void writeMessage(int id, PacketBufferBC buffer, boolean isClient) {}

    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, Object ctx) throws IOException {
        if (id == NET_WIDGET) {
            int widgetId = buffer.readUnsignedShort();
            if (widgetId < 0 || widgetId >= widgets.size()) {
                if (DEBUG) {
                    String string = "Received unknown or invalid widget ID " + widgetId + " on side "
                        + (isClient ? "CLIENT" : "SERVER");
                    if (!isClient) {
                        string += " (for player " + player.getName().getString() + ")";
                    }
                    BCLog.logger.warn(string);
                }
            } else {
                Widget_Neptune<?> widget = widgets.get(widgetId);
                if (!isClient) {
                    widget.handleWidgetDataServer(ctx, buffer);
                } else {
                    widget.handleWidgetDataClient(ctx, buffer);
                }
            }
        } else if (!isClient) {
            if (id == NET_SET_PHANTOM) {
                readSingleSetPhantom(buffer, ctx);
            } else if (id == NET_SET_PHANTOM_MULTI) {
                int count = buffer.readUnsignedByte();
                for (int i = 0; i < count; i++) {
                    readSingleSetPhantom(buffer, ctx);
                }
            }
        }
    }

    private void readSingleSetPhantom(PacketBufferBC buffer, Object ctx) throws IOException {
        int idx = buffer.readVarInt();
        ItemStack stack = buffer.readItemStack();
        if (idx >= 0 && idx < slots.size()) {
            Slot s = slots.get(idx);
            if (s instanceof SlotPhantom) {
                SlotPhantom ph = (SlotPhantom) s;
                if (ph.itemHandler.canSet(ph.handlerIndex, stack)) {
                    ph.itemHandler.setStackInSlot(ph.handlerIndex, stack);
                } else {
                    BCLog.logger.warn("[lib.container] Received an illegal phantom slot setting request! "
                        + "[The item handler disallowed the replacement] (slot_index = " + idx
                        + ", stack = " + stack + ")");
                }
                return;
            }
        }
        BCLog.logger.warn("[lib.container] Received an illegal phantom slot setting request! "
            + "[Didn't find a phantom slot for the given index] (slot_index = " + idx
            + ", stack = " + stack + ")");
    }

    public void sendSetPhantomSlot(Object handler, int index, ItemStack to) {
        sendSetPhantomSlot(findPhantomSlot(handler, index), to);
    }

    public void sendSetPhantomSlots(Object handler, List<ItemStack> stacks) {
        // STUB(R.Chen): phantom slot batch send — deferred with networking layer
    }

    private int findPhantomSlot(Object handler, int index) {
        int i = 0;
        for (Slot slot : slots) {
            if (slot instanceof SlotPhantom) {
                SlotPhantom ph = (SlotPhantom) slot;
                if (ph.itemHandler == handler && ph.handlerIndex == index) {
                    return i;
                }
            }
            i++;
        }
        throw new IllegalArgumentException("Couldn't find a slot for " + index + " @ " + handler + " in " + getClass());
    }

    public void sendSetPhantomSlot(SlotPhantom slot, ItemStack to) {
        int index = slots.indexOf(slot);
        if (index == -1) {
            throw new IllegalArgumentException("Couldn't find a slot for " + slot + " in " + getClass());
        }
        sendSetPhantomSlot(index, to);
    }

    private void sendSetPhantomSlot(int phIndex, ItemStack to) {
        sendMessage(NET_SET_PHANTOM, (buffer) -> {
            buffer.writeVarInt(phIndex);
            buffer.writeItemStack(to);
        });
    }

    private void sendSetPhantomSlots(int[] indexes, DefaultedList<ItemStack> stacks) {
        if (indexes.length != stacks.size()) {
            throw new IllegalArgumentException("Sizes don't match! (" + indexes.length + " vs " + stacks.size() + ")");
        }
        sendMessage(NET_SET_PHANTOM_MULTI, (buffer) -> {
            buffer.writeByte(indexes.length);
            for (int i = 0; i < indexes.length; i++) {
                buffer.writeVarInt(indexes[i]);
                buffer.writeItemStack(stacks.get(i));
            }
        });
    }
}
