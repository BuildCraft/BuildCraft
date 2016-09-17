package buildcraft.transport.api_move;

public interface IPipeRegistry {
    IPipeItem registerPipeAndItem(PipeDefinition definition);

    void registerPipe(PipeDefinition definition);
}
