package buildcraft.transport.pipes.bc8;

import buildcraft.api.transport.pipe_bc8.BCPipeEventHandler;
import buildcraft.api.transport.pipe_bc8.IPipeEvent_BC8;
import buildcraft.api.transport.pipe_bc8.IPipeTile_BC8;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.event.EventBusProviderASM;
import buildcraft.core.lib.event.IEventBus;
import buildcraft.core.lib.event.IEventBusProvider;

public class TilePipe_BC8 extends TileBuildCraft implements IPipeTile_BC8 {
    private static IEventBusProvider<IPipeEvent_BC8> eventBusProvider = new EventBusProviderASM<IPipeEvent_BC8>(IPipeEvent_BC8.class,
            BCPipeEventHandler.class);

    private IPipe_BC8 pipe;
    private final IEventBus<IPipeEvent_BC8> bus = eventBusProvider.newBus();

    public TilePipe_BC8() {

    }

    @Override
    public IPipe_BC8 getPipe() {
        return pipe;
    }
}
