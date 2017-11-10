/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.ISerializable;
import buildcraft.core.lib.utils.NetworkUtils;

public final class LibraryId implements Comparable<LibraryId>, ISerializable {
	public static final char BPT_SEP_CHARACTER = '-';

	public byte[] uniqueId;
	public String name = "";
	public String extension = "tpl";

	public String completeId;

	public LibraryId() {
	}

	public void generateUniqueId(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] id = digest.digest(data);

			uniqueId = id;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(NBTTagCompound nbt) {
		nbt.setByteArray("uniqueBptId", uniqueId);
		nbt.setString("name", name);
		nbt.setString("extension", extension);
	}

	public void read(NBTTagCompound nbt) {
		uniqueId = nbt.getByteArray("uniqueBptId");
		name = nbt.getString("name");
		if (nbt.hasKey("kind")) {
			extension = nbt.getByte("kind") > 0 ? "bpt" : "tpl";
		} else {
			extension = nbt.getString("extension");
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LibraryId) {
			return Arrays.equals(uniqueId, ((LibraryId) obj).uniqueId);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(ArrayUtils.addAll(uniqueId, name.getBytes()));
	}

	public String getCompleteId() {
		if (completeId == null) {
			if (uniqueId.length > 0) {
				completeId = name + BPT_SEP_CHARACTER
						+ toString(uniqueId);
			} else {
				completeId = name;
			}
		}

		return completeId;
	}

	@Override
	public String toString() {
		return getCompleteId();
	}

	private static char toHex(int i) {
		if (i < 10) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' - 10 + i);
		}
	}

	private static int fromHex(char c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		} else {
			return c - ('a' - 10);
		}
	}

	@Override
	public int compareTo(LibraryId o) {
		return getCompleteId().compareTo(o.getCompleteId());
	}

	public static String toString(byte[] bytes) {
		char[] ret = new char[bytes.length * 2];

		for (int i = 0; i < bytes.length; i++) {
			int val = bytes[i] + 128;

			ret[i * 2] = toHex(val >> 4);
			ret[i * 2 + 1] = toHex(val & 0xf);
		}

		return new String(ret);
	}

	public static byte[] toBytes(String suffix) {
		byte[] result = new byte[suffix.length() / 2];

		for (int i = 0; i < result.length; ++i) {
			result[i] = (byte) ((byte) (fromHex(suffix.charAt(i * 2 + 1)))
					+ (byte) (fromHex(suffix.charAt(i * 2)) << 4));

			result[i] -= 128;
		}

		return result;
	}

	@Override
	public void readData(ByteBuf stream) {
		uniqueId = NetworkUtils.readByteArray(stream);
		name = NetworkUtils.readUTF(stream);
		extension = NetworkUtils.readUTF(stream);
	}

	@Override
	public void writeData(ByteBuf stream) {
		NetworkUtils.writeByteArray(stream, uniqueId);
		NetworkUtils.writeUTF(stream, name);
		NetworkUtils.writeUTF(stream, extension);
	}
}
