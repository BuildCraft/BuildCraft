package buildcraft.lib.inventory;

import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;

public enum NoSpaceTransactor implements IItemTransactor {
    INSTANCE;

    @Override
    public ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate) {
        return stack;
    }

    @Override
    public List<ItemStack> insert(List<ItemStack> stacks, boolean simulate) {
        return stacks;
    }

    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        return null;
    }
}