package buildcraft.lib.net.command;

import net.minecraft.network.PacketBuffer;

@FunctionalInterface
public interface IPayloadWriter {
    void write(PacketBuffer buffer);
}