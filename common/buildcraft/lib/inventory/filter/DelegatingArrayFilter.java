package buildcraft.lib.inventory.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.api.core.IStackFilter;

public class DelegatingArrayFilter implements IStackFilter {
    private final ISingleStackFilter perStackFilter;
    private final NonNullList<ItemStack> stacks;

    public DelegatingArrayFilter(ISingleStackFilter perStackFilter, NonNullList<ItemStack> stacks) {
        this.perStackFilter = perStackFilter;
        this.stacks = stacks;
    }

    @Override
    public boolean matches(ItemStack stack) {
        for (ItemStack possible : stacks) {
            if (perStackFilter.matches(possible, stack)) {
                return true;
            }
        }
        return false;
    }
}
