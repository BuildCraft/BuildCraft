package buildcraft.core.tablet;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.network.PacketIds;

public class PacketTabletMessage extends Packet {
	private NBTTagCompound tag;

	public PacketTabletMessage() {
		tag = new NBTTagCompound();
	}

	public PacketTabletMessage(NBTTagCompound tag) {
		this.tag = tag;
	}

	@Override
	public int getID() {
		return PacketIds.TABLET_MESSAGE;
	}

	public NBTTagCompound getTag() {
		return tag;
	}

	@Override
	public void readData(ByteBuf data) {
		int length = data.readUnsignedShort();
		byte[] compressed = new byte[length];
		data.readBytes(compressed);

		try {
			this.tag = CompressedStreamTools.func_152457_a(compressed, NBTSizeTracker.field_152451_a);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		try {
			byte[] compressed = CompressedStreamTools.compress(tag);
			if (compressed.length > 65535) {
				BCLog.logger.error("NBT data is too large (" + compressed.length + " > 65535)! Please report!");
			}
			data.writeShort(compressed.length);
			data.writeBytes(compressed);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
