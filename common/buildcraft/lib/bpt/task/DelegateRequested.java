package buildcraft.lib.bpt.task;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.bpt.IMaterialProvider.IRequested;
import buildcraft.api.bpt.IMaterialProvider.IRequestedFluid;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;

public abstract class DelegateRequested implements IRequested {
    protected IRequested delegate;

    @Override
    public boolean lock() throws IllegalStateException {
        return this.delegate.lock();
    }

    @Override
    public boolean isLocked() {
        return this.delegate.isLocked();
    }

    @Override
    public void use() throws IllegalStateException {
        this.delegate.use();
    }

    @Override
    public void release() {
        this.delegate.release();
    }

    public static class DelegateItem extends DelegateRequested implements IRequestedItem {
        private final ItemStack requested;

        public DelegateItem(ItemStack requested) {
            this.requested = requested;
        }

        @Override
        public ItemStack getRequested() {
            return requested.copy();
        }
    }

    public static class DelegateFluid extends DelegateRequested implements IRequestedFluid {
        private final FluidStack requested;

        public DelegateFluid(FluidStack requested) {
            this.requested = requested;
        }

        @Override
        public FluidStack getRequested() {
            return requested.copy();
        }
    }
}
