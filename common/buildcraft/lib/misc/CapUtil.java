package buildcraft.lib.misc;

import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.inventory.IItemTransactor;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapUtil {
    // @CapabilityInject(IItemTransactor.class)
    private static Capability<IItemTransactor> capTransactor = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static Capability<IFluidHandlerAdv> capFluidHandlerAdv = CapabilityManager.get(new CapabilityToken<>() {
    });
    @Nonnull
    public static final Capability<IItemHandler> CAP_ITEMS = getCapNonNull(ForgeCapabilities.ITEM_HANDLER, IItemHandler.class);

    @Nonnull
    public static final Capability<IFluidHandler> CAP_FLUIDS = getCapNonNull(ForgeCapabilities.FLUID_HANDLER, IFluidHandler.class);
    @Nonnull
    public static final Capability<IItemTransactor> CAP_ITEM_TRANSACTOR = getCapNonNull(capTransactor, IItemTransactor.class);
    public static final Capability<IFluidHandlerAdv> CAP_FLUID_HANDLER_ADV = getCapNonNull(capFluidHandlerAdv, IFluidHandlerAdv.class);

    // Calen: called in BCLib
    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent evt) {
        evt.register(IItemTransactor.class);
        evt.register(IFluidHandlerAdv.class);
    }

    @Nonnull
    private static <T> Capability<T> getCapNonNull(Capability<T> cap, Class<T> clazz) {
        if (cap == null) {
            throw new NullPointerException("The capability " + clazz + " was null!");
        }
        return cap;
    }

//    private static <T> void registerAbstractCapability(Class<T> clazz) {
//        // By default storing and creating are illegal operations, as we don't necessarily have good default impl's
//        IStorage<T> ourStorage = new IStorage<T>() {
//            @Override
//            public Tag writeNBT(Capability<T> capability, T instance, Direction side) {
//                throw new IllegalStateException("You must provide your own implementations of " + clazz);
//            }
//
//            @Override
//            public void readNBT(Capability<T> capability, T instance, Direction side, Tag nbt) {
//                throw new IllegalStateException("You must provide your own implementations of " + clazz);
//            }
//        };
//        Callable<T> factory = () -> {
//            throw new IllegalStateException("You must provide your own instances of " + clazz);
//        };
//        CapabilityManager.INSTANCE.register(clazz, ourStorage, factory);
//    }

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
