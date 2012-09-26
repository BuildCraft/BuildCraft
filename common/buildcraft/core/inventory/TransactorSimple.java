package buildcraft.core.inventory;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import buildcraft.api.core.Orientations;

public class TransactorSimple extends Transactor {

	protected IInventory inventory;
	
	public TransactorSimple(IInventory inventory) {
		this.inventory = inventory;
	}
	
	@Override
	public int inject(ItemStack stack, Orientations orientation, boolean doAdd) {
		
		int injected = 0;
		
		int slot = 0;
		while((slot = getPartialSlot(stack, orientation, slot)) >= 0
				&& injected < stack.stackSize)
			injected += addToSlot(slot, stack, injected, doAdd);

		slot = 0;
		while((slot = getEmptySlot(orientation)) >= 0
				&& injected < stack.stackSize)
			injected += addToSlot(slot, stack, injected, doAdd);
		
		return injected;
	}
	
	protected int getPartialSlot(ItemStack stack, Orientations orientation, int skipAhead) {
		return getPartialSlot(stack, skipAhead, inventory.getSizeInventory());
	}
	
	protected int getPartialSlot(ItemStack stack, int startSlot, int endSlot) {

		for(int i = startSlot; i < endSlot; i++) {
			if(inventory.getStackInSlot(i) == null)
				continue;
			
			if(!inventory.getStackInSlot(i).isItemEqual(stack) || !ItemStack.func_77970_a(inventory.getStackInSlot(i), stack))
				continue;
			
			if(inventory.getStackInSlot(i).stackSize >= inventory.getStackInSlot(i).getMaxStackSize())
				continue;
			
			return i;
		}
	
		return -1;
	}

	protected int getEmptySlot(Orientations orientation) {
		return getEmptySlot(0, inventory.getSizeInventory());
	}
	
	protected int getEmptySlot(int startSlot, int endSlot) {
		for(int i = startSlot; i < endSlot; i++)
			if(inventory.getStackInSlot(i) == null)
				return i;
		
		return -1;
	}
	
	protected int addToSlot(int slot, ItemStack stack, int injected, boolean doAdd) {
		int remaining = stack.stackSize - injected;
		
		if(inventory.getStackInSlot(slot) == null) {
			if(doAdd) {
				inventory.setInventorySlotContents(slot, stack.copy());
				inventory.getStackInSlot(slot).stackSize = remaining;
			}
			return remaining;
		}
		
		if(!inventory.getStackInSlot(slot).isItemEqual(stack) || !ItemStack.func_77970_a(inventory.getStackInSlot(slot), stack))
			return 0;
		
		int space = inventory.getStackInSlot(slot).getMaxStackSize() - inventory.getStackInSlot(slot).stackSize;
		if(space <= 0)
			return 0;
		
		if(space >= remaining) {
			if(doAdd)
				inventory.getStackInSlot(slot).stackSize += remaining;
			return remaining;
		} else {
			if(doAdd)
				inventory.getStackInSlot(slot).stackSize = inventory.getStackInSlot(slot).getMaxStackSize();
			return space;
		}
	}
}
