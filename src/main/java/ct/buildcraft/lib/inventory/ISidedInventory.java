package ct.buildcraft.lib.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface ISidedInventory extends Container
{
    int[] getSlotsForFace(Direction side);

    /**
     * Returns true if automation can insert the given item in the given slot from the given side.
     */
    boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction);

    /**
     * Returns true if automation can extract the given item in the given slot from the given side.
     */
    boolean canExtractItem(int index, ItemStack stack, Direction direction);
}