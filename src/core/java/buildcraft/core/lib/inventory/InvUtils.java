/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.IInvSlot;
import buildcraft.core.lib.inventory.filters.IStackFilter;

public final class InvUtils {

    /** Deactivate constructor */
    private InvUtils() {}

    public static int countItems(IInventory inv, EnumFacing side, IStackFilter filter) {
        int count = 0;
        for (IInvSlot slot : InventoryIterator.getIterable(inv, side)) {
            ItemStack stack = slot.getStackInSlot();
            if (stack != null && filter.matches(stack)) {
                count += stack.stackSize;
            }
        }
        return count;
    }

    public static boolean containsItem(IInventory inv, EnumFacing side, IStackFilter filter) {
        for (IInvSlot slot : InventoryIterator.getIterable(inv, side)) {
            ItemStack stack = slot.getStackInSlot();
            if (stack != null && filter.matches(stack)) {
                return true;
            }
        }
        return false;
    }

    /** Checks if there is room for the ItemStack in the inventory.
     *
     * @param stack The ItemStack
     * @param dest The IInventory
     * @return true if room for stack */
    public static boolean isRoomForStack(ItemStack stack, EnumFacing side, IInventory dest) {
        if (stack == null || dest == null) {
            return false;
        }
        ITransactor tran = Transactor.getTransactorFor(dest);
        return tran.add(stack, side, false).stackSize > 0;
    }

    /** Attempts to move a single item from one inventory to another.
     *
     * @param source
     * @param dest
     * @param filter an IStackFilter to match against
     * @return null if nothing was moved, the stack moved otherwise */
    public static ItemStack moveOneItem(IInventory source, EnumFacing output, IInventory dest, EnumFacing intput, IStackFilter filter) {
        ITransactor imSource = Transactor.getTransactorFor(source);
        ItemStack stack = imSource.remove(filter, output, false);
        if (stack != null) {
            ITransactor imDest = Transactor.getTransactorFor(dest);
            int moved = imDest.add(stack, intput, true).stackSize;
            if (moved > 0) {
                imSource.remove(filter, output, true);
                return stack;
            }
        }
        return null;
    }

    /* STACK DROPS */
    public static void dropItems(World world, ItemStack stack, BlockPos pos) {
        if (stack == null || stack.stackSize <= 0) {
            return;
        }

        float f1 = 0.7F;
        double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
        double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
        double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
        EntityItem entityitem = new EntityItem(world, pos.getX() + d, pos.getY() + d1, pos.getZ() + d2, stack);
        entityitem.setDefaultPickupDelay();

        world.spawnEntityInWorld(entityitem);
    }

    public static void dropItems(World world, IInventory inv, BlockPos pos) {
        for (int slot = 0; slot < inv.getSizeInventory(); ++slot) {
            ItemStack items = inv.getStackInSlot(slot);

            if (items != null && items.stackSize > 0) {
                dropItems(world, inv.getStackInSlot(slot).copy(), pos);
            }
        }
    }

    public static void wipeInventory(IInventory inv) {
        for (int slot = 0; slot < inv.getSizeInventory(); ++slot) {
            inv.setInventorySlotContents(slot, null);
        }
    }

    public static NBTTagCompound getItemData(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        return nbt;
    }

    public static void addItemToolTip(ItemStack stack, String msg) {
        NBTTagCompound nbt = getItemData(stack);
        NBTTagCompound display = nbt.getCompoundTag("display");
        nbt.setTag("display", display);
        NBTTagList lore = display.getTagList("Lore", Constants.NBT.TAG_STRING);
        display.setTag("Lore", lore);
        lore.appendTag(new NBTTagString(msg));
    }

    public static void writeInvToNBT(IInventory inv, String tag, NBTTagCompound data) {
        NBTTagList list = new NBTTagList();
        for (byte slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", slot);
                stack.writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        data.setTag(tag, list);
    }

    public static void readInvFromNBT(IInventory inv, String tag, NBTTagCompound data) {
        NBTTagList list = data.getTagList(tag, Constants.NBT.TAG_COMPOUND);
        for (byte entry = 0; entry < list.tagCount(); entry++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(entry);
            int slot = itemTag.getByte("Slot");
            if (slot >= 0 && slot < inv.getSizeInventory()) {
                ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
                inv.setInventorySlotContents(slot, stack);
            }
        }
    }

    public static void readStacksFromNBT(NBTTagCompound nbt, String name, ItemStack[] stacks) {
        NBTTagList nbttaglist = nbt.getTagList(name, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < stacks.length; ++i) {
            if (i < nbttaglist.tagCount()) {
                NBTTagCompound nbttagcompound2 = nbttaglist.getCompoundTagAt(i);

                stacks[i] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
            } else {
                stacks[i] = null;
            }
        }
    }

    public static void writeStacksToNBT(NBTTagCompound nbt, String name, ItemStack[] stacks) {
        NBTTagList nbttaglist = new NBTTagList();

        for (ItemStack stack : stacks) {
            NBTTagCompound cpt = new NBTTagCompound();
            nbttaglist.appendTag(cpt);
            if (stack != null) {
                stack.writeToNBT(cpt);
            }

        }

        nbt.setTag(name, nbttaglist);
    }

    public static ItemStack consumeItem(ItemStack stack) {
        if (stack.stackSize == 1) {
            if (stack.getItem().hasContainerItem(stack)) {
                return stack.getItem().getContainerItem(stack);
            } else {
                return null;
            }
        } else {
            stack.splitStack(1);

            return stack;
        }
    }

    /** Ensures that the given inventory is the full inventory, i.e. takes double chests into account.
     *
     * @param inv
     * @return Modified inventory if double chest, unmodified otherwise. */
    public static IInventory getInventory(IInventory inv) {
        if (inv instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) inv;

            TileEntityChest adjacent = null;

            if (chest.adjacentChestXNeg != null) {
                adjacent = chest.adjacentChestXNeg;
            }

            if (chest.adjacentChestXPos != null) {
                adjacent = chest.adjacentChestXPos;
            }

            if (chest.adjacentChestZNeg != null) {
                adjacent = chest.adjacentChestZNeg;
            }

            if (chest.adjacentChestZPos != null) {
                adjacent = chest.adjacentChestZPos;
            }

            if (adjacent != null) {
                return new InventoryLargeChest("", chest, adjacent);
            }
            return inv;
        }
        return inv;
    }

    public static IInvSlot getItem(IInventory inv, IStackFilter filter) {
        for (IInvSlot s : InventoryIterator.getIterable(inv)) {
            if (s.getStackInSlot() != null && filter.matches(s.getStackInSlot())) {
                return s;
            }
        }

        return null;
    }
}
