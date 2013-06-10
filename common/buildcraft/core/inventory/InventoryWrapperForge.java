package buildcraft.core.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.core.utils.Utils;

public class InventoryWrapperForge extends InventoryWrapper {

	private int[][] sidemap;
	
	public InventoryWrapperForge(net.minecraftforge.common.ISidedInventory inventory) {
		super(inventory);
		
		sidemap = new int[ForgeDirection.VALID_DIRECTIONS.length][];
		for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			sidemap[direction.ordinal()] = Utils.createSlotArray(inventory.getStartInventorySide(direction), inventory.getSizeInventorySide(direction));
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return sidemap[side];
	}

	@Override
	public boolean canInsertItem(int slotIndex, ItemStack itemstack, int side) {
		return true;
	}

	@Override
	public boolean canExtractItem(int slotIndex, ItemStack itemstack, int side) {
		return true;
	}

}
