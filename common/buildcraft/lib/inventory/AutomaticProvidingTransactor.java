package buildcraft.lib.inventory;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;

import buildcraft.lib.misc.StackUtil;

/** Provides an {@link IItemTransactor} that cannot be inserted or extracted from directly, but implements
 * {@link IItemExtractable} so as to be noticed by pipes (and other machines) as one that will auto-insert into it. */
public enum AutomaticProvidingTransactor implements IItemExtractable {
    INSTANCE;

    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        return StackUtil.EMPTY;
    }
}
