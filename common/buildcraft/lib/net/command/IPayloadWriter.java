package buildcraft.lib.net.command;

import buildcraft.lib.net.PacketBufferBC;

@FunctionalInterface
public interface IPayloadWriter {
    void write(PacketBufferBC buffer);
}
