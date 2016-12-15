package buildcraft.lib.net;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;

import io.netty.buffer.ByteBuf;

/** Special {@link PacketBuffer} class that provides methods specific to "offset" reading and writing - like writing a
 * single bit to the stream, and auto-compacting it with simalir bits into a single byte. */
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
     * (increasing the writerIndex) or it might be added to an existing byte that was written with a previous call to
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
     * (increasing the readerIndex) or it might be read from a previous byte that was read with a previous call to this
     * method. */
    @Override
    public boolean readBoolean() {
        readPartialBitsBegin();
        int offset = 1 << readPartialOffset++;
        return (readPartialCache & offset) == offset;
    }

    /** Writes a fixed number of bits out to the stream.
     * 
     * @param value the value to write out.
     * @param length The number of bits to write.
     * @return This buffer.
     * @throws IllegalArgumentException if the length argument was less than 1 or greater than 32. */
    public PacketBufferBC writeFixedBits(int value, int length) throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("Tried to write too few bits! (" + length + ")");
        }
        if (length > 32) {
            throw new IllegalArgumentException("Tried to write more bits than are in an integer! (" + length + ")");
        }
        writePartialBitsBegin();

        // - length = 10
        // - bits = 0123456789

        // current
        // (# = already written, _ is not yet written)
        // - in buffer [######## _#######]
        // - writePartialCache = "_#######"
        // - writePartialOffset = 7

        // want we want:
        // - in buffer [######## 0###### 12345678 _______9 ]
        // - writePartialCache = "_______9"
        // - writePartialOffset = 1

        // first stage: take the toppermost bits and append them to the cache (if the cache contains bits)
        if (writePartialOffset > 0) {
            // top length = 8 - (num bits in cache) or length, whichever is SMALLER
            int availbleBits = 8 - writePartialOffset;

            if (availbleBits >= length) {
                int mask = (1 << length) - 1;
                int bitsToWrite = value & mask;

                writePartialCache |= bitsToWrite << writePartialOffset;
                setByte(writePartialIndex, writePartialCache);
                writePartialOffset += length;
                // we just wrote out the entire length, no need to do anything else.
                return this;
            } else { // topLength < length -- we will still need to be writing out more bits after this

                // length = 10
                // topLength = 1
                // value = __01 2345 6789
                // want == ____ ____ ___0
                // mask == ____ ____ ___1
                // shift back = 9

                int mask = (1 << availbleBits) - 1;

                int shiftBack = length - availbleBits;

                int bitsToWrite = (value >>> shiftBack) & mask;

                writePartialCache |= bitsToWrite << writePartialOffset;
                setByte(writePartialIndex, writePartialCache);

                // we finished a byte, reset values so that the next write will reset and create a new byte
                writePartialCache = 0;
                writePartialOffset = 8;

                // now shift the value down ready for the next iteration
                value >>>= shiftBack;
                length -= shiftBack;
            }
        }

        while (length >= 8) {
            // write out full 8 bit chunks of the length until we reach 0
            writePartialBitsBegin();

            int byteToWrite = (value >>> (length - 8)) & 0xFFFF;

            setByte(writePartialIndex, byteToWrite);

            // we finished a byte, reset values so that the next write will reset and create a new byte
            writePartialCache = 0;
            writePartialOffset = 8;

            value >>>= 8;
            length -= 8;
        }

        if (length > 0) {
            // we have a few bits left over to append
            writePartialBitsBegin();

            int mask = (1 << length) - 1;
            writePartialCache = value & mask;
            setByte(writePartialIndex, writePartialCache);
            writePartialOffset = length;
        }

        return this;
    }

    /** @param length
     * @return The read bits, compacted into an int.
     * @throws IllegalArgumentException if the length argument was less than 1 or greater than 32. */
    public int readFixedBits(int length) throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("Tried to read too few bits! (" + length + ")");
        }
        if (length > 32) {
            throw new IllegalArgumentException("Tried to read more bits than are in an integer! (" + length + ")");
        }
        readPartialBitsBegin();

        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public PacketBufferBC writeEnumValue(Enum<?> value) {
        if (value == null) {
            writeBoolean(false);
        } else {
            writeBoolean(true);
            Enum<?>[] possible = value.getClass().getEnumConstants();
            writeFixedBits(value.ordinal(), MathHelper.smallestEncompassingPowerOfTwo(possible.length));
        }
        return this;
    }

    @Override
    public <T extends Enum<T>> T readEnumValue(Class<T> enumClass) {
        boolean exists = readBoolean();
        if (!exists) {
            return null;
        }

        T[] enums = enumClass.getEnumConstants();
        int length = MathHelper.smallestEncompassingPowerOfTwo(enums.length);
        int index = readFixedBits(length);
        return enums[index];
    }

    // TODO: Fixed bits!
}
