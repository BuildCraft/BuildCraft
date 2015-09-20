package buildcraft.core.lib.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

public final class NetworkUtils {
	private NetworkUtils() {

	}

	public static void writeUTF(ByteBuf data, String str) {
		try {
			if (str == null) {
				data.writeInt(0);
				return;
			}
			byte[] b = str.getBytes("UTF-8");
			data.writeInt(b.length);
			data.writeBytes(b);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			data.writeInt(0);
		}
	}

	public static String readUTF(ByteBuf data) {
		try {
			int len = data.readInt();
			if (len == 0) {
				return "";
			}
			byte[] b = new byte[len];
			data.readBytes(b);
			return new String(b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeNBT(ByteBuf data, NBTTagCompound nbt) {
		try {
			byte[] compressed = CompressedStreamTools.compress(nbt);
			data.writeInt(compressed.length);
			data.writeBytes(compressed);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static NBTTagCompound readNBT(ByteBuf data) {
		try {
			int length = data.readInt();
			byte[] compressed = new byte[length];
			data.readBytes(compressed);
			return CompressedStreamTools.func_152457_a(compressed, NBTSizeTracker.field_152451_a);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeStack(ByteBuf data, ItemStack stack) {
		if (stack == null || stack.getItem() == null || stack.stackSize < 0) {
			data.writeByte(0);
		} else {
			// ItemStacks generally shouldn't have a stackSize above 64,
			// so we use this "trick" to save bandwidth by storing it in the first byte.
			data.writeByte((MathUtils.clamp(stack.stackSize + 1, 0, 64) & 0x7F) | (stack.hasTagCompound() ? 128 : 0));
			data.writeShort(Item.getIdFromItem(stack.getItem()));
			data.writeShort(stack.getItemDamage());
			if (stack.hasTagCompound()) {
				writeNBT(data, stack.getTagCompound());
			}
		}
	}

	public static ItemStack readStack(ByteBuf data) {
		int flags = data.readUnsignedByte();
		if (flags == 0) {
			return null;
		} else {
			boolean hasCompound = (flags & 0x80) != 0;
			int stackSize = (flags & 0x7F) - 1;
			int itemId = data.readUnsignedShort();
			int itemDamage = data.readShort();
			ItemStack stack = new ItemStack(Item.getItemById(itemId), stackSize, itemDamage);
			if (hasCompound) {
				stack.setTagCompound(readNBT(data));
			}
			return stack;
		}
	}

	public static void writeByteArray(ByteBuf stream, byte[] data) {
		stream.writeInt(data.length);
		stream.writeBytes(data);
	}

	public static byte[] readByteArray(ByteBuf stream) {
		byte[] data = new byte[stream.readInt()];
		stream.readBytes(data, 0, data.length);
		return data;
	}
}
