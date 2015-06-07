/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

/** Wrapper class used to specify part of an existing inventory to be treated as a complete inventory. Used primarily to
 * map a side of an ISidedInventory, but it is also helpful for complex inventories such as the Tunnel Bore. */
public class InventoryMapper implements IInventory {

    private final IInventory inv;
    private final int start;
    private final int size;
    private int stackSizeLimit = -1;
    private boolean checkItems = true;

    /** Creates a new InventoryMapper
     *
     * @param inv The backing inventory
     * @param start The starting index
     * @param size The size of the new inventory, take care not to exceed the end of the backing inventory */
    public InventoryMapper(IInventory inv, int start, int size) {
        this(inv, start, size, true);
    }

    public InventoryMapper(IInventory inv, int start, int size, boolean checkItems) {
        this.inv = inv;
        this.start = start;
        this.size = size;
        this.checkItems = checkItems;
    }

    public IInventory getBaseInventory() {
        return inv;
    }

    @Override
    public int getSizeInventory() {
        return size;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv.getStackInSlot(start + slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return inv.decrStackSize(start + slot, amount);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemstack) {
        inv.setInventorySlotContents(start + slot, itemstack);
    }

    @Override
    public String getCommandSenderName() {
        return inv.getCommandSenderName();
    }

    public void setStackSizeLimit(int limit) {
        stackSizeLimit = limit;
    }

    @Override
    public int getInventoryStackLimit() {
        return stackSizeLimit > 0 ? stackSizeLimit : inv.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return inv.isUseableByPlayer(entityplayer);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        inv.openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        inv.closeInventory(player);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return inv.getStackInSlotOnClosing(start + slot);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (checkItems) {
            return inv.isItemValidForSlot(start + slot, stack);
        }
        return true;
    }

    @Override
    public boolean hasCustomName() {
        return inv.hasCustomName();
    }

    @Override
    public void markDirty() {
        inv.markDirty();
    }

    @Override
    public IChatComponent getDisplayName() {
        return null;
    }

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
