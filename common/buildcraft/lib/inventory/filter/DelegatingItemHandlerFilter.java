package buildcraft.lib.inventory.filter;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

import buildcraft.api.core.IStackFilter;

import javax.annotation.Nonnull;

public class DelegatingItemHandlerFilter implements IStackFilter {
    private final ISingleStackFilter perStackFilter;
    private final IItemHandler handler;

    public DelegatingItemHandlerFilter(ISingleStackFilter perStackFilter, IItemHandler handler) {
        this.perStackFilter = perStackFilter;
        this.handler = handler;
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (perStackFilter.matches(handler.getStackInSlot(slot), stack)) {
                return true;
            }
        }
        return false;
    }
}
