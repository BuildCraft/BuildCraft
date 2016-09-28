package buildcraft.transport.api_move;

import net.minecraft.util.ResourceLocation;

public interface IPipeRegistry {
    PipeDefinition getDefinition(ResourceLocation identifier);

    IPipeItem registerPipeAndItem(PipeDefinition definition);

    void registerPipe(PipeDefinition definition);

    void setItemForPipe(PipeDefinition definition, IPipeItem item);

    IPipeItem getItemForPipe(PipeDefinition definition);

    Iterable<PipeDefinition> getAllRegisteredPipes();
}
