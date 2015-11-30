package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableFluid;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditablePower;

public interface IPipeHelper {
    IPipeContentsEditableItem getContentsForItem(ItemStack stack);

    IPipeContentsEditableFluid getContentsForFluid(FluidStack fluid);

    IPipeContentsEditablePower getContentsForPower(int power);

    /** Gets a filter that will ONLY accept the specified item, up to the maximum */
    IContentsFilter.Item getFilterForItem(Item item, int max);

    /** Gets a filter that will ONLY accept the specified fluid, up to the maximum */
    IContentsFilter.Fluid getFilterForFluid(Fluid fluid, int max);

    /** gets a filter that will only accept POWER, up the the maximum */
    IContentsFilter.Power getFilterForPower(int maxPower);

    /** Returns a filter that will only return true from {@link IContentsFilter#matches(IPipeContents)} if both of the
     * filters given return true from {@link IContentsFilter#matches(IPipeContents)}. This will return a filter with a
     * type closest matching both of the filters given. */
    IContentsFilter combineFilters(IContentsFilter filter1, IContentsFilter filter2, EnumCombiningOp op);

    /** Registers a filter combiner that can add together two filters. This will be used for
     * {@link #combineFilters(IContentsFilter, IContentsFilter, EnumCombiningOp)} and
     * {@link #getFilterCombiner(IContentsFilter, IContentsFilter)}. */
    void registerFilterCombiner(IFilterCombiner combiner);

    /** @return a combiner that can combine the filters (
     *         {@link IFilterCombiner#canCombine(IContentsFilter, IContentsFilter)} returns true). Returns null if a
     *         combiner cannot be found. */
    IFilterCombiner getFilterCombiner(IContentsFilter a, IContentsFilter b);

    // void registerFilter

    public interface IFilterCombiner {
        /** @return True if this combiner will return a valid filter from
         *         {@link #combine(IContentsFilter, IContentsFilter, EnumCombiningOp)} */
        boolean canCombine(IContentsFilter a, IContentsFilter b);

        /** @return a valid filter that combines the given filters according to the operation given */
        IContentsFilter combine(IContentsFilter filter1, IContentsFilter filter2, EnumCombiningOp op);
    }

    public enum EnumCombiningOp {
        AND,
        OR
    }
}
