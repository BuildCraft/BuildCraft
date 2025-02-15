/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

/** Special {@link FriendlyByteBuf} class that provides methods specific to "offset" reading and writing - like writing a
 * single bit to the stream, and auto-compacting it with similar bits into a single byte. */
public class PacketBufferBC extends FriendlyByteBuf {

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

    public static PacketBufferBC write(IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        writer.write(buffer);
        return buffer;
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

    void writePartialBitsBegin() {
        if (writePartialIndex == -1 || writePartialOffset == 8) {
            writePartialIndex = writerIndex();
            writePartialOffset = 0;
            writePartialCache = 0;
            writeByte(0);
        }
    }

    void readPartialBitsBegin() {
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
        int toWrite = (flag ? 1 : 0) << writePartialOffset;
        writePartialCache |= toWrite;
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
            int availableBits = 8 - writePartialOffset;

            if (availableBits >= length) {
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

                int mask = (1 << availableBits) - 1;

                int shift = length - availableBits;

                int bitsToWrite = (value >>> shift) & mask;

                writePartialCache |= bitsToWrite << writePartialOffset;
                setByte(writePartialIndex, writePartialCache);

                // we finished a byte, reset values so that the next write will reset and create a new byte
                writePartialCache = 0;
                writePartialOffset = 8;

                // now shift the value down ready for the next iteration
                length -= availableBits;
            }
        }

        while (length >= 8) {
            // write out full 8 bit chunks of the length until we reach 0
            writePartialBitsBegin();

            int byteToWrite = (value >>> (length - 8)) & 0xFF;

            setByte(writePartialIndex, byteToWrite);

            // we finished a byte, reset values so that the next write will reset and create a new byte
            writePartialCache = 0;
            writePartialOffset = 8;

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

        int value = 0;

        if (readPartialOffset > 0) {
            // If we have bits left at the top of the buffer...
            int availableBits = 8 - readPartialOffset;
            if (availableBits >= length) {
                // If the wanted bits are completely contained within the cache
                int mask = (1 << length) - 1;
                value = (readPartialCache >>> readPartialOffset) & mask;
                readPartialOffset += length;
                return value;
            } else {
                // If we need to read more bits than are available in the cache
                int bitsRead = readPartialCache >>> readPartialOffset;

                value = bitsRead;

                // We finished reading a byte, reset values so the next step will read them properly

                readPartialCache = 0;
                readPartialOffset = 8;

                length -= availableBits;
            }
        }

        while (length >= 8) {
            readPartialBitsBegin();
            length -= 8;
            value <<= 8;
            value |= readPartialCache;
            readPartialOffset = 8;
        }

        if (length > 0) {
            readPartialBitsBegin();

            int mask = (1 << length) - 1;

            value <<= length;
            value |= readPartialCache & mask;
            readPartialOffset = length;
        }

        return value;
    }

    @Override
    public PacketBufferBC writeEnum(Enum<?> value) {
        Enum<?>[] possible = value.getDeclaringClass().getEnumConstants();
        if (possible == null) throw new IllegalArgumentException("Not an enum " + value.getClass());
        if (possible.length == 0)
            throw new IllegalArgumentException("Tried to write an enum value without any values! How did you do this?");
        if (possible.length == 1) return this;
//        writeFixedBits(value.ordinal(), MathHelper.log2DeBruijn(possible.length));
        writeFixedBits(value.ordinal(), Mth.ceillog2(possible.length));
        return this;
    }

    @Override
    public <E extends Enum<E>> E readEnum(Class<E> enumClass) {
        // No need to lookup the declaring class as you cannot refer to sub-classes of Enum.
        E[] enums = enumClass.getEnumConstants();
        if (enums == null) throw new IllegalArgumentException("Not an enum " + enumClass);
        if (enums.length == 0)
            throw new IllegalArgumentException("Tried to read an enum value without any values! How did you do this?");
        if (enums.length == 1) return enums[0];
//        int length = MathHelper.log2DeBruijn(enums.length);
        int length = Mth.ceillog2(enums.length);
        int index = readFixedBits(length);
        return enums[index];
    }

    /** Reads string of any possible length */
    public String readString() {
        int length = readVarInt();
        byte[] array = new byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = readByte();
        }
        return new String(array, Charsets.UTF_8);
    }
}
