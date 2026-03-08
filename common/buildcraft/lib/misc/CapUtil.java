package buildcraft.lib.misc;

import buildcraft.api.inventory.IItemTransactor;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class CapUtil {
    @Nonnull
    public static final Capability<IItemHandler> CAP_ITEMS;

    @Nonnull
    public static final Capability<IFluidHandler> CAP_FLUIDS;

    @Nonnull
    public static final Capability<IItemTransactor> CAP_ITEM_TRANSACTOR;

    @CapabilityInject(IItemTransactor.class)
    private static Capability<IItemTransactor> capTransactor;

    static {
//        if (!ModLoader.get().hasReachedState(LoaderState.INITIALIZATION)) {
        if (ModLoadingContext.get().getActiveContainer().getCurrentState().ordinal() < ModLoadingStage.CONSTRUCT.ordinal()) {
            throw new IllegalStateException("Used CapUtil too early, you must wait until init or later!");
        }

        CAP_ITEMS = getCapNonNull(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, IItemHandler.class);
        CAP_FLUIDS = getCapNonNull(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, IFluidHandler.class);

        registerAbstractCapability(IItemTransactor.class);

        // FIXME: Move cap registration into API!
        CAP_ITEM_TRANSACTOR = getCapNonNull(capTransactor, IItemTransactor.class);
    }

    @Nonnull
    private static <T> Capability<T> getCapNonNull(Capability<T> cap, Class<T> clazz) {
        if (cap == null) {
            throw new NullPointerException("The capability " + clazz + " was null!");
        }
        return cap;
    }

    private static <T> void registerAbstractCapability(Class<T> clazz) {
        // By default storing and creating are illegal operations, as we don't necessarily have good default impl's
        IStorage<T> ourStorage = new IStorage<T>() {
            @Override
            public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
                throw new IllegalStateException("You must provide your own implementations of " + clazz);
            }

            @Override
            public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
                throw new IllegalStateException("You must provide your own implementations of " + clazz);
            }
        };
        Callable<T> factory = () ->
        {
            throw new IllegalStateException("You must provide your own instances of " + clazz);
        };
        CapabilityManager.INSTANCE.register(clazz, ourStorage, factory);
    }

    /** Attempts to fetch the given capability from the given provider, or returns null if either of those two are
     * null. */
    @Nullable
    public static <T> LazyOptional<T> getCapability(ICapabilityProvider provider, Capability<T> capability, Direction facing) {
        if (provider == null || capability == null) {
//            return null;
            return LazyOptional.empty();
        }
        return provider.getCapability(capability, facing);
    }
}
