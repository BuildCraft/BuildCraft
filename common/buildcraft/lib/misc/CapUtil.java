package buildcraft.lib.misc;

import java.lang.reflect.Field;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;

/** Provides various @Nonnull static final fields storing various capabilities. */
public class CapUtil {
    @Nonnull
    public static final Capability<IItemHandler> CAP_ITEMS;

    @Nonnull
    public static final Capability<IFluidHandler> CAP_FLUIDS;

    @Nonnull
    public static final Capability<IItemTransactor> CAP_ITEM_TRANSACTOR;

    @Nonnull
    public static final Capability<IInjectable> CAP_ITEM_INJECTABLE;

    // Whenever forge makes "registerCapability" return a non-null capability then we can remove this
    private static final Map<String, Capability<?>> INTERNAL_CAP_MAP;

    static {
        if (!Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
            throw new IllegalStateException("Used Caputil too early, you must wait until init or later!");
        }
        try {
            // Whenever forge makes "registerCapability" return a non-null capability then we can remove this
            Field fld = CapabilityManager.class.getDeclaredField("providers");
            fld.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Capability<?>> map = (Map<String, Capability<?>>) fld.get(CapabilityManager.INSTANCE);
            INTERNAL_CAP_MAP = map;
        } catch (Throwable t) {
            throw new Error("Failed to get the 'Map<String, Capability<?>> providers' field from CapabilityManager.INSTANCE!", t);
        }

        CAP_ITEMS = getCapNonNull(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, IItemHandler.class);
        CAP_FLUIDS = getCapNonNull(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, IFluidHandler.class);

        CAP_ITEM_TRANSACTOR = registerCapability(IItemTransactor.class);
        CAP_ITEM_INJECTABLE = registerCapability(IInjectable.class);
    }

    @Nonnull
    private static <T> Capability<T> getCapNonNull(Capability<T> cap, Class<T> clazz) {
        if (cap == null) {
            throw new NullPointerException("The capability " + clazz + " was null!");
        }
        return cap;
    }

    @Nonnull
    public static <T> Capability<T> registerCapability(Class<T> clazz) {
        // By default storing and creating are illegal operations, as we don't necessarily have good default impl's
        IStorage<T> ourStorage = new IStorage<T>() {
            @Override
            public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
                throw new IllegalStateException("You must provide your own implementations of " + clazz);
            }

            @Override
            public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
                throw new IllegalStateException("You must provide your own implementations of " + clazz);
            }
        };
        CapabilityManager.INSTANCE.register(clazz, ourStorage, () -> {
            throw new IllegalStateException("You must provide your own instances of " + clazz);
        });
        // Whenever forge makes "registerCapability" return a non-null capability then we can remove this
        Capability<?> cap = INTERNAL_CAP_MAP.get(clazz.getName().intern());
        if (cap == null) {
            throw new IllegalStateException("The capability " + clazz.getName() + " was not found in the capability map " + INTERNAL_CAP_MAP);
        }
        /* check to make sure that the storage implementations are the same: this guarantees that the generic type of
         * the returned capability is of type T. */
        IStorage<?> store = cap.getStorage();
        if (store != ourStorage) {
            String error = "The capability got used a different storage implementation than what we had!"//
                + " (Got = " + cap.getName() + ", expected " + clazz + ")";
            throw new IllegalStateException(error);
        }
        // at this point we know its ours
        @SuppressWarnings("unchecked")
        Capability<T> ourCap = (Capability<T>) cap;
        return ourCap;
    }
}
