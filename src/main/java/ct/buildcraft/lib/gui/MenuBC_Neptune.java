/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.api.core.BCDebugging;
import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.gui.slot.IPhantomSlot;
import ct.buildcraft.lib.gui.slot.SlotPhantom;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.net.IPayloadWriter;
import ct.buildcraft.lib.net.MessageContainer;
import ct.buildcraft.lib.net.MessageManager;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkEvent;

public abstract class MenuBC_Neptune extends AbstractContainerMenu {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.container");

    protected static final IdAllocator IDS = new IdAllocator("container");
    /** Generic "data" id. Used by all containers which only have 1 id to write out (no point in making EVERY container
     * have an {@link IdAllocator} if they only allocate one. */
    public static final int NET_DATA = IDS.allocId("DATA");
    public static final int NET_WIDGET = IDS.allocId("WIDGET");
    public static final int NET_SET_PHANTOM = IDS.allocId("SET_PHANTOM");
    public static final int NET_SET_PHANTOM_MULTI = IDS.allocId("NET_SET_PHANTOM_MULTI");

    public final Inventory playerInventory;
    private final List<Widget_Neptune<?>> widgets = new ArrayList<>();

    public MenuBC_Neptune(Inventory playerInventory, MenuType<?> type, int id) {
    	super(type, id);
        this.playerInventory = playerInventory;
    }

