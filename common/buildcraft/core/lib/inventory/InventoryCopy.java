/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

/** Creates a deep copy of an existing IInventory.
 *
 * Useful for performing inventory manipulations and then examining the results without affecting the original
 * inventory. */
public class InventoryCopy implements IInventory {

    private IInventory original;
    private ItemStack[] contents;

    public InventoryCopy(IInventory orignal) {
        this.original = orignal;
        contents = new ItemStack[orignal.getSizeInventory()];
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = orignal.getStackInSlot(i);
            if (stack != null) {
                contents[i] = stack.copy();
            }
        }
    }

    @Override
    public int getSizeInventory() {
        return contents.length;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return contents[i];
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        if (contents[i] != null) {
            if (contents[i].stackSize <= j) {
                ItemStack itemstack = contents[i];
                contents[i] = null;
                return itemstack;
            }
            ItemStack itemstack1 = contents[i].splitStack(j);
            if (contents[i].stackSize <= 0) {
                contents[i] = null;
            }
            return itemstack1;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        contents[i] = itemstack;
        if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
            itemstack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return original.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return true;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return original.getStackInSlotOnClosing(slot);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return original.isItemValidForSlot(slot, stack);
    }

    public ItemStack[] getItemStacks() {
        return contents;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public String getCommandSenderName() {
        return original.getCommandSenderName();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public IChatComponent getDisplayName() {
        return original.getDisplayName();
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {}
}
