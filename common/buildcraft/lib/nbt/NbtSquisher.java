/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.data.NbtSquishConstants;
import buildcraft.lib.misc.ProfilerUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.*;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NbtSquisher {
    // public static final Profiler profiler = new Profiler();
    public static final ProfilerFiller profiler = ProfilerUtil.newProfiler();

    /** Used by testing classes to replace ByteBuf instances with PrintingByteBuf -- but we don't have that
     * class in main because it makes checkstyle complain. */
    public static Function<ByteBuf, FriendlyByteBuf> debugBuffer = null;

    private static final int TYPE_MC_GZIP = NbtSquishConstants.VANILLA_COMPRESSED;
    private static final int TYPE_MC = NbtSquishConstants.VANILLA;
    private static final int TYPE_BC_1_GZIP = NbtSquishConstants.BUILDCRAFT_V1_COMPRESSED;
    private static final int TYPE_BC_1 = NbtSquishConstants.BUILDCRAFT_V1;

    public static byte[] squish(CompoundTag nbt, int type) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            squish(nbt, type, baos);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write to a perfectly good ByteArrayOutputStream", e);
        }
        return baos.toByteArray();
    }

    public static void squish(CompoundTag nbt, int type, ByteBuf buf) {
        try (ByteBufOutputStream bbos = new ByteBufOutputStream(buf)) {
            squish(nbt, type, bbos);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write to a perfectly good ByteBufOutputStream", e);
        }
    }

    public static void squish(CompoundTag nbt, int type, OutputStream stream) throws IOException {
        switch (type) {
            case TYPE_MC:
                squishVanillaUncompressed(nbt, new DataOutputStream(stream));
                return;
            case TYPE_MC_GZIP:
                squishVanilla(nbt, stream);
                return;
            case TYPE_BC_1:
                squishBuildCraftV1Uncompressed(nbt, new DataOutputStream(stream));
                return;
            case TYPE_BC_1_GZIP:
                squishBuildCraftV1(nbt, stream);
                return;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    public static void squishVanilla(CompoundTag nbt, OutputStream to) throws IOException {
        to.write(NbtSquishConstants.BUILDCRAFT_MAGIC_1);
        to.write(NbtSquishConstants.BUILDCRAFT_MAGIC_2);
        to.write(TYPE_MC_GZIP);
//        CompressedStreamTools.writeCompressed(nbt, to);
        NbtIo.writeCompressed(nbt, to);
    }

    public static void squishVanillaUncompressed(CompoundTag nbt, DataOutput to) throws IOException {
        to.writeShort(NbtSquishConstants.BUILDCRAFT_MAGIC);
        to.write(TYPE_MC);
//        CompressedStreamTools.write(nbt, to);
        NbtIo.write(nbt, to);
    }

    public static void squishBuildCraftV1(CompoundTag nbt, OutputStream to) throws IOException {
        to.write(NbtSquishConstants.BUILDCRAFT_MAGIC_1);
        to.write(NbtSquishConstants.BUILDCRAFT_MAGIC_2);
        to.write(TYPE_BC_1_GZIP);
        try (GZIPOutputStream gzip = new GZIPOutputStream(to, true)) {
            squishBuildCraftV1Direct(nbt, new DataOutputStream(gzip));
        }
    }

    public static void squishBuildCraftV1Uncompressed(CompoundTag nbt, DataOutput to) throws IOException {
        to.write(NbtSquishConstants.BUILDCRAFT_MAGIC_1);
        to.write(NbtSquishConstants.BUILDCRAFT_MAGIC_2);
        to.write(TYPE_BC_1);
        squishBuildCraftV1Direct(nbt, to);
    }

    public static CompoundTag expand(byte[] bytes) throws IOException {
        return expand(new ByteArrayInputStream(bytes));
    }

    public static CompoundTag expand(ByteBuf buf) throws IOException {
        return expand(new ByteBufInputStream(buf));
    }

    public static CompoundTag expand(InputStream stream) throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(5);
        int byte1 = stream.read();
        int byte2 = stream.read();

        if (byte1 == NbtSquishConstants.BUILDCRAFT_MAGIC_1 && byte2 == NbtSquishConstants.BUILDCRAFT_MAGIC_2) {
            // Defiantly a BC stream
            int type = stream.read();
            if (type == TYPE_MC) {
//                return CompressedStreamTools.read(new DataInputStream(stream));
                return NbtIo.read(new DataInputStream(stream));
            } else if (type == TYPE_MC_GZIP) {
//                return CompressedStreamTools.readCompressed(stream);
                return NbtIo.readCompressed(stream);
            } else if (type == TYPE_BC_1) {
                return readBuildCraftV1Direct(new DataInputStream(stream));
            } else if (type == TYPE_BC_1_GZIP) {
                return readBuildCraftV1Direct(new DataInputStream(new GZIPInputStream(stream)));
            } else {
                throw new InvalidInputDataException("Cannot handle BuildCraft saved NBT type " + type);
            }
        } else if (byte1 == NbtSquishConstants.GZIP_MAGIC_1 && byte2 == NbtSquishConstants.GZIP_MAGIC_2) {
            // Defiantly a GZIP stream
            // Assume its a vanilla file
            stream.reset();
//            return CompressedStreamTools.readCompressed(stream);
            return NbtIo.readCompressed(stream);
        }
        // Its not a new BC style nbt, try to red it as if it was an older style nbt
        // Reset + mark the same point, this time we only want to reset back 1 or 2 bytes
        stream.reset();
        stream.mark(5);
        int type = stream.read();

        if (type == TYPE_MC) {
//            return CompressedStreamTools.read(new DataInputStream(stream));
            return NbtIo.read(new DataInputStream(stream));
        } else if (type == TYPE_MC_GZIP) {
//            return CompressedStreamTools.readCompressed(stream);
            return NbtIo.readCompressed(stream);
        } else if (type == TYPE_BC_1) {
            return readBuildCraftV1Direct(new DataInputStream(stream));
        } else if (type == TYPE_BC_1_GZIP) {
            return readBuildCraftV1Direct(new DataInputStream(new GZIPInputStream(stream)));
        } else if (type == Tag.TAG_COMPOUND) {
            // Assume vanilla, but reset back to the first byte as vanilla needs
            stream.reset();
//            return CompressedStreamTools.read(new DataInputStream(stream));
            return NbtIo.read(new DataInputStream(stream));
        } else {
            throw new InvalidInputDataException("Cannot handle unknown saved NBT type " + type);
        }
    }

    private static CompoundTag readBuildCraftV1Direct(DataInput in) throws IOException {
        NbtSquishMap map = NbtSquishMapReader.read(in);
        WrittenType type = map.getWrittenType();
        int index = type.readIndex(in);
        return map.getFullyReadComp(index);
    }

    private static void squishBuildCraftV1Direct(CompoundTag nbt, DataOutput to) throws IOException {
        NbtSquishMap map = new NbtSquishMap();
        map.addTag(nbt);
        NbtSquishMapWriter.debug = debugBuffer != null;
        NbtSquishMapWriter.write(map, to);
        WrittenType type = map.getWrittenType();
        type.writeIndex(to, map.indexOfTag(nbt));
    }
}
