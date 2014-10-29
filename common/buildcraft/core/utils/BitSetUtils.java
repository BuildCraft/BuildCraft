package buildcraft.core.utils;

import java.util.BitSet;

public final class BitSetUtils {

	private BitSetUtils() {
		
	}
	
	public static BitSet fromByteArray(byte[] bytes) {
		BitSet bits = new BitSet(bytes.length * 8);
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - (i >> 3) - 1] & (1 << (i & 7))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	public static byte[] toByteArray(BitSet bits) {
		byte[] bytes = new byte[(bits.length() + 7) >> 3];
		System.out.println(bits.length() + " "  + bytes.length);
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length - (i >> 3) - 1] |= 1 << (i & 7);
			}
		}
		return bytes;
	}
}
