package buildcraft.core.lib.block;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * Implemented by Blocks which have an inventory Comparator override.
 */
public interface IComparatorInventory {
	boolean doesSlotCountComparator(TileEntity tile, int slot, ItemStack stack);
}
