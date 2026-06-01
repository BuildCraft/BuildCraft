// STUB(R.Chen): Forge CapabilityManager — compile shim.
package net.minecraftforge.common.capabilities;

import java.util.concurrent.Callable;

public class CapabilityManager {
    public static final CapabilityManager INSTANCE = new CapabilityManager();

    public <T> void register(Class<T> type, Capability.IStorage<T> storage, Callable<? extends T> factory) {
        // no-op — capability registration is handled by Fabric
    }
}