    /** @return The {@link IdAllocator} that allocates all ID's for this class, and its parent classes. All subclasses
     *         should override this if they allocate their own ids after calling
     *         {@link IdAllocator#makeChild(String)} */
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    protected void addFullPlayerInventory(int startX, int startY) {
        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlot(new Slot(playerInventory, sx + sy * 9 + 9, startX + sx * 18, startY + sy * 18));
            }
        }

        for (int sx = 0; sx < 9; sx++) {
        	addSlot(new Slot(playerInventory, sx, startX + sx * 18, startY + 58));
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

    @Nullable
    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        Slot slot = slotId < 0 ? null : this.slots.get(slotId);
        if (slot == null) {
            super.clicked(slotId, dragType, clickType, player);
        }

        ItemStack playerStack = getCarried();
        if (slot instanceof IPhantomSlot) {
            IPhantomSlot phantom = (IPhantomSlot) slot;
            if (playerStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else if (!StackUtil.canMerge(playerStack, StackUtil.asNonNull(slot.getItem()))) {
                ItemStack copy = playerStack.copy();
                copy.setCount(1);
                slot.set(copy);
            } else if (phantom.canAdjustCount()) {
                ItemStack stack = slot.getItem();
                if (stack.getCount() < stack.getMaxStackSize()) {
                    stack.grow(1);
                    slot.set(stack);
                }
            }
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        Slot firstSlot = slots.get(0);
        int playerInventorySize = 36;
        boolean playerInventoryFirst = firstSlot.container instanceof Inventory;

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slots.size() == playerInventorySize) return ItemStack.EMPTY;
            if (playerInventoryFirst) {
                if (index < playerInventorySize) {
                    if (!this.moveItemStackTo(itemstack1, playerInventorySize, slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, 0, playerInventorySize, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (index < slots.size() - playerInventorySize) {
                    if (!this.moveItemStackTo(itemstack1, slots.size() - playerInventorySize,
                        slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, 0, slots.size() - playerInventorySize,
                    true)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.safeInsert(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public static ItemStack safeCopy(ItemStack in) {
        return in == null ? null : in.copy();
    }

    // Package-private so that the widget itself can send this
    void sendWidgetData(Widget_Neptune<?> widget, IPayloadWriter writer) {
        int widgetId = widgets.indexOf(widget);
        if (widgetId == -1) {
            if (DEBUG) {
                throw new IllegalArgumentException(
                    "Invalid Widget Request! (" + (widget == null ? "null" : widget.getClass()) + ")");
            } else {
                BCLog.logger.warn("[lib.container] Received an invalid widget sending request!");
                BCLog.logger
                    .warn("[lib.container]   Widget {id = " + widgetId + ", class = " + widget.getClass() + "}");
                BCLog.logger.warn("[lib.container]   Container {class = " + getClass() + "}");
                BCLog.logger.warn(
                    "[lib.container]   Player {class = " + playerInventory.getClass() + ", name = " + playerInventory.getName() + "}");
            }
        } else {
            sendMessage(NET_WIDGET, (buffer) -> {
                buffer.writeShort(widgetId);
                writer.write(buffer);
            });
        }
    }

    public final void sendMessage(int id) {
        LogicalSide side = playerInventory.player.level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
        sendMessage(id, (buffer) -> writeMessage(id, buffer, side));
    }

    public final void sendMessage(int id, IPayloadWriter writer) {
        FriendlyByteBuf payload = MessageUtil.write(writer);
        MessageContainer message = new MessageContainer(containerId, id, payload);
        if (playerInventory.player.level.isClientSide) {
            MessageManager.sendToServer(message);
        } else {
            MessageManager.sendTo(message, (ServerPlayer) playerInventory.player);
        }
    }

    public void writeMessage(int id, FriendlyByteBuf buffer, LogicalSide side) {}

    public void readMessage(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        if (id == NET_WIDGET) {
            int widgetId = buffer.readUnsignedShort();
            if (widgetId < 0 || widgetId >= widgets.size()) {
                if (DEBUG) {
                    String string = "Received unknown or invalid widget ID " + widgetId + " on side " + side;
                    if (side == LogicalSide.SERVER) {
                        string += " (for player " + playerInventory.getName() + ")";
                    }
                    BCLog.logger.warn(string);
                }
            } else {
                Widget_Neptune<?> widget = widgets.get(widgetId);
                if (side == LogicalSide.SERVER) {
                    widget.handleWidgetDataServer(ctx, buffer);
                } else if (side == LogicalSide.CLIENT) {
                    widget.handleWidgetDataClient(ctx, buffer);
                }
            }
        } else if (side == LogicalSide.SERVER) {
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

    private void readSingleSetPhantom(FriendlyByteBuf buffer, NetworkEvent.Context ctx) throws IOException {
        int idx = buffer.readVarInt();
        ItemStack stack = buffer.readItem();
        if (idx >= 0 && idx < slots.size()) {
            Slot s = slots.get(idx);
            if (s instanceof SlotPhantom) {
                SlotPhantom ph = (SlotPhantom) s;
                IItemHandlerAdv handler = ph.itemHandler;
                if (handler instanceof IItemHandlerModifiable && handler.canSet(ph.handlerIndex, stack)) {
                    ((IItemHandlerModifiable) handler).setStackInSlot(ph.handlerIndex, stack);
                } else {
                    // log rather than throw an exception because of bugged/naughty clients
                    String s2 = "[lib.container] Received an illegal phantom slot setting request! ";
                    s2 += "[The item handler disallowed the replacement] (Client = ";
                    s2 += ctx.getSender().getName() + ", slot_index = " + idx;
                    s2 += ", stack = " + stack + ")";
                    BCLog.logger.warn(s2);
                }
                return;
            }
        }

        // log rather than throw an exception because of bugged/naughty clients
        String s2 = "[lib.container] Received an illegal phantom slot setting request! ";
        s2 += "[Didn't find a phantom slot for the given index] (Client = ";
        s2 += ctx.getSender().getName() + ", slot_index = " + idx;
        s2 += ", stack = " + stack + ")";
        BCLog.logger.warn(s2);
    }

    /** @throws IllegalArgumentException if a {@link SlotPhantom} couldn't be found with that handler and index */
    public void sendSetPhantomSlot(IItemHandler handler, int index, ItemStack to) {
        sendSetPhantomSlot(findPhantomSlot(handler, index), to);
    }

    /** @param stacks The list of stacks to send. NOTE: this list CAN include nulls -- that indicates that the item
     *            should not be changed.
     * @throws IllegalArgumentException if {@link List#size() stacks.size()} differs from {@link IItemHandler#getSlots()
     *             handler.getSlots()}, or if a {@link SlotPhantom} couldn't be found for that handler and any of the
     *             indexes associated with it. */
    public void sendSetPhantomSlots(IItemHandler handler, List<ItemStack> stacks) {
        if (handler.getSlots() < stacks.size()) {
            throw new IllegalStateException("Too many ItemStacks's in the list to change, compared to the "
                + "size of the inventory! (list = " + stacks + ", handler = " + handler + ")");
        }
        int[] indexes = new int[stacks.size()];
        NonNullList<ItemStack> destinationStacks = NonNullList.create();
        int i2 = 0;
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack == null) {
                continue;
            }
            destinationStacks.add(stack);
            indexes[i2] = findPhantomSlot(handler, i);
            i2++;
        }
        indexes = Arrays.copyOf(indexes, i2);
        sendSetPhantomSlots(indexes, destinationStacks);
    }

    /** @throws IllegalArgumentException if a phantom slot cannot be found */
    private int findPhantomSlot(IItemHandler handler, int index) {
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
            buffer.writeItem(to);
        });
    }

    private void sendSetPhantomSlots(int[] indexes, NonNullList<ItemStack> stacks) {
        if (indexes.length != stacks.size()) {
            throw new IllegalArgumentException("Sizes don't match! (" + indexes.length + " vs " + stacks.size() + ")");
        }
        sendMessage(NET_SET_PHANTOM_MULTI, (buffer) -> {
            buffer.writeByte(indexes.length);
            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];
                ItemStack stack = stacks.get(i);
                buffer.writeVarInt(index);
                buffer.writeItem(stack);
            }
        });
    }
    
	@OnlyIn(Dist.CLIENT)
	private static Minecraft mc = Minecraft.getInstance();
	
	@OnlyIn(Dist.CLIENT)
	public static ContainerLevelAccess CreateClientLevelAccess(FriendlyByteBuf buf) {
		return ContainerLevelAccess.create(mc.level, buf.readBlockPos());
	}
}
