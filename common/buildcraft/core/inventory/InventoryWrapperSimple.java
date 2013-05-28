package buildcraft.core.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import buildcraft.core.utils.Utils;

public class InventoryWrapperSimple extends InventoryWrapper {

	private final int[] slots;
	
	public InventoryWrapperSimple(IInventory inventory) {
		super(inventory);
		slots = Utils.createSlotArray(0, inventory.getSizeInventory());
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return slots;
	}

	@Override
	public boolean canInsertItem(int slotIndex, ItemStack itemstack, int side) {
		return isStackValidForSlot(slotIndex, itemstack);
	}

	@Override
	public boolean canExtractItem(int slotIndex, ItemStack itemstack, int side) {
		return true;
	}

}
