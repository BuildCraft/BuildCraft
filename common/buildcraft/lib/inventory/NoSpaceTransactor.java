package buildcraft.lib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;

import buildcraft.lib.misc.StackUtil;

public enum NoSpaceTransactor implements IItemTransactor {
    INSTANCE;

    @Override
    public ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate) {
        return stack;
    }

    @Override
    public NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
        return stacks;
    }

    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        return StackUtil.EMPTY;
    }
}