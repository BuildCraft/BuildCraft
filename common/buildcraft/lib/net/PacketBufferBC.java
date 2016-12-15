package buildcraft.lib.net;

import net.minecraft.network.PacketBuffer;

import io.netty.buffer.ByteBuf;

/** Special {@link PacketBuffer} class that provides methods specific to */
public class PacketBufferBC extends PacketBuffer {

    // Byte-based flag access
    private int readFlagOffset = 8;// so it resets down to 0 and reads a byte on read
    private int readFlagCache;

    /** The byte position that is currently being written to. -1 means that no bytes have been written to yet. */
    private int writeFlagIndex = -1;
    /** The current bit based offset, used to add successive flags into the cached value held in
     * {@link #writeFlagCache} */
    private int writeFlagOffset;
    /** Holds the current set of flags that will be written out. This only saves having a read */
    private int writeFlagCache;

    public PacketBufferBC(ByteBuf wrapped) {
        super(wrapped);
    }

    /** Returns the given {@link ByteBuf} as {@link PacketBufferBC}. if the given instance is already a
     * {@link PacketBufferBC} then the given buffer is returned (note that this may result in unexpected consequences if
     * multiple read/write Boolean methods are called on the given buffer before you called this). */
    public static PacketBufferBC asPacketBufferBc(ByteBuf buf) {
        if (buf instanceof PacketBufferBC) {
            return (PacketBufferBC) buf;
        } else {
            return new PacketBufferBC(buf);
        }
    }

    @Override
    public PacketBufferBC clear() {
        super.clear();
        readFlagOffset = 8;
        readFlagCache = 0;
        writeFlagIndex = -1;
        writeFlagOffset = 0;
        writeFlagCache = 0;
        return this;
    }

    /** Writes a single boolean out to some position in this buffer. The boolean flag might be written to a new byte
     * (increasing the writerIndex) or it might be added to an existing byte that was written with a previous call to
     * this method. */
    @Override
    public PacketBufferBC writeBoolean(boolean flag) {
        if (writeFlagIndex == -1 || writeFlagOffset == 8) {
            writeFlagIndex = writerIndex();
            writeFlagOffset = 0;
            writeFlagCache = 0;
            writeByte(0);
        }
        writeFlagCache |= (flag ? 1 : 0) << writeFlagOffset;
        writeFlagOffset++;
        setByte(writeFlagIndex, writeFlagCache);
        return this;
    }

    /** Reads a single boolean from some position in this buffer. The boolean flag might be read from a new byte
     * (increasing the readerIndex) or it might be read from a previous byte that was read with a previous call to this
     * method. */
    @Override
    public boolean readBoolean() {
        if (readFlagOffset == 8) {
            readFlagOffset = 0;
            readFlagCache = readUnsignedByte();
        }
        int offset = 1 << readFlagOffset++;
        return (readFlagCache & offset) == offset;
    }

    // TODO: Fixed bits!

    @Override
    public PacketBufferBC writeEnumValue(Enum<?> value) {
        super.writeEnumValue(value);
        return this;
    }

    @Override
    public <T extends Enum<T>> T readEnumValue(Class<T> enumClass) {
        return super.readEnumValue(enumClass);
    }
}
