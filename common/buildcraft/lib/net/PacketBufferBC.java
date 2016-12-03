package buildcraft.lib.net;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;

import io.netty.buffer.ByteBuf;

/** Special {@link PacketBuffer} class that provides methods specific to */
public class PacketBufferBC extends PacketBuffer {

    // Byte-based flag access
    private int readPartialOffset = 8;// so it resets down to 0 and reads a byte on read
    private int readPartialCache;

    /** The byte position that is currently being written to. -1 means that no bytes have been written to yet. */
    private int writePartialIndex = -1;
    /** The current bit based offset, used to add successive flags into the cached value held in
     * {@link #writePartialCache} */
    private int writePartialOffset;
    /** Holds the current set of flags that will be written out. This only saves having a read */
    private int writePartialCache;

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
        readPartialOffset = 8;
        readPartialCache = 0;
        writePartialIndex = -1;
        writePartialOffset = 0;
        writePartialCache = 0;
        return this;
    }

    private void writePartialBitsBegin() {
        if (writePartialIndex == -1 || writePartialOffset == 8) {
            writePartialIndex = writerIndex();
            writePartialOffset = 0;
            writePartialCache = 0;
            writeByte(0);
        }
    }

    private void readPartialBitsBegin() {
        if (readPartialOffset == 8) {
            readPartialOffset = 0;
            readPartialCache = readUnsignedByte();
        }
    }

    /** Writes a single boolean out to some position in this buffer. The boolean flag might be written to a new byte
     * (increasing the reader index) or it might be added to an existing byte that was written with a previous call to
     * this method. */
    @Override
    public PacketBufferBC writeBoolean(boolean flag) {
        writePartialBitsBegin();
        writePartialCache |= (flag ? 1 : 0) << writePartialOffset;
        writePartialOffset++;
        setByte(writePartialIndex, writePartialCache);
        return this;
    }

    /** Reads a single boolean from some position in this buffer. The boolean flag might be read from a new byte
     * (increasing the writerIndex) or it might be read from a previous byte that was read with a previous call to this
     * method. */
    @Override
    public boolean readBoolean() {
        readPartialBitsBegin();
        int offset = 1 << readPartialOffset++;
        return (readPartialCache & offset) == offset;
    }

    /** Writes a fixed number of bits
     * 
     * @param from
     * @param length
     * @return */
    public PacketBufferBC writeFixedBits(int value, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Tried to write too few bits! (" + length + ")");
        }
        if (length > 32) {
            throw new IllegalArgumentException("Tried to write more bits than are in an integer! (" + length + ")");
        }
        writePartialBitsBegin();
        int index = 0;
        // write out the toppermost byte, if the written index is
        if (writePartialIndex >= 0) {
            if (length - writePartialIndex <= 8) {
                // write it all out
            } else {

                // we're done
            }
        }

        throw new AbstractMethodError("// TODO: Implement this!");
    }

    /** @param length
     * @return */
    public int readFixedBits(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Tried to read too few bits! (" + length + ")");
        }
        if (length > 32) {
            throw new IllegalArgumentException("Tried to read more bits than are in an integer! (" + length + ")");
        }
        readPartialBitsBegin();
        int total = 0;

        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public PacketBufferBC writeEnumValue(Enum<?> value) {
        if (value == null) throw new NullPointerException("value");
        Enum<?>[] possible = value.getClass().getEnumConstants();
        writeFixedBits(value.ordinal(), MathHelper.smallestEncompassingPowerOfTwo(possible.length));
        return this;
    }

    @Override
    public <T extends Enum<T>> T readEnumValue(Class<T> enumClass) {
        T[] enums = enumClass.getEnumConstants();
        int length = MathHelper.smallestEncompassingPowerOfTwo(enums.length);
        int index = readFixedBits(length);
        return enums[index];
    }
}
