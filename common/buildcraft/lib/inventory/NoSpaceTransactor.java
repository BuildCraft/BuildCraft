package buildcraft.lib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;

import buildcraft.lib.misc.StackUtil;

import javax.annotation.Nonnull;

public enum NoSpaceTransactor implements IItemTransactor {
    INSTANCE;

    @Nonnull
    @Override
    public ItemStack insert(@Nonnull ItemStack stack, boolean allOrNone, boolean simulate) {
        return stack;
    }

    @Override
    public NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
        return stacks;
    }

    @Nonnull
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        return StackUtil.EMPTY;
    }
}