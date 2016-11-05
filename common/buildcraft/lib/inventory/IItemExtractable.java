package buildcraft.lib.inventory;

import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.api.inventory.IItemTransactor;

public interface IItemExtractable extends IItemTransactor {
    @Override
    default ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate) {
        return stack;
    }

    @Override
    default List<ItemStack> insert(List<ItemStack> stacks, boolean simulate) {
        return stacks;
    }
}
