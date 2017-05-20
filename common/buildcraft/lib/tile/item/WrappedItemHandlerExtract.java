package buildcraft.lib.tile.item;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class WrappedItemHandlerExtract extends DelegateItemHandler {
    public WrappedItemHandlerExtract(IItemHandlerModifiable delegate) {
        super(delegate);
    }

    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return stack;
    }
}
