package buildcraft.transport.wire;

import net.minecraft.item.EnumDyeColor;

public interface IWireEmitter {
    /** Checks to see if this wire emitter is currently emitting the given colour. Only used to check if a given emitter
     * is still active. */
    boolean isEmitting(EnumDyeColor colour);

    /** Emits the given wire colour this tick. */
    void emitWire(EnumDyeColor colour);
}
