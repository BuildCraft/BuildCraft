/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.test.lib.net;

import java.io.PrintStream;

import io.netty.buffer.ByteBuf;

import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Bootstrap;

public final class PrintingByteBuf extends PacketBufferBC
{
//    private static final PrintStream SYSOUT = Bootstrap.SYSOUT;
    private static final PrintStream SYSOUT = Bootstrap.STDOUT;

    public PrintingByteBuf(ByteBuf wrapped)
    {
        super(wrapped);
        // this.wrapped = PacketBufferBC.asPacketBufferBc(wrapped);
    }

    @Override
    public ByteBuf writeByte(int val)
    {
        SYSOUT.print(padLength(2, val));
        super.writeByte(val);
        return this;
    }

    @Override
    public ByteBuf writeBytes(byte[] val)
    {
        for (byte b : val)
        {
            writeByte(Byte.toUnsignedInt(b));
        }
        return this;
    }

    @Override
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
    {
        for (int i = 0; i < length; i++)
        {
            writeByte(src[i + srcIndex]);
        }
        return this;
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src)
    {
        writeBytes(src, src.readableBytes());
        return this;
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int length)
    {
        for (int i = 0; i < length; i++)
        {
            writeByte(src.readByte());
        }
        return this;
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
    {
        for (int i = 0; i < length; i++)
        {
            writeByte(src.getByte(i + srcIndex));
        }
        return this;
    }

    @Override
//    public PacketBuffer writeVarInt(int value)
    public PacketBuffer writeVarInt(int value)
    {
        SYSOUT.print(" _var[" + value + "] (");
        super.writeVarInt(value);
        SYSOUT.print(" )");
        return this;
    }

    @Override
//    public PacketBuffer writeVarLong(long value)
    public PacketBuffer writeVarLong(long value)
    {
        SYSOUT.print(" _var[" + value + "L](");
        super.writeVarLong(value);
        SYSOUT.print(" )");
        return this;
    }

    @Override
    public ByteBuf writeShort(int val)
    {
        SYSOUT.print(padLength(4, val));
        super.writeShort(val);
        return this;
    }

    @Override
    public ByteBuf writeInt(int val)
    {
        SYSOUT.print(padLength(8, val));
        super.writeInt(val);
        return this;
    }

    @Override
    public ByteBuf writeLong(long val)
    {
        SYSOUT.print(padLength(16, val));
        super.writeLong(val);
        return this;
    }

    @Override
    public ByteBuf writeFloat(float val)
    {
        SYSOUT.print(padLength(8, Float.floatToRawIntBits(val)));
        super.writeFloat(val);
        return this;
    }

    @Override
    public ByteBuf writeDouble(double val)
    {
        SYSOUT.print(padLength(16, Double.doubleToRawLongBits(val)));
        super.writeDouble(val);
        return this;
    }

    @Override
    public ByteBuf setByte(int index, int value)
    {
        SYSOUT.println("\n  Set " + index + " (" + new String(padLength(2, getByte(index)))//
                + " ) to" + new String(padLength(2, value)));
        super.setByte(index, value);
        return this;
    }

    private static char[] padLength(int length, long val)
    {
        String s = Long.toUnsignedString(val, 16);
        if (s.length() > length)
        {
            s = s.substring(s.length() - length);
        }
        char[] chars = new char[length + 1];
        chars[0] = ' ';
        int diff = length - s.length();
        for (int i = 0; i < diff; i++)
        {
            chars[i + 1] = '0';
        }
        for (int i = 0; i < s.length(); i++)
        {
            chars[i + diff + 1] = s.charAt(i);
        }
        return chars;
    }

    // PacketBufferBC overrides

    @Override
    public PacketBufferBC writeFixedBits(int value, int length) throws IllegalArgumentException
    {
        SYSOUT.println("Writing " + length + " fixed bits ( " + new String(padLength(length, value)) + " )");
        super.writeFixedBits(value, length);
        return this;
    }

    @Override
//    public PacketBufferBC writeEnumValue(Enum<?> value)
    public PacketBufferBC writeEnum(Enum<?> value)
    {
        SYSOUT.println("Writing " + value + " from " + value.getClass());
//        super.writeEnumValue(value);
        super.writeEnum(value);
        return this;
    }
}
