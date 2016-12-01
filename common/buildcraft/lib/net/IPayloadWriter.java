package buildcraft.lib.net;

@FunctionalInterface
public interface IPayloadWriter {
    void write(PacketBufferBC buffer);
}
