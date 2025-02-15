package buildcraft.lib.script;

import buildcraft.api.registry.IReloadableRegistry;
import buildcraft.api.registry.IReloadableRegistryManager;
import com.google.common.collect.Iterables;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class SimpleReloadableRegistry<E> implements IReloadableRegistry<E> {
    public final IReloadableRegistryManager manager;
    public final List<E> permanent = new ArrayList<>();
    public final Map<ResourceLocation, E> reloadable = new HashMap<>();

    public SimpleReloadableRegistry(IReloadableRegistryManager manager) {
        this.manager = manager;
    }

    @Override
    public IReloadableRegistryManager getManager() {
        return manager;
    }

    @Override
    public <T extends E> T addPermanent(T recipe) {
        if (manager.isInReload()) {
            throw new IllegalStateException(
                    "Don't add permanent recipes during reload events! (Register them once literally any other time)");
        }
        permanent.add(recipe);
        return recipe;
    }

    @Override
    public Collection<E> getPermanent() {
        return permanent;
    }

    @Override
    public Map<ResourceLocation, E> getReloadableEntryMap() {
        return reloadable;
    }

    @Override
    public Iterable<E> getAllEntries() {
        return Iterables.concat(getPermanent(), getReloadableEntryMap().values());
    }

    /** Finds the first recipe that matches the given {@link Predicate} filter. This first searches in
     * {@link #getReloadableEntryMap()} and then {@link #getPermanent()} to allow overriding.
     *
     * @return The first matching recipe, or null if one wasn't found. */
    @Nullable
    public E getFirstMatch(Predicate<E> filter) {
        for (E recipe : reloadable.values()) {
            if (filter.test(recipe)) {
                return recipe;
            }
        }
        for (E recipe : permanent) {
            if (filter.test(recipe)) {
                return recipe;
            }
        }
        return null;
    }
}
