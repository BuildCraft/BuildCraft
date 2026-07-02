package ct.buildcraft.api.transport.pluggable;

import net.minecraft.resources.ResourceLocation;

public interface IPluggableRegistry {
    default void register(PluggableDefinition definition) {
        register(definition.identifier, definition);
    }

    void register(ResourceLocation identifier, PluggableDefinition definition);

    PluggableDefinition getDefinition(ResourceLocation identifier);
}
