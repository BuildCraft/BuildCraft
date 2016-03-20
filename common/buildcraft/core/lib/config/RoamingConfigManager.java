package buildcraft.core.lib.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

import net.minecraft.util.ResourceLocation;

import buildcraft.core.proxy.CoreProxy;

public class RoamingConfigManager extends StreamConfigManager {
    private static final Map<ResourceLocation, RoamingConfigManager> instances = new HashMap<>();
    private final ResourceLocation location;
    private Boolean cacheExists = null;

    public static RoamingConfigManager getOrCreateDefault(ResourceLocation location) {
        if (!instances.containsKey(location)) {
            instances.put(location, new RoamingConfigManager(location));
        }
        return instances.get(location);
    }

    public RoamingConfigManager(ResourceLocation location) {
        this.location = location;
    }

    @Override
    protected void read() {
        cacheExists = null;
        InputStream stream = CoreProxy.proxy.getStreamForResource(location);
        if (stream != null) {
            try {
                read(stream);
                cacheExists = Boolean.TRUE;
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        if (cacheExists == null) cacheExists = Boolean.FALSE;
    }

    public boolean exists() {
        if (cacheExists == null) read();
        return cacheExists.booleanValue();
    }

    @Override
    protected void write() {}

    @Override
    protected String comment() {
        return null;
    }
}
