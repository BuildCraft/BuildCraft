package buildcraft.transport.plug;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.transport.neptune.IPluggableRegistry;
import buildcraft.api.transport.neptune.PluggableDefinition;

public enum PluggableRegistry implements IPluggableRegistry {
    INSTANCE;

    private final Map<ResourceLocation, PluggableDefinition> registered = new HashMap<>();

    @Override
    public void registerPluggable(PluggableDefinition definition) {
        registered.put(definition.identifier, definition);
    }

    @Override
    public PluggableDefinition getDefinition(ResourceLocation identifier) {
        return registered.get(identifier);
    }
}
