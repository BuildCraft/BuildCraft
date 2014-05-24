/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.NetworkData;

public final class BlueprintId implements Comparable<BlueprintId> {

	public enum Kind {
		Template, Blueprint
	};

	@NetworkData
	public byte[] uniqueId;

	@NetworkData
	public String name = "";

	@NetworkData
	public Kind kind = Kind.Blueprint;

	public String completeId;

	public BlueprintId() {
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

	public void write (NBTTagCompound nbt) {
		nbt.setByteArray("uniqueBptId", uniqueId);
		nbt.setString("name", name);
		nbt.setByte("kind", (byte) kind.ordinal());
	}

	public void read (NBTTagCompound nbt) {
		uniqueId = nbt.getByteArray("uniqueBptId");
		name = nbt.getString("name");
		kind = Kind.values()[nbt.getByte("kind")];
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BlueprintId) {
			return Arrays.equals(uniqueId, ((BlueprintId) obj).uniqueId);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(ArrayUtils.addAll(uniqueId, name.getBytes()));
	}

	public String getCompleteId () {
		if (completeId == null) {
			if (uniqueId.length > 0) {
				completeId = name + BuildCraftBuilders.BPT_SEP_CHARACTER
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
	public int compareTo(BlueprintId o) {
		return getCompleteId().compareTo(o.getCompleteId());
	}

	public static String toString (byte [] bytes) {
		char[] ret = new char[bytes.length * 2];

		for (int i = 0; i < bytes.length; i++) {
			int val = bytes [i] + 128;

			ret[i * 2] = toHex(val >> 4);
			ret[i * 2 + 1] = toHex(val & 0xf);
		}

		return new String (ret);
	}

	public static byte[] toBytes(String suffix) {
		byte [] result = new byte [suffix.length() / 2];

		for (int i = 0; i < result.length; ++i) {
			result [i] = (byte) ((byte) (fromHex(suffix.charAt(i * 2 + 1)))
					+ (byte) (fromHex(suffix.charAt(i * 2)) << 4));

			result [i] -= 128;
		}

		return result;
	}
}
