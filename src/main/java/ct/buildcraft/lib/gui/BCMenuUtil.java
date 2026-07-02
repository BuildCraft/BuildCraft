package ct.buildcraft.lib.gui;

import ct.buildcraft.lib.gui.slot.IPhantomSlot;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class BCMenuUtil {
    private static final int PLAYER_SLOT_COUNT = 36;

    private BCMenuUtil() {}

    public static boolean handleFakeSlotClick(AbstractContainerMenu menu, int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId < 0 || slotId >= menu.slots.size()) {
            return false;
        }

        Slot slot = menu.slots.get(slotId);
        if (slot instanceof RecordSlot recordSlot) {
            handleRecordSlotClick(menu, recordSlot, dragType, clickType, player);
            return true;
        }
        if (slot instanceof IPhantomSlot phantomSlot) {
            handlePhantomSlotClick(menu, slot, phantomSlot, dragType, clickType, player);
            return true;
        }
        return false;
    }

    private static void handleRecordSlotClick(AbstractContainerMenu menu, RecordSlot slot, int dragType, ClickType clickType, Player player) {
        if (clickType == ClickType.CLONE) {
            if (player.getAbilities().instabuild && menu.getCarried().isEmpty() && slot.hasItem()) {
                menu.setCarried(slot.getItem().copy());
            }
            return;
        }

        if (clickType == ClickType.QUICK_MOVE) {
            slot.clearRecordedStack();
            return;
        }

        if (clickType != ClickType.PICKUP && clickType != ClickType.PICKUP_ALL) {
            return;
        }

        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            slot.clearRecordedStack();
        } else {
            slot.setRecordedStack(carried);
        }
    }

    private static void handlePhantomSlotClick(AbstractContainerMenu menu, Slot slot, IPhantomSlot phantom, int dragType, ClickType clickType, Player player) {
        if (clickType == ClickType.CLONE) {
            if (player.getAbilities().instabuild && menu.getCarried().isEmpty() && slot.hasItem()) {
                menu.setCarried(slot.getItem().copy());
            }
            return;
        }

        if (clickType != ClickType.PICKUP && clickType != ClickType.PICKUP_ALL && clickType != ClickType.QUICK_MOVE) {
            return;
        }

        ItemStack carried = menu.getCarried();
        if (carried.isEmpty() || clickType == ClickType.QUICK_MOVE) {
            slot.set(ItemStack.EMPTY);
            slot.setChanged();
        } else if (!StackUtil.canMerge(carried, StackUtil.asNonNull(slot.getItem()))) {
            ItemStack copy = carried.copy();
            copy.setCount(1);
            slot.set(copy);
            slot.setChanged();
        } else if (phantom.canAdjustCount()) {
            ItemStack stack = slot.getItem().copy();
            int increment = dragType == 1 ? 1 : carried.getCount();
            stack.grow(increment);
            if (stack.getCount() > stack.getMaxStackSize()) {
                stack.setCount(stack.getMaxStackSize());
            }
            slot.set(stack);
            slot.setChanged();
        }
    }

    public static ItemStack quickMoveStack(AbstractContainerMenu menu, Player player, int index) {
        if (index < 0 || index >= menu.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = menu.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        if (menu.slots.size() <= PLAYER_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean playerInventoryFirst = menu.slots.get(0).container instanceof net.minecraft.world.entity.player.Inventory;

        int playerStart;
        int playerEnd;
        int machineStart;
        int machineEnd;

        if (playerInventoryFirst) {
            playerStart = 0;
            playerEnd = PLAYER_SLOT_COUNT;
            machineStart = PLAYER_SLOT_COUNT;
            machineEnd = menu.slots.size();
        } else {
            machineStart = 0;
            machineEnd = menu.slots.size() - PLAYER_SLOT_COUNT;
            playerStart = machineEnd;
            playerEnd = menu.slots.size();
        }

        int mainStart = playerStart;
        int mainEnd = playerStart + 27;
        int hotbarStart = mainEnd;
        int hotbarEnd = playerEnd;

        if (index >= machineStart && index < machineEnd) {
            if (!moveItemStackTo(menu, stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= playerStart && index < playerEnd) {
            if (!moveItemStackTo(menu, stack, machineStart, machineEnd, false)) {
                if (index >= mainStart && index < mainEnd) {
                    if (!moveItemStackTo(menu, stack, hotbarStart, hotbarEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= hotbarStart && index < hotbarEnd) {
                    if (!moveItemStackTo(menu, stack, mainStart, mainEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    private static boolean moveItemStackTo(AbstractContainerMenu menu, ItemStack stack, int startIndex, int endIndex, boolean reverse) {
        boolean changed = false;
        int index = reverse ? endIndex - 1 : startIndex;

        while (!stack.isEmpty() && inRange(index, startIndex, endIndex)) {
            Slot slot = menu.slots.get(index);
            if (canMoveInto(slot, stack)) {
                ItemStack inSlot = slot.getItem();
                if (!inSlot.isEmpty() && ItemStack.isSameItemSameTags(stack, inSlot)) {
                    int max = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());
                    int room = max - inSlot.getCount();
                    if (room > 0) {
                        int moved = Math.min(room, stack.getCount());
                        inSlot.grow(moved);
                        stack.shrink(moved);
                        slot.set(inSlot);
                        slot.setChanged();
                        changed = true;
                    }
                }
            }
            index += reverse ? -1 : 1;
        }

        index = reverse ? endIndex - 1 : startIndex;
        while (!stack.isEmpty() && inRange(index, startIndex, endIndex)) {
            Slot slot = menu.slots.get(index);
            if (canMoveInto(slot, stack) && slot.getItem().isEmpty()) {
                int max = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());
                ItemStack moved = stack.copy();
                moved.setCount(Math.min(max, stack.getCount()));
                slot.set(moved);
                slot.setChanged();
                stack.shrink(moved.getCount());
                changed = true;
                break;
            }
            index += reverse ? -1 : 1;
        }

        return changed;
    }

    private static boolean inRange(int index, int startIndex, int endIndex) {
        return index >= startIndex && index < endIndex;
    }

    private static boolean canMoveInto(Slot slot, ItemStack stack) {
        return slot != null && slot.isActive() && !isFakeSlot(slot) && slot.mayPlace(stack);
    }

    private static boolean isFakeSlot(Slot slot) {
        return slot instanceof RecordSlot || slot instanceof IPhantomSlot;
    }
}
