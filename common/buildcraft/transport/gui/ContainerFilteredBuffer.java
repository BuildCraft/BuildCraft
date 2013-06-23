package buildcraft.transport.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotPhantom;
import buildcraft.transport.TileFilteredBuffer;

public class ContainerFilteredBuffer extends BuildCraftContainer {

	private class SlotFiltered extends Slot
	{
		final TileFilteredBuffer filteredBuffer;
		
		public SlotFiltered(TileFilteredBuffer filteredBuffer, int par2, int par3, int par4) {
			super(filteredBuffer, par2, par3, par4);
			this.filteredBuffer = filteredBuffer;
		}
	    
		@Override
	    public boolean isItemValid(ItemStack itemStack) {
	    	return filteredBuffer.isStackValidForSlot(this.getSlotIndex(), itemStack);
	    }
	}
	
	private class SlotPhantomLockable extends SlotPhantom
	{
		final IInventory locks;
		
		public SlotPhantomLockable(IInventory storage, IInventory locks, int par2, int par3, int par4) {
			super(storage, par2, par3, par4);
			this.locks = locks;
		}
		
		@Override
		public boolean canAdjust() {
			return locks.getStackInSlot(this.getSlotIndex()) == null;
		}
	}
	
	IInventory playerInventory;
	TileFilteredBuffer filteredBuffer;

	public ContainerFilteredBuffer(InventoryPlayer playerInventory, TileFilteredBuffer tile) {
		super(tile.getSizeInventory());

		this.playerInventory = playerInventory;
		this.filteredBuffer = tile;

		IInventory filters = tile.getFilters();
		
		for (int col = 0; col < 9; col++) {
			// Filtered Buffer filter slots
			addSlotToContainer(new SlotPhantomLockable(filters, tile, col, 8 + col * 18, 27));
			// Filtered Buffer inventory slots
			addSlotToContainer(new SlotFiltered(tile, col, 8 + col * 18, 61));
		}
		
		// Player inventory
		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 86 + l * 18));
			}
		}

		// Player hot bar
		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 144));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return filteredBuffer.isUseableByPlayer(entityPlayer);
	}
}
