package buildcraft.transport.plug;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import buildcraft.transport.api_move.IPluggableRegistry;
import buildcraft.transport.api_move.PluggableDefinition;

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
