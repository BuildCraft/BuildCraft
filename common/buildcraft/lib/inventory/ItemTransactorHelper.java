package buildcraft.lib.inventory;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;

public class ItemTransactorHelper {
    public static IItemTransactor getTransactor(TileEntity tile, EnumFacing face) {
        IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
        if (handler == null) {
            return NoSpaceTransactor.INSTANCE;
        }
        if (handler instanceof IItemTransactor) {
            return (IItemTransactor) handler;
        }
        return new ItemHandlerWrapper(handler);
    }

    public enum NoSpaceTransactor implements IItemTransactor {
        INSTANCE;

        @Override
        public ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate) {
            return stack;
        }

        @Override
        public List<ItemStack> insertAll(List<ItemStack> stacks, boolean simulate) {
            return stacks;
        }

        @Override
        public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
            return null;
        }
    }

    private static final class ItemHandlerWrapper extends AbstractInvItemTransactor {
        private final IItemHandler wrapped;

        public ItemHandlerWrapper(IItemHandler handler) {
            this.wrapped = handler;
        }

        @Override
        protected ItemStack insert(int slot, ItemStack stack, boolean simulate) {
            return wrapped.insertItem(slot, stack, simulate);
        }

        @Override
        protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
            if (max < 0 || max < min) return null;
            ItemStack current = wrapped.getStackInSlot(slot);
            if (current == null || current.stackSize < min) return null;
            if (filter.matches(safeCopy(current))) {
                return wrapped.extractItem(slot, max, simulate);
            }
            return null;
        }

        @Override
        protected int getSlots() {
            return wrapped.getSlots();
        }

        @Override
        protected boolean isEmpty(int slot) {
            return wrapped.getStackInSlot(slot) == null;
        }
    }
}
