package buildcraft.lib.bpt.builder;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.bpt.IMaterialProvider.IRequested;
import buildcraft.api.bpt.IMaterialProvider.IRequestedFluid;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;

public abstract class AbstractRequested implements IRequested {
    private boolean isLocked, isUsed;

    @Override
    public boolean lock() throws IllegalStateException {
        if (isUsed) {
            throw new IllegalStateException("Attempted to lock a used up request!");
        }
        if (isLocked) {
            return true;
        }
        if (attemptLock()) {
            isLocked = true;
        }
        return isLocked;
    }

    @Override
    public boolean isLocked() {
        return isLocked | isUsed;
    }

    @Override
    public void use() throws IllegalStateException {
        if (isLocked) {
            unlock(true);
            isUsed = true;
            isLocked = false;
        } else {
            throw new IllegalStateException("Attempted to use up a non-locked resource!");
        }
    }

    @Override
    public void release() {
        if (isLocked) {
            unlock(false);
            isLocked = false;
        }
    }

    /** Provides the implementation behind {@link #lock()}. This is only called when this object is currently not locked
     * (so this will never be called before a call to {@link #unlock(boolean)} if this returned false
     * 
     * @return true if this could be locked, false if it couldn't. */
    protected abstract boolean attemptLock();

    /** Provides the implementation behind {@link #release()} and {@link #use()}. This is only called after the last
     * attempt to {@link #attemptLock()} returned true, and this has not been called since.
     * 
     * @param useUp If true then the requested thing should be removed from its origin, false if not. */
    protected abstract void unlock(boolean useUp);

    public interface IRequestLocker<I extends IRequested> {
        boolean attemptLock(I requested);

        void unlock(I requested, boolean useUp);
    }

    public static class RequestedItem extends AbstractRequested implements IRequestedItem {
        private final IRequestLocker<IRequestedItem> locker;
        private final ItemStack stack;

        public RequestedItem(IRequestLocker<IRequestedItem> locker, ItemStack stack) {
            this.locker = locker;
            this.stack = stack;
        }

        @Override
        public ItemStack getRequested() {
            return stack;
        }

        @Override
        protected boolean attemptLock() {
            return locker.attemptLock(this);
        }

        @Override
        protected void unlock(boolean useUp) {
            locker.unlock(this, useUp);
        }
    }

    public static class RequestedFluid extends AbstractRequested implements IRequestedFluid {
        private final IRequestLocker<IRequestedFluid> locker;
        private final FluidStack stack;

        public RequestedFluid(IRequestLocker<IRequestedFluid> locker, FluidStack stack) {
            this.locker = locker;
            this.stack = stack;
        }

        @Override
        public FluidStack getRequested() {
            return stack;
        }

        @Override
        protected boolean attemptLock() {
            return locker.attemptLock(this);
        }

        @Override
        protected void unlock(boolean useUp) {
            locker.unlock(this, useUp);
        }
    }
}
