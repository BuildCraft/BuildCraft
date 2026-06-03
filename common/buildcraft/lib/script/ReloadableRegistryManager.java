// STUB(R.Chen): Forge MinecraftForge event bus / IReloadableRegistryManager deferred
package buildcraft.lib.script;

import buildcraft.api.registry.IReloadableRegistryManager;
import buildcraft.api.registry.IReloadableRegistry;
import buildcraft.api.registry.IScriptableRegistry;

public enum ReloadableRegistryManager implements IReloadableRegistryManager {
    DATA_PACKS,
    RESOURCE_PACKS;

    public static void loadAll() {}

    @Override
    public <T> IReloadableRegistry<T> createRegistry(String id, Class<T> type, IScriptableRegistry.EntryDeserialiser<T> deserialiser) {
        return null;
    }

    @Override
    public <T> IReloadableRegistry<T> getRegistry(String id, Class<T> type) {
        return null;
    }

    public void reloadAll() {}
}
