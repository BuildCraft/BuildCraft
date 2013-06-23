package buildcraft.core.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Creates a deep copy of an existing IInventory.
 *
 * Useful for performing inventory manipulations and then examining the results
 * without affecting the original inventory.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class InventoryCopy implements IInventory {

	private IInventory orignal;
	private ItemStack contents[];

	public InventoryCopy(IInventory orignal) {
		this.orignal = orignal;
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
				onInventoryChanged();
				return itemstack;
			}
			ItemStack itemstack1 = contents[i].splitStack(j);
			if (contents[i].stackSize <= 0) {
				contents[i] = null;
			}
			onInventoryChanged();
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
		onInventoryChanged();
	}

	@Override
	public String getInvName() {
		return orignal.getInvName();
	}

	@Override
	public int getInventoryStackLimit() {
		return orignal.getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged() {
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return orignal.getStackInSlotOnClosing(slot);
	}

	@Override
	public boolean isInvNameLocalized() {
		return orignal.isInvNameLocalized();
	}

	@Override
	public boolean isStackValidForSlot(int slot, ItemStack stack) {
		return orignal.isStackValidForSlot(slot, stack);
	}

	public ItemStack[] getItemStacks() {
		return contents;
	}
}
