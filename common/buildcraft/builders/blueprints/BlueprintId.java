package buildcraft.builders.blueprints;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class BlueprintId {
	public static BlueprintId generate(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] id = digest.digest(data);

			return new BlueprintId(id);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static BlueprintId fromRawId(byte[] id) {
		if (id.length != 32) return null;

		return new BlueprintId(id);
	}

	private BlueprintId(byte[] id) {
		this.id = id;
	}

	public byte[] toRawId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BlueprintId) {
			return Arrays.equals(id, ((BlueprintId) obj).id);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(id);
	}

	@Override
	public String toString() {
		char[] ret = new char[id.length * 2];

		for (int i = 0; i < id.length; i++) {
			ret[i * 2] = toHex(id[i] >>> 4);
			ret[i * 2 + 1] = toHex(id[i] & 0xf);
		}

		return new String(ret);
	}

	private char toHex(int i) {
		return (char) (i < 10 ? '0' + i : 'a' - 10 + i);
	}

	private final byte[] id;
}
