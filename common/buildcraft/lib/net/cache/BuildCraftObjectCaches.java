package buildcraft.lib.net.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import buildcraft.api.core.BCLog;

import buildcraft.lib.misc.ItemStackKey;

/** Stores default caches for {@link ItemStack} and {@link FluidStack}. Note that because {@link ItemStack} doesn't
 * override {@link #hashCode()} or {@link #equals(Object)} {@link ItemStackKey} is used as the key type instead, so you
 * probably want to use {@link #storeItemStack(ItemStack)}, {@link #getItemStackId(ItemStack)} and
 * {@link #retrieveItemStack(int)} instead of {@link #CACHE_ITEMS} directly.
 * 
 * This also stores the */
public class BuildCraftObjectCaches {
    public static final NetworkedItemStackCache CACHE_ITEMS = new NetworkedItemStackCache();
    public static final NetworkedFluidStackCache CACHE_FLUIDS = new NetworkedFluidStackCache();

    static final List<NetworkedObjectCache<?>> CACHES = new ArrayList<>();

    public static void registerCache(NetworkedObjectCache<?> cache) {
        if (Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION)) {
            throw new IllegalStateException("May only construct a cache BEFORE post-init!");
        }
        BuildCraftObjectCaches.CACHES.add(cache);
    }

    /** @see NetworkedObjectCache.ServerView#store(Object) */
    public static int storeItemStack(ItemStack stack) {
        return CACHE_ITEMS.server().store(new ItemStackKey(stack));
    }

    /** @see NetworkedObjectCache.ServerView#getId(Object) */
    public static int getItemStackId(ItemStack stack) {
        return CACHE_ITEMS.server().getId(new ItemStackKey(stack));
    }

    /** @see NetworkedObjectCache.ClientView#retrieve(int) */
    public static Supplier<ItemStack> retrieveItemStack(int id) {
        NetworkedObjectCache<ItemStackKey>.Link link = CACHE_ITEMS.client().retrieve(id);
        return () -> link.get().baseStack;
    }

    /** Called by BuildCraftLib in the {@link FMLPreInitializationEvent} */
    public static void fmlPreInit() {
        registerCache(CACHE_ITEMS);
        registerCache(CACHE_FLUIDS);
    }

    /** Called by BuildCraftLib in the {@link FMLPostInitializationEvent} */
    public static void fmlPostInit() {
        CACHES.sort((a, b) -> {
            return a.getClass().getSimpleName().compareTo(b.getClass().getSimpleName());
        });
        if (NetworkedObjectCache.DEBUG_LOG) {
            BCLog.logger.info("[lib.net.cache] Sorted list of networked object caches:");
            for (int i = 0; i < CACHES.size(); i++) {
                final NetworkedObjectCache<?> cache = CACHES.get(i);
                BCLog.logger.info("  " + i + " = " + cache.getCacheName());
            }
            BCLog.logger.info("[lib.net.cache] Total of " + CACHES.size() + " caches");
        }
    }

    /** Called by BuildCraftLib on every client tick. */
    public static void onClientTick() {
        for (NetworkedObjectCache<?> cache : CACHES) {
            cache.onClientWorldTick();
        }
    }
}
