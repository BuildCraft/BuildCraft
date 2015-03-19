package buildcraft.robotics.map;

import net.minecraft.block.material.MapColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import buildcraft.api.core.BCLog;

public class MapChunk {
	private static final int VERSION = 1;

	private int x, z;
	private byte[] data;

	public MapChunk(int x, int y) {
		this.x = x;
		this.z = z;
		data = new byte[256];
	}

	public MapChunk(NBTTagCompound compound) {
		readFromNBT(compound);
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public int getColor(int x, int z) {
		return (int) data[((z & 15) << 4) | (x & 15)];
	}

	public void update(Chunk chunk) {
		for (int z = 0; z < 16; z++) {
			for (int x = 0; x < 16; x++) {
				int y = chunk.getHeightValue(x, z);
				int color;

				while ((color = chunk.getBlock(x, y, z).getMapColor(0).colorIndex) == MapColor.airColor.colorIndex) {
					y--;
					if (y < 0) {
						break;
					}
				}

				data[(z << 4) | x] = (byte) color;
			}
		}
	}

	public void readFromNBT(NBTTagCompound compound) {
		int version = compound.getShort("version");
		if (version > MapChunk.VERSION) {
			BCLog.logger.error("Unsupported MapChunk version: " + version);
			return;
		}
		x = compound.getInteger("x");
		z = compound.getInteger("z");
		data = compound.getByteArray("data");
		if (data.length != 256) {
			BCLog.logger.error("Invalid MapChunk data length: " + data.length);
			data = new byte[256];
		}
	}

	public void writeToNBT(NBTTagCompound compound) {
		compound.setShort("version", (short) VERSION);
		compound.setInteger("x", x);
		compound.setInteger("z", z);
		compound.setByteArray("data", data);
	}

	@Override
	public int hashCode() {
		return 31 * x + z;
	}
}
