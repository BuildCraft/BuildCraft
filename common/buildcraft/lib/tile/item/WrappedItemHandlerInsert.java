package buildcraft.lib.tile.item;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandlerModifiable;

public class WrappedItemHandlerInsert extends DelegateItemHandler {

    public WrappedItemHandlerInsert(IItemHandlerModifiable delegate) {
        super(delegate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }
}
