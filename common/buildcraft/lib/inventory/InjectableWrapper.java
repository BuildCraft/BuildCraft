package buildcraft.lib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;

import buildcraft.lib.misc.StackUtil;

import javax.annotation.Nonnull;

public class InjectableWrapper implements IItemTransactor {
    private final IInjectable injectable;
    private final EnumFacing from;

    public InjectableWrapper(IInjectable injectable, EnumFacing facing) {
        this.injectable = injectable;
        this.from = facing;
    }

    @Nonnull
    @Override
    public ItemStack insert(@Nonnull ItemStack stack, boolean allOrNone, boolean simulate) {
        if (allOrNone) {
            ItemStack leftOver = injectable.injectItem(stack, false, from, null, 0);
            if (leftOver.isEmpty()) {
                ItemStack reallyLeftOver = injectable.injectItem(stack, !simulate, from, null, 0);
                // sanity check: it really helps debugging
                if (!reallyLeftOver.isEmpty()) {
                    throw new IllegalStateException("Found an invalid IInjectable instance! (leftOver = "//
                        + leftOver + ", reallyLeftOver = " + reallyLeftOver + ", " + injectable.getClass() + ")");
                } else {
                    return StackUtil.EMPTY;
                }
            } else {
                return stack;
            }
        } else {
            return injectable.injectItem(stack, !simulate, from, null, 0);
        }
    }

    @Override
    public NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
        return ItemTransactorHelper.insertAllBypass(this, stacks, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        return StackUtil.EMPTY;
    }
}
