package buildcraft.lib.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.BCLibProxy;

public class RoamingConfigManager extends StreamConfigManager {
    private static final Map<ResourceLocation, RoamingConfigManager> instances = new HashMap<>();
    private final ResourceLocation identifier;
    private Boolean cacheExists = null;

    public static RoamingConfigManager getOrCreateDefault(ResourceLocation identifier) {
        if (!instances.containsKey(identifier)) {
            instances.put(identifier, new RoamingConfigManager(identifier));
        }
        return instances.get(identifier);
    }

    public RoamingConfigManager(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    @Override
    protected void read() {
        cacheExists = null;
        try (InputStream stream = BCLibProxy.getProxy().getStreamForIdentifier(identifier)) {
            read(stream);
            cacheExists = Boolean.TRUE;
        } catch (IOException io) {
            // ignore the error, the stream probably didn't exist
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
