package buildcraft.core.network;

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
	public void writeData(DataOutputStream data) throws IOException {

		super.writeData(data);

		byte[] compressed = CompressedStreamTools.compress(nbttagcompound);
		data.writeShort(compressed.length);
		data.write(compressed);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {

		super.readData(data);

		short length = data.readShort();
		byte[] compressed = new byte[length];
		data.readFully(compressed);
		this.nbttagcompound = CompressedStreamTools.decompress(compressed);
	}

	public NBTTagCompound getTagCompound() {
		return this.nbttagcompound;
	}

}
