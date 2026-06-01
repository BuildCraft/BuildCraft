// STUB(R.Chen): Forge IMessage — compile shim.
package net.minecraftforge.fml.common.network.simpleimpl;

import net.minecraft.network.PacketByteBuf;

public interface IMessage {
    /** Default no-op: BC messages use PacketBufferBC overloads. */
    default void fromBytes(PacketByteBuf buf) {}
    /** Default no-op: BC messages use PacketBufferBC overloads. */
    default void toBytes(PacketByteBuf buf) {}
}
