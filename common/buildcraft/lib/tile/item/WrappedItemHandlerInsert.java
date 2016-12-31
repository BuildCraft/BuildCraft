package buildcraft.lib.tile.item;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.lib.misc.StackUtil;

public class WrappedItemHandlerInsert extends DelegateItemHandler {

    public WrappedItemHandlerInsert(IItemHandlerModifiable delegate) {
        super(delegate);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return StackUtil.EMPTY;
    }
}
