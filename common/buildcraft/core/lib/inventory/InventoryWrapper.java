/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

public abstract class InventoryWrapper implements ISidedInventory {

    IInventory inventory;

    public InventoryWrapper(IInventory inventory) {
        this.inventory = inventory;
    }

    /* DIRECT MAPPING */
    @Override
    public int getSizeInventory() {
        return inventory.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return inventory.getStackInSlot(slotIndex);
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        return inventory.decrStackSize(slotIndex, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        return inventory.getStackInSlotOnClosing(slotIndex);
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemstack) {
        inventory.setInventorySlotContents(slotIndex, itemstack);
    }

    @Override
    public int getInventoryStackLimit() {
        return inventory.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        inventory.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return inventory.isUseableByPlayer(entityplayer);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        inventory.openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        inventory.closeInventory(player);
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack) {
        return inventory.isItemValidForSlot(slotIndex, itemstack);
    }

    public boolean hasCustomName() {
        return inventory.hasCustomName();
    }

    @Override
    public int getField(int id) {
        return inventory.getField(id);
    }

    @Override
    public void setField(int id, int value) {
        inventory.setField(id, value);
    }

    @Override
    public int getFieldCount() {
        return inventory.getFieldCount();
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public String getCommandSenderName() {
        return inventory.getCommandSenderName();
    }

    @Override
    public IChatComponent getDisplayName() {
        return inventory.getDisplayName();
    }

    /* STATIC HELPER */
    public static ISidedInventory getWrappedInventory(Object inventory) {
        if (inventory instanceof ISidedInventory) {
            return (ISidedInventory) inventory;
        } else if (inventory instanceof IInventory) {
            return new InventoryWrapperSimple(InvUtils.getInventory((IInventory) inventory));
        } else {
            return null;
        }
    }
}
