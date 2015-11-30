package buildcraft.transport.api.impl.event;

import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEvent_BC8;

public abstract class PipeEvent_BC8 implements IPipeEvent_BC8 {
    private final IPipe_BC8 pipe;

    public PipeEvent_BC8(IPipe_BC8 pipe) {
        this.pipe = pipe;
    }

    @Override
    public IPipe_BC8 getPipe() {
        return pipe;
    }

    public static abstract class Tick extends PipeEvent_BC8 implements IPipeEvent_BC8.Tick {
        public Tick(IPipe_BC8 pipe) {
            super(pipe);
        }

        @Override
        public long getCurrentTick() {
            return getPipe().getWorld().getTotalWorldTime();
        }

        public static class Client extends PipeEvent_BC8.Tick implements IPipeEvent_BC8.Tick.Client {
            public Client(IPipe_BC8 pipe) {
                super(pipe);
            }
        }

        public static class Server extends PipeEvent_BC8.Tick implements IPipeEvent_BC8.Tick.Server {
            public Server(IPipe_BC8 pipe) {
                super(pipe);
            }
        }
    }
}
