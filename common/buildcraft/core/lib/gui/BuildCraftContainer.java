/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.gui;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.core.lib.gui.slots.IPhantomSlot;
import buildcraft.core.lib.gui.slots.SlotBase;
import buildcraft.core.lib.gui.widgets.Widget;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.network.PacketGuiWidget;
import buildcraft.lib.container.BCContainer_BC8;

public abstract class BuildCraftContainer extends BCContainer_BC8 {

    private List<Widget> widgets = new ArrayList<>();
    private int inventorySize;

    public BuildCraftContainer(EntityPlayer player, int inventorySize) {
        super(player);
        this.inventorySize = inventorySize;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public List<Widget> getWidgets() {
        return widgets;
    }

    public void addSlot(Slot slot) {
        addSlotToContainer(slot);
    }

    public void addWidget(Widget widget) {
        widget.addToContainer(this);
        widgets.add(widget);
    }

    public void sendWidgetDataToClient(Widget widget, EntityPlayer player, byte[] data) {
        PacketGuiWidget pkt = new PacketGuiWidget(this.player, windowId, widgets.indexOf(widget), data);
        BuildCraftCore.instance.sendToPlayer(player, pkt);
    }

    public void sendWidgetDataToServer(Widget widget, byte[] data) {
        PacketGuiWidget pkt = new PacketGuiWidget(this.player, windowId, widgets.indexOf(widget), data);
        BuildCraftCore.instance.sendToServer(pkt);
    }

    public void handleWidgetData(int widgetId, byte[] data) {
        InputStream input = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(input);
        if (widgetId < 0 || widgetId >= widgets.size()) BCLog.logger.warn("Found a packet with an invalid widget ID! (" + widgetId + ")");
        else {
            Widget widget = widgets.get(widgetId);
            try {
                if (getPlayer().isServerWorld()) widget.handleServerPacketData(stream);
                else widget.handleClientPacketData(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCraftGuiOpened(ICrafting player) {
        super.onCraftGuiOpened(player);
        for (Widget widget : widgets) {
            widget.initWidget(player);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Widget widget : widgets) {
            for (ICrafting player : listeners) {
                widget.updateWidget(player);
            }
        }
    }

    @Override
    public ItemStack slotClick(int slotNum, int mouseButton, ClickType clickType, EntityPlayer player) {
        Slot slot = slotNum < 0 ? null : (Slot) this.inventorySlots.get(slotNum);
        if (slot instanceof IPhantomSlot) {
            return slotClickPhantom(slot, mouseButton, clickType, player);
        }
        return super.slotClick(slotNum, mouseButton, clickType, player);
    }

    private ItemStack slotClickPhantom(Slot slot, int mouseButton, ClickType clickType, EntityPlayer player) {
        ItemStack stack = null;
        if (mouseButton == 2) {
            if (((IPhantomSlot) slot).canAdjust()) {
                slot.putStack(null);
            }
        } else if (mouseButton == 0 || mouseButton == 1) {
            InventoryPlayer playerInv = player.inventory;
            slot.onSlotChanged();
            ItemStack stackSlot = slot.getStack();
            ItemStack stackHeld = playerInv.getItemStack();

            if (stackSlot != null) {
                stack = stackSlot.copy();
            }

            if (stackSlot == null) {
                if (stackHeld != null && slot.isItemValid(stackHeld)) {
                    fillPhantomSlot(slot, stackHeld, mouseButton, clickType);
                }
            } else if (stackHeld == null) {
                adjustPhantomSlot(slot, mouseButton, clickType);
                slot.onPickupFromSlot(player, playerInv.getItemStack());
            } else if (slot.isItemValid(stackHeld)) {
                if (StackHelper.canStacksMerge(stackSlot, stackHeld)) {
                    adjustPhantomSlot(slot, mouseButton, clickType);
                } else {
                    fillPhantomSlot(slot, stackHeld, mouseButton, clickType);
                }
            }
        }
        return stack;
    }

    protected void adjustPhantomSlot(Slot slot, int mouseButton, ClickType clickType) {
        if (!((IPhantomSlot) slot).canAdjust()) {
            return;
        }
        ItemStack stackSlot = slot.getStack();
        int stackSize;
        if (clickType == ClickType.QUICK_MOVE || clickType == ClickType.PICKUP_ALL) {// modifier == 1
            stackSize = mouseButton == 0 ? (stackSlot.stackSize + 1) / 2 : stackSlot.stackSize * 2;
        } else {
            stackSize = mouseButton == 0 ? stackSlot.stackSize - 1 : stackSlot.stackSize + 1;
        }

        if (stackSize > slot.getSlotStackLimit()) {
            stackSize = slot.getSlotStackLimit();
        }

        stackSlot.stackSize = stackSize;

        if (stackSlot.stackSize <= 0) {
            slot.putStack(null);
        }
    }

    protected void fillPhantomSlot(Slot slot, ItemStack stackHeld, int mouseButton, ClickType clickType) {
        if (!((IPhantomSlot) slot).canAdjust()) {
            return;
        }
        int stackSize = mouseButton == 0 ? stackHeld.stackSize : 1;
        if (stackSize > slot.getSlotStackLimit()) {
            stackSize = slot.getSlotStackLimit();
        }
        ItemStack phantomStack = stackHeld.copy();
        phantomStack.stackSize = stackSize;

        slot.putStack(phantomStack);
    }

    protected boolean shiftItemStack(ItemStack stackToShift, int start, int end) {
        boolean changed = false;
        if (stackToShift.isStackable()) {
            for (int slotIndex = start; stackToShift.stackSize > 0 && slotIndex < end; slotIndex++) {
                Slot slot = inventorySlots.get(slotIndex);
                ItemStack stackInSlot = slot.getStack();
                if (stackInSlot != null && StackHelper.canStacksMerge(stackInSlot, stackToShift)) {
                    int resultingStackSize = stackInSlot.stackSize + stackToShift.stackSize;
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
                    if (resultingStackSize <= max) {
                        stackToShift.stackSize = 0;
                        stackInSlot.stackSize = resultingStackSize;
                        slot.onSlotChanged();
                        changed = true;
                    } else if (stackInSlot.stackSize < max) {
                        stackToShift.stackSize -= max - stackInSlot.stackSize;
                        stackInSlot.stackSize = max;
                        slot.onSlotChanged();
                        changed = true;
                    }
                }
            }
        }
        if (stackToShift.stackSize > 0) {
            for (int slotIndex = start; stackToShift.stackSize > 0 && slotIndex < end; slotIndex++) {
                Slot slot = inventorySlots.get(slotIndex);
                ItemStack stackInSlot = slot.getStack();
                if (stackInSlot == null) {
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
                    stackInSlot = stackToShift.copy();
                    stackInSlot.stackSize = Math.min(stackToShift.stackSize, max);
                    stackToShift.stackSize -= stackInSlot.stackSize;
                    slot.putStack(stackInSlot);
                    slot.onSlotChanged();
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean tryShiftItem(ItemStack stackToShift, int numSlots) {
        for (int machineIndex = 0; machineIndex < numSlots - 9 * 4; machineIndex++) {
            Slot slot = inventorySlots.get(machineIndex);
            if (slot instanceof SlotBase && !((SlotBase) slot).canShift()) {
                continue;
            }
            if (slot instanceof IPhantomSlot) {
                continue;
            }
            if (!slot.isItemValid(stackToShift)) {
                continue;
            }
            if (shiftItemStack(stackToShift, machineIndex, machineIndex + 1)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack originalStack = null;
        Slot slot = inventorySlots.get(slotIndex);
        int numSlots = inventorySlots.size();
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            originalStack = stackInSlot.copy();
            if (slotIndex >= numSlots - 9 * 4 && tryShiftItem(stackInSlot, numSlots)) {
                // NOOP
            } else if (slotIndex >= numSlots - 9 * 4 && slotIndex < numSlots - 9) {
                if (!shiftItemStack(stackInSlot, numSlots - 9, numSlots)) {
                    return null;
                }
            } else if (slotIndex >= numSlots - 9 && slotIndex < numSlots) {
                if (!shiftItemStack(stackInSlot, numSlots - 9 * 4, numSlots - 9)) {
                    return null;
                }
            } else if (!shiftItemStack(stackInSlot, numSlots - 9 * 4, numSlots)) {
                return null;
            }
            slot.onSlotChange(stackInSlot, originalStack);
            if (stackInSlot.stackSize <= 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
            if (stackInSlot.stackSize == originalStack.stackSize) {
                return null;
            }
            slot.onPickupFromSlot(player, stackInSlot);
        }
        return originalStack;
    }

    public int getInventorySize() {
        return inventorySize;
    }
}
