package buildcraft.core.inventory;

import buildcraft.core.inventory.InventoryIterator.IInvSlot;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class InvUtils {

	public static int countItems(IInventory inv, ForgeDirection side, ItemStack... filter) {
		return countItems(inv, side, new ArrayStackFilter(filter));
	}

	public static int countItems(IInventory inv, ForgeDirection side, IStackFilter filter) {
		int count = 0;
		for (IInvSlot slot : InventoryIterator.getIterable(inv, side)) {
			ItemStack stack = slot.getStackInSlot();
			if (stack != null && filter.matches(stack)) {
				count += stack.stackSize;
			}
		}
		return count;
	}

	public static boolean containsItem(IInventory inv, ForgeDirection side, ItemStack... filter) {
		return containsItem(inv, side, new ArrayStackFilter(filter));
	}

	public static boolean containsItem(IInventory inv, ForgeDirection side, IStackFilter filter) {
		for (IInvSlot slot : InventoryIterator.getIterable(inv, side)) {
			ItemStack stack = slot.getStackInSlot();
			if (stack != null && filter.matches(stack)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if there is room for the ItemStack in the inventory.
	 *
	 * @param stack The ItemStack
	 * @param dest The IInventory
	 * @return true if room for stack
	 */
	public static boolean isRoomForStack(ItemStack stack, ForgeDirection side, IInventory dest) {
		if (stack == null || dest == null) {
			return false;
		}
		ITransactor tran = Transactor.getTransactorFor(dest);
		return tran.add(stack, side, false).stackSize > 0;
	}

	/**
	 * Attempts to move a single item from one inventory to another.
	 *
	 * @param source
	 * @param dest
	 * @param filer an IStackFilter to match against
	 * @return null if nothing was moved, the stack moved otherwise
	 */
	public static ItemStack moveOneItem(IInventory source, ForgeDirection output, IInventory dest, ForgeDirection intput, IStackFilter filter) {
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

	public static ItemStack moveOneItem(IInventory source, ForgeDirection output, IInventory dest, ForgeDirection intput, ItemStack... filter) {
		return moveOneItem(source, output, dest, intput, new ArrayStackFilter(filter));
	}

	/* STACK DROPS */
	public static void dropItems(World world, ItemStack stack, int i, int j, int k) {
		if (stack == null || stack.stackSize <= 0)
			return;

		float f1 = 0.7F;
		double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		EntityItem entityitem = new EntityItem(world, i + d, j + d1, k + d2, stack);
		entityitem.delayBeforeCanPickup = 10;

		world.spawnEntityInWorld(entityitem);
	}

	public static void dropItems(World world, IInventory inv, int i, int j, int k) {
		for (int slot = 0; slot < inv.getSizeInventory(); ++slot) {
			ItemStack items = inv.getStackInSlot(slot);

			if (items != null && items.stackSize > 0) {
				dropItems(world, inv.getStackInSlot(slot).copy(), i, j, k);
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
			nbt = new NBTTagCompound("tag");
			stack.setTagCompound(nbt);
		}
		return nbt;
	}

	public static void addItemToolTip(ItemStack stack, String tag, String msg) {
		NBTTagCompound nbt = getItemData(stack);
		NBTTagCompound display = nbt.getCompoundTag("display");
		nbt.setCompoundTag("display", display);
		NBTTagList lore = display.getTagList("Lore");
		display.setTag("Lore", lore);
		lore.appendTag(new NBTTagString(tag, msg));
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
		NBTTagList list = data.getTagList(tag);
		for (byte entry = 0; entry < list.tagCount(); entry++) {
			NBTTagCompound itemTag = (NBTTagCompound) list.tagAt(entry);
			int slot = itemTag.getByte("Slot");
			if (slot >= 0 && slot < inv.getSizeInventory()) {
				ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
				inv.setInventorySlotContents(slot, stack);
			}
		}
	}

	public static void readStacksFromNBT(NBTTagCompound nbt, String name, ItemStack[] stacks) {
		NBTTagList nbttaglist = nbt.getTagList(name);

		for (int i = 0; i < stacks.length; ++i) {
			if (i < nbttaglist.tagCount()) {
				NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist.tagAt(i);

				stacks[i] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
			} else {
				stacks[i] = null;
			}
		}
	}

	public static void writeStacksToNBT(NBTTagCompound nbt, String name, ItemStack[] stacks) {
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < stacks.length; ++i) {
			NBTTagCompound cpt = new NBTTagCompound();
			nbttaglist.appendTag(cpt);
			if (stacks[i] != null) {
				stacks[i].writeToNBT(cpt);
			}

		}

		nbt.setTag(name, nbttaglist);
	}

	public static ItemStack consumeItem(ItemStack stack) {
		if (stack.stackSize == 1) {
			if (stack.getItem().hasContainerItem()) {
				return stack.getItem().getContainerItemStack(stack);
			} else {
				return null;
			}
		} else {
			stack.splitStack(1);

			return stack;
		}
	}

	/**
	 * Ensures that the given inventory is the full inventory, i.e. takes double
	 * chests into account.
	 *
	 * @param inv
	 * @return Modified inventory if double chest, unmodified otherwise.
	 */
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

			if (chest.adjacentChestZPosition != null) {
				adjacent = chest.adjacentChestZPosition;
			}

			if (adjacent != null) {
				return new InventoryLargeChest("", inv, adjacent);
			}
			return inv;
		}
		return inv;
	}
}
