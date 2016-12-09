package buildcraft.lib.net.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

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

    /** @see NetworkedObjectCache.ServerView#store(Object) */
    public static int storeItemStack(ItemStack stack) {
        return CACHE_ITEMS.server().store(new ItemStackKey(stack));
    }

    /** @see NetworkedObjectCache.ServerView#getId(Object) */
    public static int getItemStackId(ItemStack stack) {
        return CACHE_ITEMS.server().getId(new ItemStackKey(stack));
    }

    /** @see NetworkedObjectCache.ClientView#retrieve(Object) */
    public static Supplier<ItemStack> retrieveItemStack(int id) {
        NetworkedObjectCache<ItemStackKey>.Link link = CACHE_ITEMS.client().retrieve(id);
        return () -> link.get().baseStack;
    }
}
