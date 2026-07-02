package ct.buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;

/** Fired whenever a connection change is picked up by an {@link IPipe}. This even doesn't include the new value
 * (boolean isConnected) as it can be accessed via {@link IPipe#isConnected(Direction)}. */
public class PipeEventConnectionChange extends PipeEvent {

    public final Direction direction;

    public PipeEventConnectionChange(IPipeHolder holder, Direction direction) {
        super(holder);
        this.direction = direction;
    }
}
