package ct.buildcraft.api.transport.pipe;

/** Fired when the state of a pipe's tile entity changes. Listen for subclasses, not this one! */
public abstract class PipeEventTileState extends PipeEvent {
    PipeEventTileState(IPipeHolder holder) {
        super(holder);
    }

    /** Fired in {@link EntityBlock#invalidate()} */
    public static class Invalidate extends PipeEventTileState {
        public Invalidate(IPipeHolder holder) {
            super(holder);
        }
    }

    /** Fired in {@link EntityBlock#validate()} */
    public static class Validate extends PipeEventTileState {
        public Validate(IPipeHolder holder) {
            super(holder);
        }
    }

    /** Fired in {@link EntityBlock#onChunkUnload()} */
    public static class ChunkUnload extends PipeEventTileState {
        public ChunkUnload(IPipeHolder holder) {
            super(holder);
        }
    }
}
