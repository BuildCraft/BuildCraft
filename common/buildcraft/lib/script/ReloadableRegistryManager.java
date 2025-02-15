package buildcraft.lib.script;

import buildcraft.api.registry.*;
import buildcraft.api.registry.IReloadableRegistry.PackType;
import buildcraft.lib.misc.JsonUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

public enum ReloadableRegistryManager implements IReloadableRegistryManager {
    DATA_PACKS(PackType.DATA_PACK),
    RESOURCE_PACKS(PackType.RESOURCE_PACK);

    private static boolean isLoadingAll;

    private final PackType sourceType;
    private final BiMap<String, IReloadableRegistry<?>> registries = HashBiMap.create();
    private boolean isReloading;
    private int reloadCount = 0;

    ReloadableRegistryManager(PackType sourceType) {
        this.sourceType = sourceType;
    }

    public static void loadAll() {
        try {
            isLoadingAll = true;

            DATA_PACKS.reloadAll();
            if (BuildCraftRegistryManager.managerResourcePacks != null) {
                RESOURCE_PACKS.reloadAll();
            }

        } finally {
            isLoadingAll = false;
        }
    }

    @Override
    public PackType getType() {
        return sourceType;
    }

    @Override
    public boolean isLoadingAll() {
        return isLoadingAll;
    }

    /** Reloads every registry. Generally calling this is a very bad idea if you don't also call other reload events. */
    public void reloadAll() {
        reload(new HashSet<>(registries.values()));
    }

    @Override
    public void reload(IReloadableRegistry<?> registry) {
        reload(Collections.singleton(registry));
    }

    @Override
    public void reload(IReloadableRegistry<?>... all) {
        reload(new HashSet<>(Arrays.asList(all)));
    }

    @Override
    public void reload(Set<IReloadableRegistry<?>> set) {
        if (isInReload()) {
            throw new IllegalStateException("Cannot reload while we are reloading!");
        }
        try {
            isReloading = true;
            // Calen: unlock the EVENT_BUS
            MinecraftForge.EVENT_BUS.start();
            MinecraftForge.EVENT_BUS.post(new EventBuildCraftReload.BeforeClear(this, set));
            set.forEach(registry -> registry.getReloadableEntryMap().clear());

            MinecraftForge.EVENT_BUS.post(new EventBuildCraftReload.PreLoad(this, set));

            GsonBuilder builder = new GsonBuilder();
            // register our own types here, so that others can replace them
            JsonUtil.registerTypeAdaptors(builder);

            MinecraftForge.EVENT_BUS.post(new EventBuildCraftReload.PopulateGson(this, set, builder));
            Gson gson = builder.create();

            for (IReloadableRegistry<?> registry : set) {
                if (registry instanceof ScriptableRegistry<?>) {
                    ((ScriptableRegistry<?>) registry).loadScripts(gson);
                }
            }

            MinecraftForge.EVENT_BUS.post(new EventBuildCraftReload.PostLoad(this, set));
        } finally {
            reloadCount++;
            isReloading = false;
        }
        MinecraftForge.EVENT_BUS.post(new EventBuildCraftReload.FinishLoad(this, set));
    }

    @Override
    public boolean isInReload() {
        return isReloading;
    }

    @Override
    public int getReloadCount() {
        return reloadCount;
    }

    @Override
    public Map<String, IReloadableRegistry<?>> getAllRegistries() {
        return registries;
    }

    @Override
    public <R> IReloadableRegistry<R> createRegistry(String name) {
        SimpleReloadableRegistry<R> registry = new SimpleReloadableRegistry<>(this);
        getAllRegistries().put(name, registry);
        return registry;
    }

    @Override
    public <R> IScriptableRegistry<R> createScriptableRegistry(String entryPath) {
        ScriptableRegistry<R> registry = new ScriptableRegistry<>(this, entryPath);
        registerRegistry(registry);
        return registry;
    }

    @Override
    public void registerRegistry(String entryType, IScriptableRegistry<?> registry) {
        if (entryType.indexOf(':') != -1) {
            throw new IllegalArgumentException(
                    "The entry type must be a valid resource path! (so it must not contain a colon)");
        }
        registries.put(entryType, registry);
    }
}
