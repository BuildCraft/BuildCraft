package buildcraft.lib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.api.inventory.IItemTransactor;

public interface IItemExtractable extends IItemTransactor {
    @Override
    default ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate) {
        return stack;
    }

    @Override
    default NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
        return stacks;
    }
}
