// STUB(R.Chen): Forge FluidUtil — compile shim. TODO: migrate call sites to Fabric Transfer API.
package net.minecraftforge.fluids;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import buildcraft.lib.compat.FluidStackBC;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidUtil {

    @Nullable
    public static FluidStackBC getFluidContained(ItemStack stack) {
        // TODO(R.Chen): implement via Fabric FluidStorage.ITEM
        return null;
    }

    @Nullable
    public static IFluidHandlerItem getFluidHandler(ItemStack stack) {
        // TODO(R.Chen): implement via Fabric FluidStorage.ITEM
        return null;
    }

    public static ItemStack getFilledBucket(FluidStackBC fluidStack) {
        // TODO(R.Chen): implement via vanilla BucketItem
        return ItemStack.EMPTY;
    }
}
