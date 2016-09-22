package buildcraft.transport.api_move;

import net.minecraft.util.ResourceLocation;

public interface IPluggableRegistry {
    PluggableDefinition getDefinition(ResourceLocation identifier);

    void registerPipe(PluggableDefinition definition);
}
