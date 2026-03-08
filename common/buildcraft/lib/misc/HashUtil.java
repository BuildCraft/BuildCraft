package buildcraft.lib.misc;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.TreeMap;

public class HashUtil {
    public static final int DIGEST_LENGTH = 32;
    private static final MessageDigest SHA_256;
    private static final MethodHandle HANDLE_NBT_WRITE;

    static {
        try {
            SHA_256 = MessageDigest.getInstance("sha-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        int realLength = SHA_256.getDigestLength();
        if (realLength != DIGEST_LENGTH) {
            // Just in case
            throw new IllegalStateException("Digest length of sha-256 is meant to be 32, but returned " + realLength);
        }
        Method[] methods = Tag.class.getDeclaredMethods();
        Class<?>[] expectedParams = { DataOutput.class };
        Method read = null;
        for (Method m : methods) {
            // Target is NbtBase.write(DataOutput output)
            Class<?>[] params = m.getParameterTypes();
            if (Arrays.equals(expectedParams, params)) {
                if (read != null) {
                    throw new IllegalStateException("Found multiple acceptable methods! (" + read + " and " + m + ")");
                }
                read = m;
                read.setAccessible(true);
            }
        }
        try {
            Lookup lookup = MethodHandles.lookup();
            HANDLE_NBT_WRITE = lookup.unreflect(read);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
        // Test the method -- just in case
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("test", 42);
        computeHash(nbt);
    }

    public static byte[] computeHash(byte[] data) {
        return SHA_256.digest(data);
    }

    public static byte[] computeHash(CompoundTag nbt) {
        // Order is important here - we have to use a stable algorithm for the order
        // (Otherwise we depend on the order that HashMap assigns us)
        try (DigestOutputStream dos = createDigestStream()) {

            writeStableCompound(nbt, new DataOutputStream(dos));

        } catch (IOException io) {
            throw new RuntimeException("Failed to write to a perfectly good DigestOutputStream!", io);
        }
        return SHA_256.digest();
    }

    public static DigestOutputStream createDigestStream() {
        return new DigestOutputStream(ByteStreams.nullOutputStream(), SHA_256);
    }

    public static String convertHashToString(byte[] hash) {
        StringBuilder str = new StringBuilder();
        for (byte b : hash) {
            String s = Integer.toString(Byte.toUnsignedInt(b), 16);
            if (s.length() < 2) {
                str.append('0');
            }
            str.append(s);
        }
        return str.toString();
    }

    public static byte[] convertStringToHash(String str) {
        byte[] hash = new byte[str.length() / 2];
        for (int i = 0; i < hash.length; i++) {
            String s2 = str.substring(i * 2, i * 2 + 2);
            hash[i] = (byte) Integer.parseInt(s2, 16);
        }
        return hash;
    }

    // #####################
    //
    // Stable NBT writer
    //
    // #####################

    private static void writeStableCompound(CompoundTag nbt, DataOutput out) throws IOException {
        TreeMap<String, Tag> entries = new TreeMap<>();
        for (String key : nbt.getAllKeys()) {
            entries.put(key, nbt.get(key));
        }
        for (String key : entries.keySet()) {
            Tag tag = entries.get(key);
            byte id = tag.getId();
            out.writeByte(id);
            if (id != 0) {
                out.writeUTF(key);
                writeStableNbt(tag, out);
            }
            out.writeByte(0);
        }
    }

    private static void writeStableList(ListTag nbt, DataOutput out) throws IOException {
        // We have to intercept lists as they might contain compounds
        // (Although normal lists are already stable)
        int type;
        if (nbt.isEmpty()) {
            type = 0;
        } else {
            type = nbt.get(0).getId();
        }
        out.writeByte(type);
        out.writeInt(nbt.size());
        for (int i = 0; i < nbt.size(); i++) {
            writeStableNbt(nbt.get(i), out);
        }
    }

    private static void writeStableNbt(Tag nbt, DataOutput out) throws IOException {
        if (nbt instanceof CompoundTag) {
            writeStableCompound((CompoundTag) nbt, out);
        } else if (nbt instanceof ListTag) {
            writeStableList((ListTag) nbt, out);
        } else {
            // Normal NBT writing is package-private
            // We can skip around it with hacks though
            try {
                HANDLE_NBT_WRITE.invokeExact(nbt, out);
            } catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        }
    }
}
