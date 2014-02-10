package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class PacketNBT extends PacketCoordinates {

	private NBTTagCompound nbttagcompound;

	public PacketNBT() {
	}

	public PacketNBT(int id, NBTTagCompound nbttagcompound, int xCoord, int yCoord, int zCoord) {
		super(id, xCoord, yCoord, zCoord);
		this.nbttagcompound = nbttagcompound;
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);

		try {
			byte[] compressed = CompressedStreamTools.compress(nbttagcompound);
			data.writeShort(compressed.length);
			data.writeBytes(compressed);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		short length = data.readShort();
		byte[] compressed = new byte[length];
		data.readBytes(compressed);
		
		try {
			this.nbttagcompound = CompressedStreamTools.decompress(compressed);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public NBTTagCompound getTagCompound() {
		return this.nbttagcompound;
	}

}
