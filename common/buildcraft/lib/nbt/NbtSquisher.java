/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import org.apache.commons.io.IOUtils;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.profiler.Profiler;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.data.NBTSquishConstants;

public class NbtSquisher {
    public static final Profiler profiler = new Profiler();
    /** Used by testing classes to replace ByteBuf instances with PrintingByteBuf -- but we don't have that
     * class in main because it makes checkstyle complain. */
    public static Function<ByteBuf, PacketBuffer> debugBuffer = null;

    private static final int TYPE_MC_GZIP = NBTSquishConstants.VANILLA_COMPRESSED;
    private static final int TYPE_MC = NBTSquishConstants.VANILLA;
    private static final int TYPE_BC_1_GZIP = NBTSquishConstants.BUILDCRAFT_V1_COMPRESSED;
    private static final int TYPE_BC_1 = NBTSquishConstants.BUILDCRAFT_V1;

    /*
     * Defines a compression program that can turn large, mostly-similar, dense, NBTTagCompounds into much smaller
     * variants.
     * 
     * Compression has the following steps:
     * 
     * - 1:
     */

    public static byte[] squish(NBTTagCompound nbt, int type) {
        ByteBuf buf = Unpooled.buffer();
        squish(nbt, type, buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public static void squish(NBTTagCompound nbt, int type, OutputStream stream) throws IOException {
        switch (type) {
            case TYPE_MC:
                squishVanillaUncompressed(nbt, new DataOutputStream(stream));
                return;
            case TYPE_MC_GZIP:
                squishVanilla(nbt, stream);
                return;
            case TYPE_BC_1:
                squishBuildCraftV1Uncompressed(nbt, stream);
                return;
            case TYPE_BC_1_GZIP:
                squishBuildCraftV1(nbt, stream);
                return;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    public static void squish(NBTTagCompound nbt, int type, ByteBuf buf) {
        switch (type) {
            case TYPE_MC:
                squishVanillaUncompressed(nbt, buf);
                return;
            case TYPE_MC_GZIP:
                squishVanilla(nbt, buf);
                return;
            case TYPE_BC_1:
                squishBuildCraftV1Uncompressed(nbt, buf);
                return;
            case TYPE_BC_1_GZIP:
                squishBuildCraftV1(nbt, buf);
                return;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    /*
     * Vanilla compressed
     */

    public static void squishVanilla(NBTTagCompound nbt, ByteBuf buf) {
        try {
            squishVanilla(nbt, new ByteBufOutputStream(buf));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write a safe NBTTagCompound!", e);
        }
    }

    public static byte[] squishVanilla(NBTTagCompound nbt) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            squishVanilla(nbt, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write a safe NBTTagCompound!", e);
        }
    }

    public static void squishVanilla(NBTTagCompound nbt, OutputStream to) throws IOException {
        to.write(TYPE_MC_GZIP);
        CompressedStreamTools.writeCompressed(nbt, to);
    }

    /*
     * Vanilla uncompressed
     */

    public static void squishVanillaUncompressed(NBTTagCompound nbt, ByteBuf buf) {
        squishVanillaUncompressed(nbt, new ByteBufOutputStream(buf));
    }

    public static byte[] squishVanillaUncompressed(NBTTagCompound nbt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        squishVanillaUncompressed(nbt, new DataOutputStream(baos));
        return baos.toByteArray();
    }

    public static void squishVanillaUncompressed(NBTTagCompound nbt, DataOutput to) {
        try {
            to.write(TYPE_MC);
            CompressedStreamTools.write(nbt, to);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write a safe NBTTagCompound!", e);
        }
    }

    /*
     * BuildCraft V1 compressed
     */

    public static byte[] squishBuildCraftV1(NBTTagCompound nbt) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            squishBuildCraftV1(nbt, baos);
            return baos.toByteArray();
        } catch (IOException io) {
            throw new RuntimeException("Failed to write to a perfectly good ByteArrayOutputStream!", io);
        }
    }

    public static void squishBuildCraftV1(NBTTagCompound nbt, ByteBuf buf) {
        try (ByteBufOutputStream bbos = new ByteBufOutputStream(buf)) {
            squishBuildCraftV1(nbt, bbos);
        } catch (IOException io) {
            throw new RuntimeException("Failed to write to a perfectly good ByteBufOutputStream!", io);
        }
    }

    public static void squishBuildCraftV1(NBTTagCompound nbt, OutputStream stream) throws IOException {
        stream.write(TYPE_BC_1_GZIP);
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        int len = squishBuildCraftV1Direct(nbt, buffer);
        new DataOutputStream(stream).writeInt(len);
        try (GZIPOutputStream def = new GZIPOutputStream(stream, true)) {
            writeBufToStream(buffer, def);
            def.close();
        }
    }

    /*
     * BuildCraft V1 uncompressed
     */

    public static byte[] squishBuildCraftV1Uncompressed(NBTTagCompound nbt) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            squishBuildCraftV1Uncompressed(nbt, baos);
            return baos.toByteArray();
        } catch (IOException io) {
            throw new RuntimeException("Failed to write to a perfectly good ByteArrayOutputStream!", io);
        }
    }

    public static void squishBuildCraftV1Uncompressed(NBTTagCompound nbt, OutputStream stream) throws IOException {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        squishBuildCraftV1Uncompressed(nbt, buf);
        writeBufToStream(buf, stream);
    }

    public static void squishBuildCraftV1Uncompressed(NBTTagCompound nbt, ByteBuf buf) {
        buf.writeByte(TYPE_BC_1);
        int idx = buf.writerIndex();
        buf.writeInt(0);// length
        int len = squishBuildCraftV1Direct(nbt, new PacketBuffer(buf));
        buf.setInt(idx, len);
    }

    /*
     * Expanding
     */

    public static NBTTagCompound expand(byte[] bytes) throws IOException {
        return expand(new ByteArrayInputStream(bytes));
    }

    public static NBTTagCompound expand(ByteBuf buf) throws IOException {
        return expand(new ByteBufInputStream(buf));
    }

    public static NBTTagCompound expand(InputStream stream) throws IOException {
        int type = stream.read();
        switch (type) {
            case TYPE_MC: {
                return CompressedStreamTools.read(new DataInputStream(stream));
            }
            case TYPE_MC_GZIP: {
                return CompressedStreamTools.readCompressed(stream);
            }
            case TYPE_BC_1_GZIP:
                int len = new DataInputStream(stream).readInt();
                return readBuildCraftV1Direct(new GZIPInputStream(stream), len);
            case TYPE_BC_1: {
                len = new DataInputStream(stream).readInt();
                return readBuildCraftV1Direct(stream, len);
            }
            case GZIPInputStream.GZIP_MAGIC >> 8: {
                // Looks like we're reading an older style of stream
                int rest = stream.read();
                if (rest == (GZIPInputStream.GZIP_MAGIC & 0xff)) {
                    // We exactly match the GZIP header-- try reading it as if it was one
                    final int[] read = { 0 };
                    InputStream s2 = new InputStream() {
                        @Override
                        public int read() throws IOException {
                            int r = read[0];
                            switch (r) {
                                case 0: {
                                    read[0] = 1;
                                    return type;
                                }
                                case 1: {
                                    read[0] = 2;
                                    return rest;
                                }
                                default: {
                                    return stream.read();
                                }
                            }
                        }
                    };
                    return CompressedStreamTools.readCompressed(s2);
                } else {
                    throw new InvalidInputDataException("Unknown type " + type + ", and not a gzip stream (2nd = "
                        + rest + ")");
                }
            }
            default: {
                throw new InvalidInputDataException("Unknown type " + type);
            }
        }
    }

    /*
     * Util
     */

    private static NBTTagCompound readBuildCraftV1Direct(InputStream stream, int length) throws IOException {
        byte[] bytes = new byte[length];
        IOUtils.read(stream, bytes);
        PacketBuffer buf = new PacketBuffer(Unpooled.wrappedBuffer(bytes));
        try {
            NBTSquishMap map = NBTSquishMapReader.read(buf);
            WrittenType width = map.getWrittenType();
            int index = width.readIndex(buf);
            return map.getFullyReadComp(index);
        } catch (IndexOutOfBoundsException ioobe) {
            throw new IOException("The byte buf was not big enough!", ioobe);
        }
    }

    private static int squishBuildCraftV1Direct(NBTTagCompound nbt, PacketBuffer buf) {
        buf = createBuffer(buf);
        int start = buf.writerIndex();
        NBTSquishMap map = new NBTSquishMap();
        map.addTag(nbt);
        NBTSquishMapWriter.debug = debugBuffer != null;
        NBTSquishMapWriter.write(map, buf);
        WrittenType type = map.getWrittenType();
        type.writeIndex(buf, map.indexOfTag(nbt));
        return buf.writerIndex() - start;
    }

    private static PacketBuffer createBuffer(ByteBuf buf) {
        if (debugBuffer == null) {
            return new PacketBuffer(buf);
        }
        return debugBuffer.apply(buf);
    }

    private static void writeBufToStream(PacketBuffer buffer, OutputStream stream) throws IOException {
        stream.write(buffer.array(), 0, buffer.readableBytes());
    }
}
