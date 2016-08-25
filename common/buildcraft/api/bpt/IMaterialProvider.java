package buildcraft.api.bpt;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

public interface IMaterialProvider {

    /** Requests a single item stack.
     * 
     * @return A requested item that will attempt to provide the item to you. */
    IRequestedItem requestStack(ItemStack stack);

    /** Requests a (single) {@link ItemStack} that would be required to place the given {@link IBlockState}. */
    IRequestedItem requestStackForBlock(IBlockState state);

    IRequestedFluid requestFluid(FluidStack fluid);

    /** Designates *something* that can be requested. Use a child interface rather than this directly.
     *
     * @param <T> */
    public interface IRequested {

        /** Attempts to fully reserve the object, but without actually using it.
         * 
         * @return True if this object was available and has been properly reserved, or if this has been previously
         *         locked, False if not all of this request has been locked, so the world MIGHT have been changed.
         * @throws IllegalStateException if this has already been locked, and {@link #release()} has been called
         *             successfully. */
        boolean lock() throws IllegalStateException;

        /** @return True if this stack is currently locked, or it has already been used. */
        boolean isLocked();

        /** Uses up the stack, unlocking it and making this request useless. Future calls to {@link #lock()} will throw
         * an {@link IllegalStateException}.
         * 
         * @throws IllegalStateException if this was not locked previously. */
        void use() throws IllegalStateException;

        /** Unlocks this request WITHOUT using it up. Note that this never throws, so it is safe to call this at any
         * time. */
        void release();
    }

    /** An item stack that has preciously been requested. This starts off unlocked (it may or may not actually
     * exist). */
    public interface IRequestedItem extends IRequested {
        /** @return The {@link ItemStack} that was requested. */
        ItemStack getRequested();
    }

    /** A fluid stack that has preciously been requested. This starts off unlocked (it may or may not actually
     * exist). */
    public interface IRequestedFluid extends IRequested {
        /** @return The {@link FluidStack} that was requested */
        FluidStack getRequested();
    }

}
