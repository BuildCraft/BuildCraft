package buildcraft.core.lib.utils;

import java.util.Random;

/**
 * Based on http://xorshift.di.unimi.it/xorshift128plus.c
 * TODO: This thing ought to have tests!
 */
public class XorShift128Random {
	private static final Random seed = new Random();
	private static final double DOUBLE_UNIT = 0x1.0p-53;
	private final long[] s = new long[2];

	public XorShift128Random() {
		s[0] = seed.nextLong();
		s[1] = seed.nextLong();
	}

	public long nextLong() {
		long s1 = s[0];
		long s0 = s[1];
		s[0] = s0;
		s1 ^= s1 << 23;
		s[1] = (s1 ^ s0 ^ (s1 >> 17) ^ (s0 >> 26)) + s0;
		return s[1];
	}

	public int nextInt() {
		return (int) nextLong();
	}

	public boolean nextBoolean() {
		return (nextLong() & 0x1) != 0;
	}

	public int nextInt(int size) {
		int nl = (int) nextLong();
		return nl < 0 ? ((nl + 0x80000000) % size) : (nl % size);
	}

	public double nextDouble() {
		return (double) (long) (nextLong() & 0x1FFFFFFFFFFFFFL) * DOUBLE_UNIT;
	}
}
