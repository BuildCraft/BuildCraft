// STUB(R.Chen): 1.12 IReloadableResourceManager — compile shim.
package net.minecraft.client.resources;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceManagerReloadListener;

public interface IReloadableResourceManager extends ResourceManager {
    void registerReloadListener(ResourceManagerReloadListener listener);
}
