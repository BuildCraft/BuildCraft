package buildcraft.lib.bpt.builder;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.bpt.IBuilderAccessor.IRequested;
import buildcraft.api.bpt.IBuilderAccessor.IRequestedFluid;
import buildcraft.api.bpt.IBuilderAccessor.IRequestedItem;

public class RequestedFree implements IRequested {

    @Override
    public boolean lock() throws IllegalStateException {
        return true;
    }

    @Override
    public boolean isLocked() {
        return true;
    }

    @Override
    public void use() throws IllegalStateException {}

    @Override
    public void release() {}

    public static class FreeItem extends RequestedFree implements IRequestedItem {
        public static final IRequestedItem NO_ITEM = new FreeItem(null);

        private final ItemStack stack;

        public FreeItem(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack getRequested() {
            return stack;
        }
    }

    public static class FreeFluid extends RequestedFree implements IRequestedFluid {
        private final FluidStack stack;

        public FreeFluid(FluidStack stack) {
            this.stack = stack;
        }

        @Override
        public FluidStack getRequested() {
            return stack;
        }
    }
}
