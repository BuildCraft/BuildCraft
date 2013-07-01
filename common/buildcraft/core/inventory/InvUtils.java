package buildcraft.core.inventory;

import buildcraft.core.inventory.InventoryIterator.IInvSlot;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
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
}
