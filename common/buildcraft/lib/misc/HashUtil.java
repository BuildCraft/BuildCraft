package buildcraft.lib.misc;

import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.io.ByteStreams;

public class HashUtil {
    private static final MessageDigest SHA_256;
    public static final int DIGEST_LENGTH = 32;

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
    }

    public static byte[] computeHash(byte[] data) {
        return SHA_256.digest(data);
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
}
