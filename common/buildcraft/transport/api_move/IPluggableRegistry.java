package buildcraft.transport.api_move;

import net.minecraft.util.ResourceLocation;

public interface IPluggableRegistry {
    void registerPluggable(PluggableDefinition definition);

    PluggableDefinition getDefinition(ResourceLocation identifier);
}
